package boot.kakaotech.communitybe.comment.service;

import boot.kakaotech.communitybe.auth.dto.ValueDto;
import boot.kakaotech.communitybe.comment.dto.CommentDto;
import boot.kakaotech.communitybe.comment.dto.CreateCommentDto;
import boot.kakaotech.communitybe.comment.entity.Comment;
import boot.kakaotech.communitybe.comment.repository.CommentRepository;
import boot.kakaotech.communitybe.common.exception.BusinessException;
import boot.kakaotech.communitybe.common.exception.ErrorCode;
import boot.kakaotech.communitybe.common.s3.service.S3Service;
import boot.kakaotech.communitybe.common.scroll.dto.CursorPage;
import boot.kakaotech.communitybe.post.entity.Post;
import boot.kakaotech.communitybe.post.repository.PostRepository;
import boot.kakaotech.communitybe.user.entity.User;
import boot.kakaotech.communitybe.util.UserUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final S3Service s3Service;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 댓글 조회
     * 1. Pageable 객체 생성
     * 2. CommentDto를 리스트로 repository를 통해 조회
     * 3. 비어있으면 null 반환
     * 4. size + 1로 조회 후 더 볼게 있는지 flag 설정 후 반환
     *
     * @param postId
     * @param parentId
     * @param cursor
     * @param size
     * @return
     */
    @Override
    public CursorPage<CommentDto> getComments(Integer postId, Integer parentId, Integer cursor, Integer size) {
        log.info("[CommentService] 댓글 조회 시작");

        Pageable pageable = PageRequest.of(cursor, size);
        List<CommentDto> comments = commentRepository.getComments(postId, parentId, pageable);

        if (comments.isEmpty()) {
            return null;
        }

        comments.forEach(comment -> {
            String profileImageUrl = comment.getProfileImageUrl();
            comment.setProfileImageUrl(s3Service.createGETPresignedUrl(bucket, profileImageUrl));
        });
        boolean hasNextCursor = comments.size() > size;
        Integer nextCursor = hasNextCursor ? comments.getLast().getId() : null;

        return CursorPage.<CommentDto>builder()
                .list(comments)
                .hasNextCursor(hasNextCursor)
                .nextCursor(nextCursor)
                .build();
    }

    /**
     * 댓글 생성
     * 1. 대댓글이라면 부모댓글 조회
     * 2. post와 현재 댓글 생성 요청 보낸 user 조회
     * 3. 새 Comment 엔티티 생성
     * 4. 저장
     *
     * @param postId
     * @param dto
     * @return
     * @throws UserPrincipalNotFoundException
     */
    @Override
    @Transactional
    public Integer addComment(HttpServletRequest request, Integer postId, CreateCommentDto dto) throws UserPrincipalNotFoundException {
        log.info("[CommentService] 댓글 생성 시작, content: {}", dto.getContent());

        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Integer parentId = dto.getParentId();
        Comment parent = commentRepository.findById(parentId == null ? 0 : parentId).orElse(null);
        Post post = postRepository.findById(postId).orElse(null);
        User user = userUtil.getCurrentUser(session);

        Comment comment = Comment.builder()
                .parentComment(parent)
                .post(post)
                .user(user)
                .depth(parent == null ? 0 : parent.getDepth() + 1)
                .content(dto.getContent())
                .createdAt(LocalDateTime.now())
                .build();

        commentRepository.save(comment);

        return comment.getId();
    }

    /**
     * 댓글 수정
     * 1. 요청보낸 User 조회
     * 2. Comment 조회
     * 3. null이거나 댓글 작성자가 아닌 경우 throw error
     * 4. 수정 후 저장
     *
     * @param commentId
     * @param value
     * @throws UserPrincipalNotFoundException
     */
    @Override
    @Transactional
    public void updateComment(HttpServletRequest request, Integer commentId, ValueDto value) throws UserPrincipalNotFoundException {
        log.info("[CommentService] 댓글 수정 시작");

        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        User user = userUtil.getCurrentUser(session);

        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
        }

        if (!comment.getUser().equals(user)) {
            throw new BusinessException(ErrorCode.REQUEST_FROM_OTHERS);
        }

        comment.setContent(value.getValue());
    }

    /**
     * 댓글 삭제
     * 1. 요청 보낸 유저 조회
     * 2. 댓글 조회
     * 3. 없거나 작성자 아니면 throw error
     * 4. Comment의 deletedAt 세팅 후 저장
     *
     * @param commentId
     * @throws UserPrincipalNotFoundException
     */
    @Override
    @Transactional
    public void softDeleteComment(HttpServletRequest request, Integer commentId) throws UserPrincipalNotFoundException {
        log.info("[CommentService] 댓글 삭제 시작");

        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        User user = userUtil.getCurrentUser(session);

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
