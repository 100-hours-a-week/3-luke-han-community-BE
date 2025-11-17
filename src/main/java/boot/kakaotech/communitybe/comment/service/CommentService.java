package boot.kakaotech.communitybe.comment.service;

import boot.kakaotech.communitybe.comment.dto.CommentDto;
import boot.kakaotech.communitybe.common.scroll.dto.CursorPage;

public interface CommentService {

    CursorPage<CommentDto> getComments(Integer postId, Integer parentId, Integer cursor, Integer size);

}
