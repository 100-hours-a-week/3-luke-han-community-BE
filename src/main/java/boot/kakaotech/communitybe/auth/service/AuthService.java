package boot.kakaotech.communitybe.auth.service;

import boot.kakaotech.communitybe.auth.dto.LoginDto;
import boot.kakaotech.communitybe.auth.dto.LoginUserDto;
import boot.kakaotech.communitybe.auth.dto.SignupDto;
import boot.kakaotech.communitybe.auth.dto.ValueDto;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {

    String signup(SignupDto signupDto);

    boolean checkEmail(ValueDto valueDto);

    boolean checkNickname(ValueDto valueDto);

    LoginUserDto login(HttpServletResponse response, LoginDto dto);

    void logout(HttpServletResponse response);

}
