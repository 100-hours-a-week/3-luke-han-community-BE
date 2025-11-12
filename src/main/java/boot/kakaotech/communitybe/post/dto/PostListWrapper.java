package boot.kakaotech.communitybe.post.dto;

import boot.kakaotech.communitybe.user.dto.SimpUserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostListWrapper {

    private PostListDto post;

    private SimpUserInfo author;

}
