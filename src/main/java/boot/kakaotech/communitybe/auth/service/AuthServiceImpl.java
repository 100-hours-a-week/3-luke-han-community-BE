package boot.kakaotech.communitybe.auth.service;

import boot.kakaotech.communitybe.auth.dto.LoginRequest;
import boot.kakaotech.communitybe.auth.dto.LoginResponse;
import boot.kakaotech.communitybe.auth.dto.SignupRequest;
import boot.kakaotech.communitybe.auth.dto.ValueDto;
import boot.kakaotech.communitybe.auth.jwt.JwtProvider;
import boot.kakaotech.communitybe.auth.jwt.TokenName;
import boot.kakaotech.communitybe.common.encoder.PasswordEncoder;
import boot.kakaotech.communitybe.common.properties.JwtProperty;
import boot.kakaotech.communitybe.common.properties.S3Property;
import boot.kakaotech.communitybe.common.s3.service.S3Service;
import boot.kakaotech.communitybe.common.validation.Validator;
import boot.kakaotech.communitybe.user.entity.User;
import boot.kakaotech.communitybe.user.repository.UserRepository;
import boot.kakaotech.communitybe.common.util.CookieUtil;
import boot.kakaotech.communitybe.common.util.ThreadLocalContext;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final JwtProvider provider;
    private final CookieUtil cookieUtil;
    private final Validator validator;
    private final ThreadLocalContext context;

    private final PasswordEncoder passwordEncoder;

    private final JwtProperty jwtProperty;
    private final S3Property s3Property;
    private final S3Service s3Service;

    /**
     * 회원가입 서비스 메서드
     *
     * 1. 회원가입 요청으로 들어온 DTO validation
     * 2. 이미지 S3용 키 발급
     * 3. 새 유저 생성
     * 4. 저장
     * 5. presigned url 발급 및 반환
     *
     * @param request
     * @return
     */
    @Override
    @Transactional
    public String signup(SignupRequest request) {
        log.info("[AuthService] 회원가입 시작");

        validator.validateSignup(request);
        // 요청값 검증

        boolean hasProfileImage = request.getProfileImageName() != null;
        String key = hasProfileImage ?  s3Service.makeUserProfileKey(request.getEmail(), request.getProfileImageName()) : null;

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // 비밀번호 해시 후 저장
                .nickname(request.getNickname())
                .profileImageKey(key)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user); // 생성 시 save() 호출해야함

        String presignedURl = null;
        if (hasProfileImage) {
            presignedURl = s3Service.createPUTPresignedUrl(s3Property.getS3().getBucket(), key);
        }

        return presignedURl;
    }

    /**
     * 로그인 메서드
     * 1. email로 User 조회
     * 2. 유저 존재 여부, 비밀번호 일치 여부 확인
     * 3. response 객체에 토큰 담기
     * 4. 프로필 사진 GET용 presigned url 발급
     * 5. LoginResponse 생성 후 반환
     *
     * @param response
     * @param loginRequest
     * @return
     */
    @Override
    public LoginResponse login(HttpServletResponse response, LoginRequest loginRequest) {
        log.info("[AuthService] 로그인 시작 - email: {}", loginRequest.getEmail());

        User user = userRepository.findByEmail(loginRequest.getEmail()).orElse(null);
        // Email로 유저 조회
        validator.validateUserInfo(loginRequest, user);
        // 유저 존재 여부, 비밀번호 일치 여부 확인

        String accessToken = provider.generateToken(user, TokenName.ACCESS_TOKEN);
        String refreshToken = provider.generateToken(user, TokenName.REFRESH_TOKEN);
        // 토큰 생성

        cookieUtil.addCookie(
                response,
                jwtProperty.getName().getRefreshToken(),
                refreshToken,
                (int) jwtProperty.getExpireTime().getRefreshTokenExpireTime() / 1000);
        // 쿠키에 넣기
        response.addHeader(jwtProperty.getAuthorization(), "Bearer " + accessToken);
        // Authorization 헤더에 넣기

        String presignedUrl = s3Service.createGETPresignedUrl(s3Property.getS3().getBucket(), user.getProfileImageKey());

        return LoginResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(presignedUrl)
                .build();
    }

    /**
     * 로그아웃 API
     * - ThreadLocal에 저장된 유저정보 삭제, 쿠키에서 Refresh Token 삭제
     *
     * @param response
     */
    @Override
    public void logout(HttpServletResponse response) {
        log.info("[AuthService] 로그아웃 시작");

        context.clear();
        cookieUtil.deleteCookie(response, jwtProperty.getName().getRefreshToken());
    }

    @Override
    public boolean checkEmail(ValueDto dto) {
        String email = dto.getValue();
        log.info("[AuthService] 이메일 중복확인 시작 - email: {}", email);

        validator.validateEmail(email);
        User user = userRepository.findByEmail(email).orElse(null);

        return user != null;
    }

    @Override
    public boolean checkNickname(ValueDto dto) {
        String nickname = dto.getValue();
        log.info("[AuthService] 닉네임 중복확인 시작 - nickname: {}", nickname);

        validator.validateNickname(nickname);
        User user = userRepository.findByNickname(nickname).orElse(null);

        return user != null;
    }

}
