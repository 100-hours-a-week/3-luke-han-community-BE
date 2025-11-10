package boot.kakaotech.communitybe.auth.controller;

import boot.kakaotech.communitybe.auth.dto.LoginRequest;
import boot.kakaotech.communitybe.auth.dto.LoginResponse;
import boot.kakaotech.communitybe.auth.dto.SignupRequest;
import boot.kakaotech.communitybe.auth.service.AuthService;
import boot.kakaotech.communitybe.common.CommonResponseDto;
import jakarta.servlet.http.HttpServletResponse;
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

    /**
     * 회원가입 API
     * 1. AuthService로 회원가입 및 PUT용 presigned url 생성
     * 2. 공통응답DTO에 presigned url 넣어서 ResponseEntity에 담아 반환
     *
     * @param signupRequest
     * @return
     */
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

    /**
     * 로그인 API
     * 1. AuthService로 로그인 시도
     * 2. 성공 시 LoginResponse 생성
     * 3. 공통응답DTO에 LoginResponse 담아서 반환
     *
     * @param response
     * @param loginRequest
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<CommonResponseDto<LoginResponse>> login(
            HttpServletResponse response,
            @RequestBody LoginRequest loginRequest
    ) {
        log.info("[AuthController] 로그인 시작 - email: {}", loginRequest.getEmail());

        LoginResponse loginResponse = authService.login(response, loginRequest);

        CommonResponseDto<LoginResponse> res = CommonResponseDto.<LoginResponse>builder()
                .data(loginResponse)
                .message("로그인 성공")
                .build();

        log.info("[AuthController] 로그인 성공 - email: {}", loginRequest.getEmail());
        return ResponseEntity.ok(res);
    }

}
