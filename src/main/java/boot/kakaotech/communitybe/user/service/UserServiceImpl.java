package boot.kakaotech.communitybe.user.service;

import boot.kakaotech.communitybe.common.util.ThreadLocalContext;
import boot.kakaotech.communitybe.common.validation.Validator;
import boot.kakaotech.communitybe.user.dto.SimpUserInfo;
import boot.kakaotech.communitybe.user.entity.User;
import boot.kakaotech.communitybe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final ThreadLocalContext context;
    private final Validator validator;

    /**
     * 유저 프로필 정보 업데이트하는 메서드
     *
     * @param userInfo
     * @return
     */
    @Override
    @Transactional
    public String updateUserInfo(SimpUserInfo userInfo) {
        log.info("[UserService] 유저 프로필 수정 시작");

        User requestUser = context.getCurrentUser();
        User user = validator.validateUserInfo(requestUser, userInfo.getId());

        user.setNickname(userInfo.getName());
        String presignedUrl = ""; // TODO: 프로필 사진 바뀌었다면 presigned url 발급로직 추가

        userRepository.save(user);

        return presignedUrl;
    }

}
