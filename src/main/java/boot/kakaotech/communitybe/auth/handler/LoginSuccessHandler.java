package boot.kakaotech.communitybe.auth.handler;

import boot.kakaotech.communitybe.auth.dto.CustomUserDetails;
import boot.kakaotech.communitybe.auth.dto.LoginUserDto;
import boot.kakaotech.communitybe.auth.service.JwtService;
import boot.kakaotech.communitybe.common.s3.service.S3Service;
import boot.kakaotech.communitybe.user.entity.User;
import boot.kakaotech.communitybe.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final S3Service s3Service;

    private final CookieUtil cookieUtil;
    private final ObjectMapper objectMapper;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("[LoginSuccessHandler] 로그인 성공");
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        cookieUtil.addCookie(response, "refresh_token", refreshToken, (int) jwtService.getRefreshTokenExpireTime() / 1000);

        response.setHeader("Authorization", "Bearer " + accessToken);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(
                LoginUserDto.builder()
                        .userId(user.getId())
                        .profileImageUrl(s3Service.createGETPresignedUrl(bucket, user.getProfileImageUrl()))
                        .build())
        );
    }
}
