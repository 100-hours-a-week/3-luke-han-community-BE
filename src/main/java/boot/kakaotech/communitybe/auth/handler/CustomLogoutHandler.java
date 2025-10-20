package boot.kakaotech.communitybe.auth.handler;

import boot.kakaotech.communitybe.auth.dto.CustomUserDetails;
import boot.kakaotech.communitybe.auth.service.JwtService;
import boot.kakaotech.communitybe.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomLogoutHandler implements LogoutHandler {

    private static final String AUTHORIZATION = "Authorization";

    private final JwtService jwtService;
    private final CookieUtil cookieUtil;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        log.info("[LogoutHandler] 로그아웃 처리 시작");
        try {
            // access token은 프론트에서 삭제하고 refresh token만 보내기
            Cookie refreshCookie = cookieUtil.getCookie(request, "refresh_token");
            String refreshToken = null;
            if (refreshCookie != null) {
                refreshToken = refreshCookie.getValue();
                cookieUtil.deleteCookie(response, "refresh_token");

                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                jwtService.invalidateRefreshToken(userDetails.getUsername());

                response.setStatus(HttpServletResponse.SC_OK);
            }
        } catch (Exception e) {
            log.error("[LogoutHandler] 에러가 발생하였습니다.", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
