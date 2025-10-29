package boot.kakaotech.communitybe.auth.service;

import boot.kakaotech.communitybe.auth.dto.SignupDto;
import boot.kakaotech.communitybe.auth.dto.ValueDto;
import boot.kakaotech.communitybe.common.encoder.PasswordEncoder;
import boot.kakaotech.communitybe.common.exception.BusinessException;
import boot.kakaotech.communitybe.common.exception.ErrorCode;
import boot.kakaotech.communitybe.common.s3.service.S3Service;
import boot.kakaotech.communitybe.user.entity.User;
import boot.kakaotech.communitybe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final S3Service s3Service;

    private final PasswordEncoder passwordEncoder;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 회원가입 하는 검증 메서드
     * 1. signup validation 진행
     * 2. 새 User 엔티티 생성
     * 3. save
     *
     * @param signupDto
     */
    @Override
    @Transactional
    public String signup(SignupDto signupDto) {
        log.info("[AuthService] 회원가입 시작");

        validateSignup(signupDto);
        String key = "user:" + signupDto.getEmail() + ":" + UUID.randomUUID().toString() + ":" + signupDto.getProfileImageName();

        User user = User.builder()
                .email(signupDto.getEmail())
                .password(passwordEncoder.encode(signupDto.getPassword()))
                .nickname(signupDto.getNickname())
                .profileImageUrl(key)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
        String presignedUrl = s3Service.createPUTPresignedUrl(bucket, key);

        log.info("[AuthService] 회원가입 성공");
        return presignedUrl;
    }

    /**
     * 중복 이메일 확인하는 메서드
     * 1. 이메일 validation 진행
     * 2. User 존재하는지 확인
     * 3. boolean 값 리턴
     *
     * @param value
     * @return
     */
    @Override
    public boolean checkEmail(ValueDto value) {
        log.info("[AuthService] 이메일 중복확인 시작");

        String email = value.getValue();
        validateEmail(email);
        User user = userRepository.findByEmail(email).orElse(null);

        return user != null;
    }

    /**
     * 닉네임 중복 여부 확인하는 메서드
     * 1. 닉네임 validation 진행
     * 2. User 존재하는지 확인
     * 3. boolean 값 리턴
     *
     * @param value
     * @return
     */
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
