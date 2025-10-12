package boot.kakaotech.communitybe.comment.service;

import boot.kakaotech.communitybe.auth.dto.ValueDto;
import boot.kakaotech.communitybe.comment.dto.CommentDto;
import boot.kakaotech.communitybe.comment.dto.CreateCommentDto;
import boot.kakaotech.communitybe.comment.entity.Comment;
import boot.kakaotech.communitybe.comment.repository.CommentRepository;
import boot.kakaotech.communitybe.common.exception.BusinessException;
import boot.kakaotech.communitybe.common.exception.ErrorCode;
import boot.kakaotech.communitybe.common.scroll.dto.CursorPage;
import boot.kakaotech.communitybe.post.entity.Post;
import boot.kakaotech.communitybe.post.repository.PostRepository;
import boot.kakaotech.communitybe.user.entity.User;
import boot.kakaotech.communitybe.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    private final UserUtil userUtil;

    @Override
    public CursorPage<CommentDto> getComments(Integer postId, Integer parentId, Integer cursor, Integer size) {
        log.info("[CommentService] 댓글 조회 시작");

        Pageable pageable = PageRequest.of(cursor, size);
        List<CommentDto> comments = commentRepository.getComments(postId, parentId, pageable);

        if (comments.isEmpty()) {
            return null;
        }

        boolean hasNextCursor = comments.size() > size;
        Integer nextCursor = hasNextCursor ? comments.getLast().getId() : null;

        return CursorPage.<CommentDto>builder()
                .list(comments)
                .hasNextCursor(hasNextCursor)
                .nextCursor(nextCursor)
                .build();
    }

    @Override
    @Transactional
    public Integer addComment(Integer postId, CreateCommentDto dto) throws UserPrincipalNotFoundException {
        log.info("[CommentService] 댓글 생성 시작");

        Integer parentId = dto.getParentId();
        Comment parent = commentRepository.findById(parentId == null ? 0 : parentId).orElse(null);
        Post post = postRepository.findById(postId).orElse(null);
        User user = userUtil.getCurrentUser();

        Comment comment = Comment.builder()
                .parentComment(parent)
                .post(post)
                .user(user)
                .depth(parent == null ? 0 : parent.getDepth() + 1)
                .content(dto.getContent())
                .build();

        commentRepository.save(comment);

        return comment.getId();
    }

    @Override
    @Transactional
    public void updateComment(Integer commentId, ValueDto value) throws UserPrincipalNotFoundException {
        log.info("[CommentService] 댓글 수정 시작");

        User user = userUtil.getCurrentUser();

        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
        }

        if (!comment.getUser().equals(user)) {
            throw new BusinessException(ErrorCode.REQUEST_FROM_OTHERS);
        }

        comment.setContent(value.getValue());
    }

    @Override
    @Transactional
    public void softDeleteComment(Integer commentId) throws UserPrincipalNotFoundException {
        log.info("[CommentService] 댓글 삭제 시작");
        User user = userUtil.getCurrentUser();

        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
        }

        if (!comment.getUser().equals(user)) {
            throw new BusinessException(ErrorCode.REQUEST_FROM_OTHERS);
        }

        comment.setDeletedAt(LocalDateTime.now());
    }

}
