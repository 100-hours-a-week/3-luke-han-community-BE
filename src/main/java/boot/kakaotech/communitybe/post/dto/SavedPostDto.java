package boot.kakaotech.communitybe.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedPostDto {

    private Integer postId;

    private List<String> presignedUrls;

}
