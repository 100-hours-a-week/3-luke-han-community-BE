package boot.kakaotech.communitybe.comment.service;

import boot.kakaotech.communitybe.comment.dto.CommentDto;
import boot.kakaotech.communitybe.comment.repository.CommentRepository;
import boot.kakaotech.communitybe.common.scroll.dto.CursorPage;
import boot.kakaotech.communitybe.common.util.ThreadLocalContext;
import boot.kakaotech.communitybe.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    private final ThreadLocalContext context;

    /**
     * 무한스크롤링 적용된 댓글 리스트 조회하는 메서드
     *
     * @param postId
     * @param parentId
     * @param cursor
     * @param size
     * @return
     */
    @Override
    public CursorPage<CommentDto> getComments(Integer postId, Integer parentId, Integer cursor, Integer size) {
        log.info("[CommentService] 댓글 조회 시작 - postId: {}", postId);

        List<CommentDto> comments = getCommentsFromRepository(postId, parentId, cursor, size);
        setImagesIntoList(comments);

        return makeCursorPageIncludedComments(comments, size);
    }

    /**
     * CursorPage 객체에 데이터 담아 반환하는 메서드
     *
     * @param comments
     * @param size
     * @return
     */
    private CursorPage<CommentDto> makeCursorPageIncludedComments(List<CommentDto> comments, int size) {
        boolean hasNextCursor = comments.size() > size;
        Integer nextCursor = hasNextCursor ? comments.getLast().getId() : null;

        return CursorPage.<CommentDto>builder()
                .list(comments)
                .hasNextCursor(hasNextCursor)
                .nextCursor(nextCursor)
                .build();
    }

    /**
     * 댓글 작성자 프로필 이미지 presigned url 발급하는 메서드
     *
     * @param comments
     */
    private void setImagesIntoList(List<CommentDto> comments) {
        comments.forEach(comment -> {
            String profileImageUrl = comment.getProfileImageUrl();
            // TODO: presigned url 발급 메서드
        });
    }

    /**
     * Pageable 객체 생성해서 Repository에서 이미지 조회하는 메서드
     *
     * @param postId
     * @param parentId
     * @param cursor
     * @param size
     * @return
     */
    private List<CommentDto> getCommentsFromRepository(Integer postId, Integer parentId, Integer cursor, Integer size) {
        Pageable pageable = PageRequest.of(cursor, size);
        return commentRepository.getComments(postId, parentId, pageable);
    }

}
