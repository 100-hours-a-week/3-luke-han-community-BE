package boot.kakaotech.communitybe.auth.service;

import boot.kakaotech.communitybe.auth.dto.SignupRequest;

public interface AuthService {

    String signup(SignupRequest request);

}
