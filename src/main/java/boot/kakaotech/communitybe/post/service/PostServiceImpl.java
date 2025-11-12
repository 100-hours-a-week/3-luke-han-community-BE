package boot.kakaotech.communitybe.post.service;

import boot.kakaotech.communitybe.comment.repository.CommentRepository;
import boot.kakaotech.communitybe.common.properties.PrefixProperty;
import boot.kakaotech.communitybe.common.scroll.dto.CursorPage;
import boot.kakaotech.communitybe.common.util.KeyValueStore;
import boot.kakaotech.communitybe.post.dto.PostListWrapper;
import boot.kakaotech.communitybe.post.repository.PostRepository;
import boot.kakaotech.communitybe.common.util.ThreadLocalContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    private final ThreadLocalContext context;
    private final KeyValueStore kvStore;

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
