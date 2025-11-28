package boot.kakaotech.communitybe.user.service;

import boot.kakaotech.communitybe.user.dto.PasswordDto;
import boot.kakaotech.communitybe.user.dto.SimpUserInfo;

public interface UserService {

    String updateUserInfo(SimpUserInfo userInfo);

    void updatePassword(PasswordDto password);

    String getMyPresignedUrl();

}
