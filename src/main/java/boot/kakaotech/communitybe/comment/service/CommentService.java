package boot.kakaotech.communitybe.comment.service;

import boot.kakaotech.communitybe.auth.dto.ValueDto;
import boot.kakaotech.communitybe.comment.dto.CommentDto;
import boot.kakaotech.communitybe.comment.dto.CreateCommentDto;
import boot.kakaotech.communitybe.common.scroll.dto.CursorPage;

import java.nio.file.attribute.UserPrincipalNotFoundException;

public interface CommentService {

    CursorPage<CommentDto> getComments(Integer postId, Integer parentId, Integer cursor, Integer size);

    Integer addComment(Integer postId, CreateCommentDto createCommentDto) throws UserPrincipalNotFoundException;

    void updateComment(Integer commentId, ValueDto value) throws UserPrincipalNotFoundException;

    void softDeleteComment(Integer commentId) throws UserPrincipalNotFoundException;

}
