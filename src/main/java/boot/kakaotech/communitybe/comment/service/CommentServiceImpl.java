package boot.kakaotech.communitybe.comment.service;

import boot.kakaotech.communitybe.auth.dto.ValueDto;
import boot.kakaotech.communitybe.comment.dto.CommentDto;
import boot.kakaotech.communitybe.comment.dto.CreateCommentDto;
import boot.kakaotech.communitybe.comment.entity.Comment;
import boot.kakaotech.communitybe.comment.repository.CommentRepository;
import boot.kakaotech.communitybe.common.properties.S3Property;
import boot.kakaotech.communitybe.common.s3.service.S3Service;
import boot.kakaotech.communitybe.common.scroll.dto.CursorPage;
import boot.kakaotech.communitybe.common.util.ThreadLocalContext;
import boot.kakaotech.communitybe.common.validation.Validator;
import boot.kakaotech.communitybe.post.entity.Post;
import boot.kakaotech.communitybe.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final Validator validator;

    private final ThreadLocalContext context;
    private final S3Service s3Service;
    private final S3Property s3Property;

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

        int lastId = (cursor == null ? 0 : cursor);

        List<CommentDto> comments = commentRepository.getComments(postId, parentId, lastId, size);
        setImagesIntoList(comments);

        return makeCursorPageIncludedComments(comments, size);
    }

    /**
     * 댓글 생성 API
     *
     * @param postId
     * @param dto
     * @return
     */
    @Override
    public Integer addComment(Integer postId, CreateCommentDto dto) {
        log.info("[CommentService] 댓글 생성 시작, postId: {}", postId);

        Post post = validator.validatePostByIdAndReturn(postId);
        Integer parentId = dto.getParentId();
        Comment parent = commentRepository.findById(parentId).orElse(null);
        User user = context.getCurrentUser();

        Comment comment = Comment.builder()
                .parentComment(parent)
                .post(post)
                .user(user)
                .depth(parent == null ? 0 : 1) // 대댓글은 한 번까지만
                .content(dto.getContent())
                .createdAt(LocalDateTime.now())
                .build();

        commentRepository.save(comment);

        return comment.getId();
    }

    /**
     * 댓글 수정하는 메서드
     *
     * @param commentId
     * @param dto
     */
    @Override
    @Transactional
    public void updateComment(Integer commentId, ValueDto dto) {
        log.info("[CommentService] 댓글 수정 시작 - commentId: {}", commentId);

        User user = context.getCurrentUser();
        Comment comment = changeComment(commentId, user, dto);
    }

    @Override
    @Transactional
    public void softDeleteComment(Integer commentId) {
        log.info("[CommentService] 댓글 삭제 시작 - commentId: {}", commentId);

        User user = context.getCurrentUser();
        Comment comment = validator.validateCommentByIdAndReturn(commentId, user);

        comment.setDeletedAt(LocalDateTime.now());
    }

    /**
     * 수정 요청 받은 댓글에 대한 검증과 수정을 담당하는 메서드
     *
     * @param commentId
     * @param user
     * @param dto
     * @return
     */
    private Comment changeComment(Integer commentId, User user, ValueDto dto) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        validator.validateCommentAndAuthor(comment, user);

        comment.setContent(dto.getValue());

        return comment;
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
        Integer nextCursor = null;

        List<CommentDto> pageItems = comments;

        if (hasNextCursor) {
            pageItems = comments.subList(0, size);
            nextCursor = pageItems.get(pageItems.size() - 1).getId();
        }

        return CursorPage.<CommentDto>builder()
                .list(pageItems)
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
            String key = comment.getProfileImageUrl();

            if (key == null || key.isBlank()) {
                return;
            }

            comment.setProfileImageUrl(
                    s3Service.createGETPresignedUrl(s3Property.getS3().getBucket(), key)
            );
        });
    }

}
