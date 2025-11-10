package boot.kakaotech.communitybe.auth.service;

import boot.kakaotech.communitybe.auth.dto.SignupRequest;
import boot.kakaotech.communitybe.common.encoder.PasswordEncoder;
import boot.kakaotech.communitybe.user.entity.User;
import boot.kakaotech.communitybe.user.repository.UserRepository;
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

    private final PasswordEncoder passwordEncoder;

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

        // TODO: 밸리데이션 함수

        // TODO: 키 만드는거 메서드로 빼기
        String key = "user:" + request.getEmail() + ":" + UUID.randomUUID() + ":" + request.getProfileImageName();

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .profileImageKey(key)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
        String presignedURl = ""; // TODO: S3에서 PUT용 presigned url 발급

        log.info("[AuthService] 회원가입 성공");
        return presignedURl;
    }

}
