package boot.kakaotech.communitybe.user.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpUserInfo {

    private Integer id;

    private String name;

    private String profileImageKey;

}
