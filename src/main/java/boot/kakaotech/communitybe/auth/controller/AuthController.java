package boot.kakaotech.communitybe.auth.controller;

import boot.kakaotech.communitybe.auth.dto.SignupRequest;
import boot.kakaotech.communitybe.auth.service.AuthService;
import boot.kakaotech.communitybe.common.CommonResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<CommonResponseDto<String>> signup(
            @RequestBody SignupRequest signupRequest
            ) {
        log.info("[AuthController] 회원가입 요청 시작");

        String presignedUrl = authService.signup(signupRequest);
        CommonResponseDto<String> response = CommonResponseDto.<String>builder()
                .data(presignedUrl)
                .message("회원가입 성공")
                .build();

        log.info("[AuthController] 회원가입 요청 성공");
        return ResponseEntity.ok(response);
    }

}
