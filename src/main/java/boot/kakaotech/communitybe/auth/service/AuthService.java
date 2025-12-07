package boot.kakaotech.communitybe.auth.service;

import boot.kakaotech.communitybe.auth.dto.LoginRequest;
import boot.kakaotech.communitybe.auth.dto.LoginResponse;
import boot.kakaotech.communitybe.auth.dto.SignupRequest;
import boot.kakaotech.communitybe.auth.dto.ValueDto;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    String signup(SignupRequest request);

    LoginResponse login(HttpServletResponse response, LoginRequest loginRequest);

    boolean checkEmail(ValueDto value);

    boolean checkNickname(ValueDto value);

    void logout(HttpServletResponse response);

}
