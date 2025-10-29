package boot.kakaotech.communitybe.user.service;

import boot.kakaotech.communitybe.common.encoder.PasswordEncoder;
import boot.kakaotech.communitybe.common.exception.BusinessException;
import boot.kakaotech.communitybe.common.exception.ErrorCode;
import boot.kakaotech.communitybe.common.s3.service.S3Service;
import boot.kakaotech.communitybe.user.dto.PasswordDto;
import boot.kakaotech.communitybe.user.dto.SimpUserInfo;
import boot.kakaotech.communitybe.user.entity.User;
import boot.kakaotech.communitybe.user.repository.UserRepository;
import boot.kakaotech.communitybe.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserUtil userUtil;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    @Transactional
    public String updateUserInfo(SimpUserInfo userInfo) throws UserPrincipalNotFoundException {
        log.info("[UserService] 유저정보 업데이트 시작");

        User user = userUtil.getCurrentUser();

        user.setNickname(userInfo.getName());
        String presignedUrl = null;

        if (userInfo.getProfileImageUrl() != null) {
            String imageKey = "user:" + user.getEmail() + ":" + UUID.randomUUID() + userInfo.getProfileImageUrl();
            presignedUrl = s3Service.createPUTPresignedUrl(bucket, imageKey);
        }

        log.info("[UserService] 유저정보 업데이트 성공");
        return presignedUrl;
    }

    @Override
    @Transactional
    public void updatePassword(PasswordDto passwordDto) throws UserPrincipalNotFoundException {
        log.info("[UserService] 비밀번호 변경 시작");

        User user = userUtil.getCurrentUser();

        if (!passwordEncoder.matches(passwordDto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_NOT_MATCHED);
        }

        user.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
    }

}
