package boot.kakaotech.communitybe.auth.filter;

import boot.kakaotech.communitybe.auth.jwt.JwtProvider;
import boot.kakaotech.communitybe.auth.jwt.JwtVerifier;
import boot.kakaotech.communitybe.auth.jwt.TokenName;
import boot.kakaotech.communitybe.common.CommonErrorDto;
import boot.kakaotech.communitybe.common.CommonResponseMapper;
import boot.kakaotech.communitybe.common.exception.BusinessException;
import boot.kakaotech.communitybe.common.exception.ErrorCode;
import boot.kakaotech.communitybe.common.properties.JwtProperty;
import boot.kakaotech.communitybe.user.entity.User;
import boot.kakaotech.communitybe.user.repository.UserRepository;
import boot.kakaotech.communitybe.common.util.CookieUtil;
import boot.kakaotech.communitybe.common.util.ThreadLocalContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtVerificationFilter extends OncePerRequestFilter {

    private final JwtProperty jwtProperty;

    private final JwtVerifier verifier;
    private final PathPatternParser parser;
    private final CookieUtil cookieUtil;
    private final ThreadLocalContext context;

    private final UserRepository userRepository;

    private final ObjectMapper objectMapper;
    private final CommonResponseMapper responseMapper;

    private List<PathPattern> excludedPatterns;
    private final JwtProvider jwtProvider;

    @PostConstruct
    private void init() {
        this.excludedPatterns = jwtProperty.getExcludedPatterns()
                .stream()
                .map(parser::parse)
                .toList();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("[JwtVerificationFilter] 토큰 검증 시작");
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = getAuthHeader(request);
        if (accessToken == null) {
            setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.INVALID_TOKEN, "잘못된 access token입니다.");
            return;
        }

        try {
            processAccessToken(accessToken, response);
        } catch (ExpiredJwtException e) {
            log.info("[JwtVerificationFilter] Access Token 만료");

            String refreshToken = cookieUtil
                    .getCookie(request, jwtProperty.getName()
                    .getRefreshToken())
                    .getValue();

            if (refreshToken == null || !handleExpiredAccessToken(response, refreshToken)) {
                setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.INVALID_TOKEN, "로그인이 필요한 사용자입니다.");
                return;
            }

            String newAccessToken = jwtProvider.generateToken(context.getCurrentUser(), TokenName.ACCESS_TOKEN);
            response.setHeader(jwtProperty.getAuthorization(), "Bearer " + newAccessToken);
            cookieUtil.addCookie(response, jwtProperty.getName().getRefreshToken(), refreshToken, (int) jwtProperty.getExpireTime().getRefreshTokenExpireTime() / 1000);
        }

        filterChain.doFilter(request, response);
        context.clear();
    }

    private boolean handleExpiredAccessToken(HttpServletResponse response, String refreshToken) throws IOException {
        int userId = verifier.extractUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return false;
        }

        try {
            verifier.isValidToken(refreshToken, user);
        } catch (BusinessException e) {
            return false;
        }

        context.set(user);
        return true;
    }

    /**
     * Access Token 검증하고, 검증되었으면 thread local에 user 세팅하는 메서드
     *
     * @param accessToken
     * @param response
     * @throws IOException
     */
    private void processAccessToken(String accessToken, HttpServletResponse response) throws IOException {
        int userId = verifier.extractUserIdFromToken(accessToken);
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.INVALID_TOKEN, "잘못된 access token입니다.");
            return;
        }

        try {
            verifier.isValidToken(accessToken, user);
        } catch (BusinessException e) {
            setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.INVALID_TOKEN, "잘못된 access token입니다.");
        }

        context.set(user);
    }

    /**
     * request에서 access token 추출하여 반환하는 메서드
     * 만약 토큰이 없거나 Bearer 로 시작하지 않으면 null 반환
     *
     * @param request
     * @return
     */
    private String getAuthHeader(HttpServletRequest request) {
        String authHeader = request.getHeader(jwtProperty.getAuthorization());
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        return authHeader.substring(7);
    }

    /**
     * CommonErrorDto response에 넣고 반환하는 메서드
     *
     * @param response
     * @param status
     * @param code
     * @param message
     * @throws IOException
     */
    private void setErrorResponse(HttpServletResponse response, int status, ErrorCode code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        CommonErrorDto dto = responseMapper.createError(code, message);

        response.getWriter().write(objectMapper.writeValueAsString(dto));
    }

}
