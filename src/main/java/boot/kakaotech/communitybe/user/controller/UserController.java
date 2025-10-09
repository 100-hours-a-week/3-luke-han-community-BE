package boot.kakaotech.communitybe.user.controller;

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

    @PatchMapping
    public ResponseEntity<Void> updateUserInfo(@RequestBody SimpUserInfo userInfo) {
        log.info("[UserController] 회원정보 수정 시작");

        userService.updateUserInfo(userInfo);
        log.info("[UserController] 회원정보 수정 성공");

        return ResponseEntity.ok().build();
    }

}
