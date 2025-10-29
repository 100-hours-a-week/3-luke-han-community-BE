package boot.kakaotech.communitybe.user.service;

import boot.kakaotech.communitybe.user.dto.PasswordDto;
import boot.kakaotech.communitybe.user.dto.SimpUserInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.nio.file.attribute.UserPrincipalNotFoundException;

public interface UserService {

    String updateUserInfo(HttpServletRequest request, SimpUserInfo userInfo) throws UserPrincipalNotFoundException;

    void updatePassword(HttpServletRequest request, PasswordDto password) throws UserPrincipalNotFoundException;

}
