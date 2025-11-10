package boot.kakaotech.communitybe.common.validation;

import boot.kakaotech.communitybe.auth.dto.LoginRequest;
import boot.kakaotech.communitybe.auth.dto.SignupRequest;
import boot.kakaotech.communitybe.common.encoder.PasswordEncoder;
import boot.kakaotech.communitybe.common.exception.BusinessException;
import boot.kakaotech.communitybe.common.exception.ErrorCode;
import boot.kakaotech.communitybe.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class Validator {

    private final PasswordEncoder passwordEncoder;

    /**
     * 이메일과 패스워드 validation하는 메서드
     * 맞지 않으면 throw error
     *
     * @param request
     * @param user
     */
    public void validateUserInfo(LoginRequest request, User user) {
        if (user == null || passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_USERINFO);
        }
    }

    /**
     * signup 요청 시 받은 데이터 검증 메서드
     * 1. 이메일 형식 체크
     * 2. 비밀번호 길이, 영문 대문자, 소문자, 특수문자, 숫자 체크
     * 3. 닉네임 길이, 공백 체크
     *
     * @param signupRequest
     */
    public void validateSignup(SignupRequest signupRequest) {
        String email = signupRequest.getEmail();
        validateEmail(email);

        String password = signupRequest.getPassword();
        validatePassword(password);

        String nickname = signupRequest.getNickname();
        validateNickname(nickname);
    }

    /**
     * 이메일 validation하는 메서드
     * ooo@ooo.ooo 형식인지 확인
     *
     * @param email
     */
    private void validateEmail(String email) {
        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

        if (!email.matches(regex)) {
            throw new BusinessException(ErrorCode.INVALID_FORMAT);
        }
    }

    /**
     * 비밀번호 validation하는 메서드
     * 8자 이상, 20자 이하, 대문자, 소문자, 특수문자 1개 이상 포함하는지 확인
     *
     * @param password
     */
    private void validatePassword(String password) {
        String regex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=`~\\[\\]{};':\",./<>?]).+$";

        if (password.length() < 8 || password.length() > 20 || !password.matches(regex)) {
            throw new BusinessException(ErrorCode.INVALID_FORMAT);
        }
    }

    /**
     * 닉네임 validation하는 메서드
     * 비어있는지, 10자 이내인지, 중간에 띄어쓰기 있는지 확인
     *
     * @param nickname
     */
    private void validateNickname(String nickname) {
        String regex = ".*\\s.*";

        if (nickname.isEmpty() || nickname.length() > 10 || nickname.matches(regex)) {
            throw new BusinessException(ErrorCode.INVALID_FORMAT);
        }
    }



}
