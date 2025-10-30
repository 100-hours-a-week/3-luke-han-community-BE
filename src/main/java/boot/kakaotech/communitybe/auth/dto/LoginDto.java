package boot.kakaotech.communitybe.auth.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {

    private String email;

    private String password;

}
