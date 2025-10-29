package boot.kakaotech.communitybe.auth.controller;

import boot.kakaotech.communitybe.auth.dto.LoginDto;
import boot.kakaotech.communitybe.auth.dto.LoginUserDto;
import boot.kakaotech.communitybe.auth.dto.SignupDto;
import boot.kakaotech.communitybe.auth.dto.ValueDto;
import boot.kakaotech.communitybe.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(
            @RequestBody SignupDto signupDto) {
        log.info("[AuthController] 회원가입 요청 시작");

        String url = authService.signup(signupDto);
        log.info("[AuthController] 회원가입 성공");

        return ResponseEntity.status(HttpStatus.CREATED).body(url);
    }

    @PostMapping("/duplications/email")
    public ResponseEntity<Void> duplicateEmail(
            @RequestBody ValueDto email
            ) {
        log.info("[AuthController] 이메일 중복확인 시작");

        boolean isExist = authService.checkEmail(email);
        log.info("[AuthController] 이메일 중복확인 성공");

        return isExist ?
                ResponseEntity.status(HttpStatus.CONFLICT).build() :
                ResponseEntity.noContent().build();
    }

    @PostMapping("/duplications/nickname")
    public ResponseEntity<Void> duplicateNickname(
            @RequestBody ValueDto nickname
    ) {
        log.info("[AuthController] 닉네임 중복확인 시작");

        boolean isExist = authService.checkNickname(nickname);
        log.info("[AuthController] 닉네임 중복확인 성공");

        return isExist ?
                ResponseEntity.status(HttpStatus.CONFLICT).build() :
                ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/signin")
    public ResponseEntity<LoginUserDto> login(HttpServletRequest request, @RequestBody LoginDto dto) {
        log.info("[AuthController] 로그인 시작 - email: {}", dto.getEmail());

        LoginUserDto res = authService.login(request, dto);
        log.info("[AuthController] 로그인 성공 - email: {}", dto.getEmail());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        log.info("[AuthController] 로그아웃 시작");

        authService.logout(request);
        log.info("[AuthController] 로그아웃 성공");
        return ResponseEntity.ok().build();
    }

}
