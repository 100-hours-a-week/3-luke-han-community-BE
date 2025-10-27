package boot.kakaotech.communitybe.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupDto {

    private String email;

    private String password;

    private String nickname;

    private String profileImageName;

}
