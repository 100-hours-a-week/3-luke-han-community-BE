package boot.kakaotech.communitybe.user.controller;

import boot.kakaotech.communitybe.common.CommonResponseDto;
import boot.kakaotech.communitybe.common.CommonResponseMapper;
import boot.kakaotech.communitybe.user.dto.PasswordDto;
import boot.kakaotech.communitybe.user.dto.SimpUserInfo;
import boot.kakaotech.communitybe.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 유저의 비밀번호를 변경하는 API
     *
     * @param password
     * @return
     */
    @PatchMapping("/password")
    public ResponseEntity<CommonResponseDto<Void>> updatePassword(
            @RequestBody PasswordDto password
            ) {
        log.info("[UserController] 비밀번호 변경 시작 - userId: {}", password.getUserId());

        userService.updatePassword(password);
        CommonResponseDto<Void> response = mapper.createResponse("비밀번호 변경 성공");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/images")
    public ResponseEntity<CommonResponseDto<String>> getMyProfileImageUrl() {
        log.info("[UserController] 프로필 이미지 조회 시작");

        String url = userService.getMyPresignedUrl();
        CommonResponseDto<String> response = mapper.createResponse(url, "presigned url 생성 성공");

        return ResponseEntity.ok(response);
    }

}
