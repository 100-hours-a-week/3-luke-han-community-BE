package boot.kakaotech.communitybe.auth.service;

import boot.kakaotech.communitybe.auth.dto.SignupRequest;
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

    @Override
    @Transactional
    public String signup(SignupRequest request) {
        log.info("[AuthService] 회원가입 시작");

        // TODO: 밸리데이션 함수

        String key = "user:" + request.getEmail() + ":" + UUID.randomUUID() + ":" + request.getProfileImageName();

        User user = User.builder()
                .email(request.getEmail())
                .password("" /* TODO: passwordEncoder */)
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
