package boot.kakaotech.communitybe.post.service;

import boot.kakaotech.communitybe.comment.dto.CommentDto;
import boot.kakaotech.communitybe.comment.dto.CommentThreadDto;
import boot.kakaotech.communitybe.comment.repository.CommentRepository;
import boot.kakaotech.communitybe.common.properties.PrefixProperty;
import boot.kakaotech.communitybe.common.properties.S3Property;
import boot.kakaotech.communitybe.common.s3.service.S3Service;
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

    private static final int PARENT_COMMENT_SIZE = 10;
    private static final int CHILD_PREVIEW_SIZE   = 3;

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    private final ThreadLocalContext context;
    private final KeyValueStore kvStore;
    private final Validator validator;

    private final PrefixProperty property;
    private final S3Service s3Service;
    private final S3Property s3Property;

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

        List<String> presignedUrls = makePresignedUrls(post, dto);

        return SavedPostDto.builder()
                .postId(post.getId())
                .presignedUrls(presignedUrls)
                .build();
    }

    /**
     * 게시글 삭제하는 메서드
     * 1. 요청 보낸 유저 조회
     * 2. 해당 게시글 조회
     * 3. validation
     * 4. deletedAt 세팅
     *
     * @param postId
     */
    @Override
    @Transactional
    public void softDeletePost(int postId) {
        log.info("[PostService] 게시글 삭제 시작 - postId = {}", postId);

        User user = context.getCurrentUser();
        Post post = postRepository.findById(postId).orElse(null);
        validator.validatePostAndAuthor(post, user);

        post.setDeletedAt(LocalDateTime.now());
    }

    private List<String> makePresignedUrls(Post post, CreatePostDto dto) {
        List<String> presignedUrls = new ArrayList<>();
        List<String> images = dto.getImages();

        if (!dto.getIsImageChanged()) {
            return null;
        }

        post.clearImages();
        if (images != null && !images.isEmpty()) {
            for (String originalFileName : images) {
                if (originalFileName == null || originalFileName.isEmpty()) {
                    continue;
                }

                String key = s3Service.makePostKey(post.getId(), originalFileName);
                PostImage newImage = PostImage.builder().post(post).imageKey(key).build();
                post.addImage(newImage);

                presignedUrls.add(s3Service.createPUTPresignedUrl(s3Property.getS3().getBucket(), key));
            }
        }

        return presignedUrls;
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
            String imageKey = s3Service.makePostKey(post.getId(), image);
            imagesList.add(PostImage.builder().post(post).imageKey(imageKey).build());
            presignedUrls.add(s3Service.createPUTPresignedUrl(s3Property.getS3().getBucket(), imageKey));
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
            String key = s3Service.makePostKey(post.getPost().getId(), image);
            String presignedUrl = s3Service.createGETPresignedUrl(s3Property.getS3().getBucket(), key);
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
                s3Service.createGETPresignedUrl(s3Property.getS3().getBucket(), authorProfile)
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
        int postId = post.getPost().getId();

        CursorPage<CommentThreadDto> threads = loadCommentThreadsForDetail(postId);

        post.setComments(threads);
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

        String value = kvStore.get(key); // kvStore에서 조회
        Integer viewCount;
        Integer originalViewCount = postRepository.findViewCountByPostId(post.getPost().getId()).orElse(0);
        if (value == null) { // kvStore에 없으면
            viewCount = 0;
        } else {
            viewCount = Integer.parseInt(value);
        }

        kvStore.put(key, String.valueOf(++viewCount));
        post.getPost().setViewCount(originalViewCount + viewCount);
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

        posts.forEach(post -> {
            String profileImageKey = post.getAuthor().getProfileImageUrl();

            post.getAuthor().setProfileImageUrl(s3Service.createGETPresignedUrl(s3Property.getS3().getBucket(), profileImageKey));
        });

        return CursorPage.<PostListWrapper>builder()
                .list(posts)
                .hasNextCursor(hasNextCursor)
                .nextCursor(nextCursor)
                .build();
    }

    /**
     * 부모 댓글과 함께 자식 댓글 최대 3개까지 담아 반환하는 메서드
     * - 댓글 더보기 기능을 위해 추가
     *
     * @param postId
     * @return
     */
    private CursorPage<CommentThreadDto> loadCommentThreadsForDetail(int postId) {
        // 1) 부모 댓글 페이지 로드 (cursor 계산 포함)
        ParentCommentPage parentPage = loadParentComments(postId);

        // 2) 부모 리스트를 CommentThreadDto 리스트로 변환
        List<CommentThreadDto> threads = buildThreadsForParents(postId, parentPage.parents());

        // 3) 최종 CursorPage로 래핑
        return toCommentCursorPage(threads, parentPage);
    }

    /**
     * 부모(최상위) 댓글들을 조회하고, cursor/hasNext까지 계산하는 메서드
     */
    private ParentCommentPage loadParentComments(int postId) {
        Pageable parentPageable = PageRequest.of(0, PARENT_COMMENT_SIZE + 1);

        List<CommentDto> parents = commentRepository.getComments(postId, 0, parentPageable);

        boolean hasNextParent = parents.size() > PARENT_COMMENT_SIZE;
        Integer nextParentCursor = null;

        if (hasNextParent) {
            CommentDto last = parents.remove(parents.size() - 1);
            nextParentCursor = last.getId();      // 다음 페이지용 cursor (id 기반)
        }

        // 부모 댓글 작성자 프로필 presigned 세팅
        setImagesIntoList(parents);

        return new ParentCommentPage(parents, hasNextParent, nextParentCursor);
    }

    /**
     * 부모 댓글 리스트를 받아서 각 부모에 대한 CommentThreadDto를 생성하는 메서드
     */
    private List<CommentThreadDto> buildThreadsForParents(int postId, List<CommentDto> parents) {
        List<CommentThreadDto> threads = new ArrayList<>();

        for (CommentDto parent : parents) {
            CommentThreadDto thread = buildSingleThread(postId, parent);
            threads.add(thread);
        }

        return threads;
    }

    /**
     * 하나의 부모 댓글에 대한 CommentThreadDto를 만드는 메서드
     * - 자식 댓글 프리뷰 로드 + hasMoreChildren / childNextCursor 세팅
     */
    private CommentThreadDto buildSingleThread(int postId, CommentDto parent) {
        ChildCommentPage childPage = loadChildPreview(postId, parent.getId());

        return CommentThreadDto.builder()
                .parent(parent)
                .children(childPage.children())
                .hasMoreChildren(childPage.hasMore())
                .childNextCursor(childPage.nextCursor())
                .build();
    }

    /**
     * 특정 부모 댓글에 대한 자식 댓글(대댓글) 프리뷰를 로드하는 메서드
     * - 최대 CHILD_PREVIEW_SIZE + 1개 조회해서 hasMoreChildren 결정
     */
    private ChildCommentPage loadChildPreview(int postId, Integer parentId) {
        Pageable childPageable = PageRequest.of(0, CHILD_PREVIEW_SIZE + 1);

        List<CommentDto> children = commentRepository.getComments(postId, parentId, childPageable);

        boolean hasMoreChildren = children.size() > CHILD_PREVIEW_SIZE;
        Integer childNextCursor = null;

        if (hasMoreChildren) {
            CommentDto lastChild = children.remove(children.size() - 1);
            childNextCursor = lastChild.getId();
        }

        // 자식 댓글 작성자 프로필 presigned 세팅
        setImagesIntoList(children);

        return new ChildCommentPage(children, hasMoreChildren, childNextCursor);
    }

    /**
     * CommentThreadDto 리스트와 부모 댓글 페이지 정보를 이용해 CursorPage로 포장하는 메서드
     */
    private CursorPage<CommentThreadDto> toCommentCursorPage(
            List<CommentThreadDto> threads,
            ParentCommentPage parentPage
    ) {
        return CursorPage.<CommentThreadDto>builder()
                .list(threads)
                .hasNextCursor(parentPage.hasNext())
                .nextCursor(parentPage.nextCursor())
                .build();
    }

    private void setImagesIntoList(List<CommentDto> comments) {
        comments.forEach(comment -> {
            String profileImageUrl = comment.getProfileImageUrl();
            comment.setProfileImageUrl(
                    s3Service.createGETPresignedUrl(s3Property.getS3().getBucket(), profileImageUrl)
            );
        });
    }

    private record ParentCommentPage(
            List<CommentDto> parents,
            boolean hasNext,
            Integer nextCursor
    ) {}

    private record ChildCommentPage(
            List<CommentDto> children,
            boolean hasMore,
            Integer nextCursor
    ) {}

}
