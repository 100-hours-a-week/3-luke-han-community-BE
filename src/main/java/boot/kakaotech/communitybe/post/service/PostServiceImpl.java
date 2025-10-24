package boot.kakaotech.communitybe.post.service;

import boot.kakaotech.communitybe.comment.dto.CommentDto;
import boot.kakaotech.communitybe.comment.repository.CommentRepository;
import boot.kakaotech.communitybe.common.exception.BusinessException;
import boot.kakaotech.communitybe.common.exception.ErrorCode;
import boot.kakaotech.communitybe.common.s3.service.S3Service;
import boot.kakaotech.communitybe.common.scroll.dto.CursorPage;
import boot.kakaotech.communitybe.post.dto.CreatePostDto;
import boot.kakaotech.communitybe.post.dto.SavedPostDto;
import boot.kakaotech.communitybe.post.dto.PostDetailWrapper;
import boot.kakaotech.communitybe.post.dto.PostListWrapper;
import boot.kakaotech.communitybe.post.entity.Post;
import boot.kakaotech.communitybe.post.entity.PostImage;
import boot.kakaotech.communitybe.post.repository.PostRepository;
import boot.kakaotech.communitybe.user.entity.User;
import boot.kakaotech.communitybe.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final S3Service s3Service;

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    private final UserUtil userUtil;

    private final RedisTemplate<String, String> redisTemplate;
    private static final String VIEW_COUNT_PREFIX = "post_view:";

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 게시글 목록 조회
     * 1. Pageable 객체 생성
     * 2. repository로 List 조회
     * 3. 비어있으면 null 반환
     * 4. size를 하나 더 가져와서 다음에 요청 보낼게 있나 flag 설정 후 반환
     *
     * @param cursor
     * @param size
     * @return
     */
    @Override
    public CursorPage<PostListWrapper> getPosts(int cursor, int size) {
        log.info("[PostService] 게시글 목록 조회 시작");

        Pageable pageable = PageRequest.of(cursor, size);
        List<PostListWrapper> posts = postRepository.getPostsUsingFetch(pageable);

        if (posts.isEmpty()) {
            return null;
        }

        boolean hasNextCursor = posts.size() > size;
        Integer nextCursor = hasNextCursor ? posts.getLast().getPost().getId() : null;

        if (hasNextCursor) {
            posts.removeLast();
        }

        return CursorPage.<PostListWrapper>builder()
                .list(posts)
                .hasNextCursor(hasNextCursor)
                .nextCursor(nextCursor)
                .build();
    }

    /**
     * 게시글 상세조회
     * 1. PostDetailWrapper dto에 맞춰 조회
     * 2. null이면 반환
     * 3. 뷰카운터 증가
     * 4. post 반환
     *
     * @param postId
     * @return
     */
    @Override
    public PostDetailWrapper getPost(int postId) {
        log.info("[PostService] 게시글 상세조회 시작");

        PostDetailWrapper post = postRepository.getPostById(postId);

        if (post == null) {
            return null;
        }

        List<String> images = postRepository.getImages(postId);
        post.getPost().setImages(new ArrayList<>());
        images.stream().forEach(image -> {
            String presignedUrl = s3Service.createGETPresignedUrl(bucket, image);
            post.getPost().getImages().add(presignedUrl);
        });

        String viewCount = redisTemplate.opsForValue().get(VIEW_COUNT_PREFIX + postId);

        Integer count = 0;
        if (viewCount == null) {
            count = post.getPost().getViewCount();
        } else {
            count = Integer.parseInt(viewCount);
        }

        redisTemplate.opsForValue().set(
                VIEW_COUNT_PREFIX + postId,
                Integer.toString(count + 1)
        );
        post.getPost().setViewCount(count + 1);

        Pageable pageable = PageRequest.of(0, 10);
        List<CommentDto> comments = commentRepository.getComments(postId, 0, pageable);
        post.setComments(comments);

        return post;
    }

    /**
     * 게시글 생성
     * 1. 요청보낸 User 조회
     * 2. Post 엔티티 생성
     * 3. 이미지가 있다면 presigned url 발급
     * 4. 저장
     *
     * @param createPostDto
     * @return
     * @throws UserPrincipalNotFoundException
     */
    @Override
    @Transactional
    public SavedPostDto savePost(CreatePostDto createPostDto) throws UserPrincipalNotFoundException {
        log.info("[PostService] 게시글 생성 시작");

        User author = userUtil.getCurrentUser();

        Post post = Post.builder()
                .author(author)
                .title(createPostDto.getTitle())
                .content(createPostDto.getContent())
                .viewCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        List<PostImage> imagesList = new ArrayList<>();
        List<String> presignedUrls = new ArrayList<>();
        List<String> images = createPostDto.getImages();

        postRepository.saveAndFlush(post);
        images.stream().forEach(image -> {
            String imageKey = "post:" + post.getId() + ":" + UUID.randomUUID() + ":" + image;
            imagesList.add(PostImage.builder().post(post).imageKey(imageKey).build());
            presignedUrls.add(s3Service.createPUTPresignedUrl(bucket, imageKey));
        });

        post.setImages(imagesList);

        return SavedPostDto.builder()
                .postId(post.getId())
                .presignedUrls(presignedUrls)
                .build();
    }

    /**
     * 게시글 수정
     * 1. 요청보낸 User 조회
     * 2. Post 조회
     * 3. post가 없거나 작성자가 아니면 throw error
     * 4. 게시글 수정
     * 5. 이미지 변경이 있다면 새로운 이미지 presigned url 생성 후 반환
     * 6. 저장
     *
     * @param createPostDto
     * @throws UserPrincipalNotFoundException
     */
    @Override
    @Transactional
    public SavedPostDto updatePost(CreatePostDto createPostDto) throws UserPrincipalNotFoundException {
        log.info("[PostService] 게시글 수정 시작");

        User user = userUtil.getCurrentUser();

        Post post = postRepository.findById(createPostDto.getId()).orElse(null);
        if (post == null) {
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
        }

        if (!post.getAuthor().equals(user)) {
            throw new BusinessException(ErrorCode.REQUEST_FROM_OTHERS);
        }

        String title = createPostDto.getTitle();
        String content = createPostDto.getContent();

        if (title != null) {
            post.setTitle(title);
        }

        if (content != null) {
            post.setContent(content);
        }

        List<String> presignedUrls = new ArrayList<>();
        List<String> images = createPostDto.getImages();
        if (createPostDto.getIsImageChanged()) {
            post.clearImages();

            if (images != null && !images.isEmpty()) {
                for (String originalFileName : images) {
                    if (originalFileName != null || originalFileName.isBlank()) {
                        continue;
                    }

                    String key = "post:" + post.getId() + ":" + UUID.randomUUID().toString() + ":" + originalFileName;

                    PostImage newImage = PostImage.builder().post(post).imageKey(key).build();
                    post.addImage(newImage);

                    presignedUrls.add(s3Service.createPUTPresignedUrl(bucket, key));
                }
            }
        }

        return SavedPostDto.builder()
                .postId(post.getId())
                .presignedUrls(presignedUrls)
                .build();
    }

    /**
     * 게시글 삭제
     * 1. 요청보낸 User 조회
     * 2. post 조회
     * 3. 없거나 작성자가 아니면 throw error
     * 4. post의 deletedAt을 설정 후 저장
     *
     * @param postId
     * @throws UserPrincipalNotFoundException
     */
    @Override
    public void softDeletePost(int postId) throws UserPrincipalNotFoundException {
        log.info("[PostService] 게시글 삭제 시작");

        User user = userUtil.getCurrentUser();

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
        }

        if (!post.getAuthor().equals(user)) {
            throw new BusinessException(ErrorCode.REQUEST_FROM_OTHERS);
        }

        post.setDeletedAt(LocalDateTime.now());
        postRepository.save(post);
    }

}
