package boot.kakaotech.communitybe.user.service;

import boot.kakaotech.communitybe.common.encoder.PasswordEncoder;
import boot.kakaotech.communitybe.common.properties.S3Property;
import boot.kakaotech.communitybe.common.s3.service.S3Service;
import boot.kakaotech.communitybe.common.util.ThreadLocalContext;
import boot.kakaotech.communitybe.common.validation.Validator;
import boot.kakaotech.communitybe.user.dto.PasswordDto;
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
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;
    private final S3Service s3Service;
    private final S3Property s3Property;

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
        String presignedUrl = null;
        String profileImageUrl = userInfo.getProfileImageUrl();
        if (profileImageUrl != null) {
            String key = s3Service.makeUserProfileKey(user.getEmail(), profileImageUrl);
            presignedUrl = s3Service.createPUTPresignedUrl(s3Property.getS3().getBucket(), key);
        }

        userRepository.save(user);

        return presignedUrl;
    }

    /**
     * 유저의 비밀번호를 변경하는 메서드
     *
     * @param dto
     */
    @Override
    @Transactional
    public void updatePassword(PasswordDto dto) {
        log.info("[UserService] 비밀번호 변경 시작");

        User requestUser = context.getCurrentUser();
        User user = validator.validateNewPassword(requestUser, dto);

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    }

}
