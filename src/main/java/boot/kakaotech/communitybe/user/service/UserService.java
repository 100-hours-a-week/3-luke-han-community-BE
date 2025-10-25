package boot.kakaotech.communitybe.user.service;

import boot.kakaotech.communitybe.user.dto.PasswordDto;
import boot.kakaotech.communitybe.user.dto.SimpUserInfo;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.nio.file.attribute.UserPrincipalNotFoundException;

public interface UserService {

    String updateUserInfo(SimpUserInfo userInfo) throws UserPrincipalNotFoundException;

    void updatePassword(PasswordDto password) throws UserPrincipalNotFoundException;

}
