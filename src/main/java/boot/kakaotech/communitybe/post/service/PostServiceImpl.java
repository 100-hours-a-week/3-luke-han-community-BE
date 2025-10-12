package boot.kakaotech.communitybe.post.service;

import boot.kakaotech.communitybe.common.exception.BusinessException;
import boot.kakaotech.communitybe.common.exception.ErrorCode;
import boot.kakaotech.communitybe.common.scroll.dto.CursorPage;
import boot.kakaotech.communitybe.post.dto.CreatePostDto;
import boot.kakaotech.communitybe.post.dto.PostDetailWrapper;
import boot.kakaotech.communitybe.post.dto.PostListWrapper;
import boot.kakaotech.communitybe.post.entity.Post;
import boot.kakaotech.communitybe.post.entity.PostImage;
import boot.kakaotech.communitybe.post.repository.PostRepository;
import boot.kakaotech.communitybe.user.entity.User;
import boot.kakaotech.communitybe.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    private final UserUtil userUtil;

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

        // TODO: 레디스에서 viewCount 증가로직 추가

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
     * @param images
     * @return
     * @throws UserPrincipalNotFoundException
     */
    @Override
    @Transactional
    public Integer savePost(CreatePostDto createPostDto, List<String> images) throws UserPrincipalNotFoundException {
        log.info("[PostService] 게시글 생성 시작");

        User author = userUtil.getCurrentUser();

        Post post = Post.builder()
                .author(author)
                .title(createPostDto.getTitle())
                .content(createPostDto.getContent())
                .viewCount(0)
                .build();

        List<PostImage> imagesList = null;
        // TODO: presigned url 발급받는 로직 추가

        postRepository.save(post);
        return post.getId();
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
     * @param images
     * @throws UserPrincipalNotFoundException
     */
    @Override
    @Transactional
    public void updatePost(CreatePostDto createPostDto, List<String> images) throws UserPrincipalNotFoundException {
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

        if (images != null) {
            // TODO: 이미지 변경 확인 후 postImage 전체 수정하기
        }

        postRepository.save(post);
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
