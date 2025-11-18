package boot.kakaotech.communitybe.comment.service;

import boot.kakaotech.communitybe.auth.dto.ValueDto;
import boot.kakaotech.communitybe.comment.dto.CommentDto;
import boot.kakaotech.communitybe.comment.dto.CreateCommentDto;
import boot.kakaotech.communitybe.common.scroll.dto.CursorPage;

public interface CommentService {

    CursorPage<CommentDto> getComments(Integer postId, Integer parentId, Integer cursor, Integer size);

    Integer addComment(Integer postId, CreateCommentDto dto);

    void updateComment(Integer commentId, ValueDto valueDto);

}
