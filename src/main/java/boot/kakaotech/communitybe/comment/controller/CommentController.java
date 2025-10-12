package boot.kakaotech.communitybe.comment.controller;

import boot.kakaotech.communitybe.auth.dto.ValueDto;
import boot.kakaotech.communitybe.comment.dto.CommentDto;
import boot.kakaotech.communitybe.comment.dto.CreateCommentDto;
import boot.kakaotech.communitybe.comment.service.CommentService;
import boot.kakaotech.communitybe.common.scroll.dto.CursorPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.attribute.UserPrincipalNotFoundException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/{postId}/comments")
    public ResponseEntity<CursorPage<CommentDto>> getComments(
            @PathVariable("postId") Integer postId,
            @RequestParam("pid") Integer parentId,
            @RequestParam("cursor") Integer cursor,
            @RequestParam("size") Integer size
    ) {
        log.info("[CommentController] 댓글 조회 시작 - postId: {}, parentId: {}", postId, parentId);

        CursorPage<CommentDto> comments = commentService.getComments(postId, parentId, cursor, size);
        log.info("[CommentController] 댓글 조회 성공");

        return comments != null ? ResponseEntity.ok(comments) : ResponseEntity.noContent().build();
    }

    @PostMapping("/post/{postId}/comments")
    public ResponseEntity<Integer> addComment(@PathVariable("postId") Integer postId, @RequestBody CreateCommentDto dto) throws UserPrincipalNotFoundException {
        log.info("[CommentController] 댓글 생성 시작 - postId: {}", postId);

        Integer commentId = commentService.addComment(postId, dto);
        log.info("[CommentController] 댓글 생성 성공");

        return ResponseEntity.status(HttpStatus.CREATED).body(commentId);
    }

    @PatchMapping("/post/{postId}/comments/{commentId}")
    public ResponseEntity<Void> updateComment(
            @PathVariable("postId") Integer postId,
            @PathVariable("commentId") Integer commentId,
            @RequestBody ValueDto value
    ) {
        log.info("[CommentController] 댓글 수정 시작 - postId: {}, commentId: {}", postId, commentId);

        commentService.updateComment(commentId, value);
        log.info("[CommentController] 댓글 수정 성공");

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/post/{postId}/comments/{commentId}/status")
    public ResponseEntity<Void> updateCommentStatus(
            @PathVariable("postId") Integer postId,
            @PathVariable("commentId") Integer commentId
    ) {
        log.info("[CommentController] 댓글 삭제 시작 -  postId: {}, commentId: {}", postId, commentId);

        commentService.softDeleteComment(commentId);
        log.info("[CommentController] 댓글 삭제 성공");

        return ResponseEntity.ok().build();
    }

}
