package boot.kakaotech.communitybe.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordDto {

    private Integer userId;

    private String oldPassword;

    private String newPassword;

}
