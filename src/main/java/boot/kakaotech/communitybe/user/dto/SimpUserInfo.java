package boot.kakaotech.communitybe.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpUserInfo {

    private Integer id;

    private String name;

    private String profileImageUrl;

    public void setId(Integer id) {
        this.id = id;
    }

}
