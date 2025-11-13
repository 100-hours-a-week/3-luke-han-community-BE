package boot.kakaotech.communitybe.post.service;

import boot.kakaotech.communitybe.comment.dto.CommentDto;
import boot.kakaotech.communitybe.comment.repository.CommentRepository;
import boot.kakaotech.communitybe.common.properties.PrefixProperty;
import boot.kakaotech.communitybe.common.scroll.dto.CursorPage;
import boot.kakaotech.communitybe.common.util.KeyValueStore;
import boot.kakaotech.communitybe.common.validation.Validator;
import boot.kakaotech.communitybe.post.dto.CreatePostDto;
import boot.kakaotech.communitybe.post.dto.PostDetailWrapper;
import boot.kakaotech.communitybe.post.dto.PostListWrapper;
import boot.kakaotech.communitybe.post.dto.SavedPostDto;
import boot.kakaotech.communitybe.post.entity.Post;
import boot.kakaotech.communitybe.post.entity.PostImage;
import boot.kakaotech.communitybe.post.repository.PostRepository;
import boot.kakaotech.communitybe.common.util.ThreadLocalContext;
import boot.kakaotech.communitybe.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    private final ThreadLocalContext context;
    private final KeyValueStore kvStore;
    private final Validator validator;

    private final PrefixProperty property;

    /**
     * 게시글 목록 조회하는 메서드
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

        CursorPage<PostListWrapper> response = createPostList(posts, size);
        return response;
    }

    /**
     * post detail을 조회하는 메서드
     * 1. 현재 요청한 유저id 조회
     * 2. PostDetailWrapper 조회
     *
     * @param postId
     * @return
     */
    @Override
    public PostDetailWrapper getPost(int postId) {
        log.info("[PostService] 게시글 상세조회 시작 - postId = {}", postId);

        int userId = context.getCurrentUserId();
        PostDetailWrapper post = makePostDetail(postId, userId);

        return post;
    }

    /**
     * 게시글 저장하는 메서드
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional
    public SavedPostDto savePost(CreatePostDto dto) {
        log.info("[PostService] 게시글 생성 시작");

        User author = context.getCurrentUser();
        Post post = createPost(author, dto);

        List<PostImage> imagesList = new ArrayList<>();
        List<String> presignedUrls = new ArrayList<>();

        changeAndSetImages(post, presignedUrls, imagesList, dto.getImages());

        return SavedPostDto.builder()
                .postId(post.getId())
                .presignedUrls(presignedUrls)
                .build();
    }

    /**
     * 게시글 수정하는 메서드
     * 1. 요청에 대한 validation 후 title과 content 수정
     * 2. 이미지 업로드는 Lambda로 진행할거라 이 부분 어떻게 처리할지 고민해야 함
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional
    public SavedPostDto updatePost(CreatePostDto dto) {
        log.info("[PostService] 게시글 수정 시작 - postId: {}", dto.getId());

        User user = context.getCurrentUser();
        Post post = changePost(user, dto);

        // TODO: 이미지 처리 람다로

        return SavedPostDto.builder()
                .postId(post.getId())
                .presignedUrls(null)
                .build();
    }

    /**
     * 수정 요청을 보낸이에 대한 검증과 수정을 담당하는 메서드
     *
     * @param user
     * @param dto
     * @return
     */
    private Post changePost(User user, CreatePostDto dto) {
        Post post = postRepository.findById(dto.getId()).orElse(null);
        validator.validatePostAndAuthor(post, user);

        String title = dto.getTitle();
        String content = dto.getContent();

        if (title != null) {
            post.setTitle(title);
        }

        if (content != null) {
            post.setContent(content);
        }

        return post;
    }

    /**
     * Post 엔티티 생성해서 저장 후 id까지 반환하는 메서드
     *
     * @param author
     * @param dto
     * @return
     */
    private Post createPost(User author, CreatePostDto dto) {
        Post post = Post.builder()
                .author(author)
                .title(dto.getTitle())
                .content(dto.getContent())
                .viewCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        postRepository.saveAndFlush(post);
        return post;
    }

    /**
     * 이미지 파일명으로 PostImage 엔티티 생성 및 presigned url 발급하는 로직
     * - 근데 lambda로 이미지 업로드 구현하면 이 부분은 필요없을지도
     *
     * @param post
     * @param presignedUrls
     * @param imagesList
     * @param images
     */
    private void changeAndSetImages(Post post, List<String> presignedUrls, List<PostImage> imagesList, List<String> images) {
        images.stream().forEach(image -> {
            String imageKey = "post:" + post.getId() + ":" + UUID.randomUUID() + ":" + image;
            imagesList.add(PostImage.builder().post(post).imageKey(imageKey).build());
            presignedUrls.add("" /* TODO: presigned url 생성 로직 추가 */);
            // 근데 아마 이거 올리는거 lambda 쓰면 이거 없어도 될지도
        });

        post.setImages(imagesList);
    }

    /**
     * PostDetailWrapper 조회 후 반환하는 메서드
     * 1. DB에서 조회
     * 2. 게시글 이미지들 추가
     * 3. 작성자 추가
     * 4. 댓글 추가
     * 5. 조회수 증가
     * 6. 반환
     *
     * @param postId
     * @param userId
     * @return
     */
    private PostDetailWrapper makePostDetail(int postId, int userId) {
        PostDetailWrapper post = postRepository.getPostById(postId, userId);

        if (post == null) {
            return null;
        }

        setImageIntoPostDetail(post);
        setAuthorIntoPostDetail(post);
        setCommentsIntoPostDetail(post);
        increaseViewCount(post);

        return post;
    }

    /**
     * PostDetailWrapper 내 게시글 이미지 추가하는 메서드
     * 1. DB에서 이미지 조회
     * 2. 이미지 별 presigned url 발급받아 post에 세팅
     *
     * @param post
     */
    private void setImageIntoPostDetail(PostDetailWrapper post) {
        List<String> images = postRepository.getImages(post.getPost().getId());
        post.getPost().setImages(new ArrayList<>());
        images.stream().forEach(image -> {
            String presignedUrl = ""; // TODO: GET용 presigned url 발급로직 추가
            post.getPost().getImages().add(presignedUrl);
        });
    }

    /**
     * PostDetailWrapper 내 작성자 프로필이미지 presigned url 추가하는 메서드
     *
     * @param post
     */
    private void setAuthorIntoPostDetail(PostDetailWrapper post) {
        String authorProfile = post.getAuthor().getProfileImageUrl();
        post.getAuthor().setProfileImageUrl(
                "" // TODO: author의 profileImage presigned url 발급로직 추가
        );
    }

    /**
     * PostDetailWrapper 내 댓글 추가하는 메서드
     * 1. DB에서 댓글 조회
     * 2. 댓글 작성자마다 프로필 presigned url 추가
     * 3. post에 적재
     *
     * @param post
     */
    private void setCommentsIntoPostDetail(PostDetailWrapper post) {
        Pageable pageable = PageRequest.of(0, 10);
        List<CommentDto> comments = commentRepository.getComments(post.getPost().getId(), 0, pageable);
        // DB에서 comment 조회
        comments.stream().forEach(comment -> {
            String profileImageUrl = comment.getProfileImageUrl();
            comment.setProfileImageUrl("" /* TODO: 댓글 작성자의 profileImage presigned url 발급로직 */);
        }); // List 돌면서 presigned url 발급
        post.setComments(comments);
    }

    /**
     * 조회수 증가시키는 메서드
     * 1. kvStore 내 저장된거 확인
     *
     * @param post
     */
    private void increaseViewCount(PostDetailWrapper post) {
        int postId = post.getPost().getId();
        String key = property.getViewCount() + postId;

        Integer viewCount = Integer.valueOf(kvStore.get(key)); // kvStore에서 조회
        if (viewCount == null) { // kvStore에 없으면
            viewCount = postRepository.findViewCountByPostId(postId).orElse(0);
            // DB에서 조회하는데 없으면 0으로 초기화
        }

        kvStore.put(key, String.valueOf(++viewCount));
        post.getPost().setViewCount(viewCount);
        // kvStore에 추가 및 post에 적재
    }

    /**
     * CursorPage 객체에 게시글 리스트 담아서 반환하는 메서드
     * - posts의 size로 다음 게시글 있는지 확인 후 반환
     *
     * @param posts
     * @param size
     * @return
     */
    private CursorPage<PostListWrapper> createPostList(List<PostListWrapper> posts, int size) {
        boolean hasNextCursor = posts.size() > size;
        Integer nextCursor = hasNextCursor ? posts.getLast().getPost().getId() : null;

        if (hasNextCursor) {
            posts.removeLast();
        }

        // TODO: GET용 presigned url 발급

        return CursorPage.<PostListWrapper>builder()
                .list(posts)
                .hasNextCursor(hasNextCursor)
                .nextCursor(nextCursor)
                .build();
    }

}
