package boot.kakaotech.communitybe.auth.filter;

import boot.kakaotech.communitybe.auth.jwt.JwtProvider;
import boot.kakaotech.communitybe.auth.jwt.JwtVerifier;
import boot.kakaotech.communitybe.user.entity.User;
import boot.kakaotech.communitybe.user.repository.UserRepository;
import boot.kakaotech.communitybe.util.CookieUtil;
import boot.kakaotech.communitybe.util.ThreadLocalContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtVerificationFilter extends OncePerRequestFilter {

    private final ThreadLocalContext context;
    private final JwtVerifier verifier;
    private final JwtProvider provider;
    private final CookieUtil cookieUtil;

    private final UserRepository userRepository;

    private final PathPatternParser patternParser = new PathPatternParser();
    private final List<PathPattern> excludedUrls = Arrays.asList(
            "/api/auth/**",
            "/terms",
            "/privacy"
    ).stream().map(patternParser::parse).toList();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("[JwtVerificationFilter] 인증필터 시작 - method: {}", request.getMethod());
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@ preflight 여기까지 넘어옴");

        // access token 헤더에서 추출 및 validatoin
        String accessToken = getAccessTokenFromRequest(request);
        if (accessToken == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // access token에 담긴 userId로 user 조회
        int userId = verifier.extractUserIdFromToken(accessToken);
        User user = userRepository.findById(userId).orElseThrow();

        if (verifier.isValidToken(accessToken, user)) {
            // access token이 유효하면 ThreadLocal에 user 세팅 후 다음 필터로
            context.set(user);
            filterChain.doFilter(request, response);
            context.clear();
            return;
        } else {
            // access token이 유효하지 않으면 refresh token도 조회
            Cookie cookie = cookieUtil.getCookie(request, "refresh_token");
            if (cookie != null || cookie.getValue() == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String refreshToken = cookie.getValue();
            // refresh token이 유효하지 않으면 종료
            if (!verifier.isValidToken(refreshToken, user)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // refresh token rotate 진행
            Map<String, String> tokens = verifier.rotateToken(user);
            String newAccessToken = tokens.get("accessToken");
            String newRefreshToken = tokens.get("refreshToken");
            response.setHeader("Authorization", "Bearer " + newAccessToken);
            cookieUtil.addCookie(response, "refresh_token", newRefreshToken, (int) provider.getRefreshTokenExpireTime() / 1000);
            // ThreadLocal에 유저 세팅
            context.set(user);
        }

        filterChain.doFilter(request, response);
        // dispatcher servlet에 들렀다 나온 후 ThreadLocal 초기화
        context.clear();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        PathContainer container = PathContainer.parsePath(uri);
        return excludedUrls.stream().anyMatch(p -> p.matches(container));
    }

    /**
     * request Authorization header에서 access token 가져와서 반환하는 메서드
     * access token이 null이거나 Bearer로 시작하지 않으면 throw error
     *
     * @param request
     * @return
     */
    private String getAccessTokenFromRequest(HttpServletRequest request) {
        String accessToken = request.getHeader("Authorization");
        return accessToken == null ? null : accessToken.substring(7);
    }
}
