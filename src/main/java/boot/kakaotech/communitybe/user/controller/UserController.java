package boot.kakaotech.communitybe.user.controller;

import boot.kakaotech.communitybe.common.CommonResponseDto;
import boot.kakaotech.communitybe.common.CommonResponseMapper;
import boot.kakaotech.communitybe.user.dto.SimpUserInfo;
import boot.kakaotech.communitybe.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    private final CommonResponseMapper mapper;

    /**
     * 유저 프로필 정보 수정하는 API
     *
     * @param userInfo
     * @return
     */
    @PatchMapping
    public ResponseEntity<CommonResponseDto<String>> updateUserInfo(
            @RequestBody SimpUserInfo userInfo
            ) {
        log.info("[UserController] 유저 프로필 업데이트 시작 - userId: {}", userInfo.getId());

        String presignedUrl = userService.updateUserInfo(userInfo);
        CommonResponseDto<String> response = mapper.createResponse(presignedUrl, "유저 프로필 수정 성공");

        return ResponseEntity.ok(response);
    }

}
