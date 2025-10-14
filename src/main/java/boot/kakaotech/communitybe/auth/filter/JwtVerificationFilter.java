package boot.kakaotech.communitybe.auth.filter;

import boot.kakaotech.communitybe.auth.dto.CustomUserDetails;
import boot.kakaotech.communitybe.auth.service.CustomUserDetailsService;
import boot.kakaotech.communitybe.auth.service.JwtService;
import boot.kakaotech.communitybe.util.CookieUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.PathContainer;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtVerificationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    private final PathPatternParser patternParser = new PathPatternParser();
    private final CookieUtil cookieUtil;

    private final List<PathPattern> excludedUrls = Arrays.asList(
            "/api/auth/**"
    ).stream().map(patternParser::parse).toList();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("[JwtVerificationFilter] 토큰 검증 시작");

        String authHeader = request.getHeader(AUTHORIZATION);
        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }

        if (accessToken != null) {
            try {
                processAccessToken(accessToken, response);
            } catch (ExpiredJwtException e) {
                log.info("[JwtVerificationFilter] Access token 만료");

                String refreshToken = cookieUtil.getCookie(request, "refresh_token").getValue();
                handleExpiredAccessToken(response, refreshToken);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        log.info("[JwtVerificationFilter] 토큰 검증 성공");
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        PathContainer container = PathContainer.parsePath(path);

        return excludedUrls.stream().anyMatch(p -> p.matches(container));
    }

    /**
     * SecurityContextHolder에 저장된 인증 정보가 없다면 새로 저장하는 메서드
     *
     * @param accessToken
     * @param response
     */
    private void processAccessToken(String accessToken, HttpServletResponse response) {
        String email = jwtService.getEmailFromToken(accessToken);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails =  userDetailsService.loadUserByUsername(email);

            if (jwtService.isValidAccessToken(accessToken, userDetails)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
    }

    private void handleExpiredAccessToken(HttpServletResponse response, String refreshToken) {
        if (refreshToken == null) {
            return;
        }

        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(
                jwtService.getEmailFromToken(refreshToken)
        );

        Map<String, String> tokens = jwtService.rotateTokens(refreshToken, userDetails.getUser());
        if (tokens != null) {
            cookieUtil.addCookie(response, "refresh_token", tokens.get("refresh_token"), (int) (jwtService.getRefreshTokenExpireTime() / 1000));

            response.setHeader(AUTHORIZATION, "Bearer " + tokens.get("access_token"));
            processAccessToken(tokens.get("access_token"), response);
            return;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

}
