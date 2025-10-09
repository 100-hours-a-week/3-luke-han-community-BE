package boot.kakaotech.communitybe.user.service;

import boot.kakaotech.communitybe.user.dto.PasswordDto;
import boot.kakaotech.communitybe.user.dto.SimpUserInfo;
import boot.kakaotech.communitybe.user.entity.User;
import boot.kakaotech.communitybe.user.repository.UserRepository;
import boot.kakaotech.communitybe.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserUtil userUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void updateUserInfo(SimpUserInfo userInfo) {
        log.info("[UserService] 유저정보 업데이트 시작");

        int userId = userUtil.getCurrentUserId();

        User user = userRepository.findById(userId).orElseThrow(
                // TODO: 커스텀에러 던지기
        );

        user.setNickname(userInfo.getName());
        // TODO: 이미지처리
    }

    @Override
    @Transactional
    public void updatePassword(PasswordDto passwordDto) {
        log.info("[UserService] 비밀번호 변경 시작");

        int userId = userUtil.getCurrentUserId();

        User user = userRepository.findById(userId).orElseThrow(
                // TODO: 커스텀 에러 던지기
        );

        if (!passwordEncoder.matches(passwordDto.getOldPassword(), user.getPassword())) {
            // TODO: 커스텀 에러 던지기
        }

        user.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
    }

}
