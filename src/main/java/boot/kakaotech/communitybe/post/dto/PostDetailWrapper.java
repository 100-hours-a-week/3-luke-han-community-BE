package boot.kakaotech.communitybe.post.dto;

import boot.kakaotech.communitybe.comment.dto.CommentThreadDto;
import boot.kakaotech.communitybe.common.scroll.dto.CursorPage;
import boot.kakaotech.communitybe.user.dto.SimpUserInfo;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailWrapper {

    private SimpUserInfo author;

    private PostDetailDto post;

    private CursorPage<CommentThreadDto> comments;

}
