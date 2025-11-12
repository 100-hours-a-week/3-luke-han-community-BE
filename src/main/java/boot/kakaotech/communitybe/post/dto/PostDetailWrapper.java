package boot.kakaotech.communitybe.post.dto;

import boot.kakaotech.communitybe.comment.dto.CommentDto;
import boot.kakaotech.communitybe.user.dto.SimpUserInfo;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailWrapper {

    private SimpUserInfo author;

    private PostDetailDto post;

    private List<CommentDto> comments;

}
