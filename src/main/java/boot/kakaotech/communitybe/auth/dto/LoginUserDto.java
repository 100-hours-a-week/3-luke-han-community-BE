package boot.kakaotech.communitybe.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserDto {

    private Integer userId;

    private String profileImageUrl;

}
