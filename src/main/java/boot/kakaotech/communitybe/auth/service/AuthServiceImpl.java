package boot.kakaotech.communitybe.auth.service;

import boot.kakaotech.communitybe.auth.dto.SignupDto;
import boot.kakaotech.communitybe.auth.dto.ValueDto;
import boot.kakaotech.communitybe.common.exception.BusinessException;
import boot.kakaotech.communitybe.common.exception.ErrorCode;
import boot.kakaotech.communitybe.common.s3.service.S3Service;
import boot.kakaotech.communitybe.user.entity.User;
import boot.kakaotech.communitybe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final S3Service s3Service;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void signup(SignupDto signupDto, MultipartFile file) {
        log.info("[AuthService] 회원가입 시작");

        validateSignup(signupDto);
        User user = User.builder()
                .email(signupDto.getEmail())
                .password(passwordEncoder.encode(signupDto.getPassword()))
                .nickname(signupDto.getNickname())
                .build();

        userRepository.save(user);
        // TODO: presigned url 발급 처리
        log.info("[AuthService] 회원가입 성공");
    }

    @Override
    public boolean checkEmail(ValueDto value) {
        log.info("[AuthService] 이메일 중복확인 시작");

        String email = value.getValue();
        validateEmail(email);
        User user = userRepository.findByEmail(email).orElse(null);

        return user != null;
    }

    @Override
    public boolean checkNickname(ValueDto value) {
        log.info("[AuthService] 닉네임 중복확인 시작");

        String nickname = value.getValue();
        validateNickname(nickname);
        User user = userRepository.findByNickname(nickname).orElse(null);

        return user != null;
    }

    /**
     * signup 요청 시 받은 데이터 검증 메서드
     * 1. 이메일 형식 체크
     * 2. 비밀번호 길이, 영문 대문자, 소문자, 특수문자, 숫자 체크
     * 3. 닉네임 길이, 공백 체크
     *
     * @param signupDto
     */
    private void validateSignup(SignupDto signupDto) {
        String email = signupDto.getEmail();
        validateEmail(email);

        String password = signupDto.getPassword();
        validatePassword(password);

        String nickname = signupDto.getNickname();
        validateNickname(nickname);
    }

    private void validateEmail(String email) {
        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

        if (!email.matches(regex)) {
            throw new BusinessException(ErrorCode.INVALID_FORMAT);
        }
    }

    private void validatePassword(String password) {
        String regex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=`~\\[\\]{};':\",./<>?]).+$";

        if (password.length() < 8 || password.length() > 20 || password.matches(regex)) {
            throw new BusinessException(ErrorCode.INVALID_FORMAT);
        }
    }

    private void validateNickname(String nickname) {
        String regex = ".*\\s.*";

        if (nickname.isEmpty() || nickname.length() > 10 || nickname.matches(regex)) {
            throw new BusinessException(ErrorCode.INVALID_FORMAT);
        }
    }

}
