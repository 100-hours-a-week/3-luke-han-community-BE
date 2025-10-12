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
