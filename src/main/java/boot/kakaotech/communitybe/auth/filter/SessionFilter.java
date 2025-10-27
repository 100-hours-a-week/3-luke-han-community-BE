package boot.kakaotech.communitybe.auth.filter;

import boot.kakaotech.communitybe.common.exception.BusinessException;
import boot.kakaotech.communitybe.common.exception.ErrorCode;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionFilter implements Filter {

    private final PathPatternParser patternParser = new PathPatternParser();
    private final List<PathPattern> excludedUris = Arrays.asList(
            "/api/auth/**",
            "/terms",
            "/privacy"
    ).stream().map(patternParser::parse).toList();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        log.info("[SessionFilter] 요청 세션 확인 시작");

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);
        String uri = httpRequest.getRequestURI();

        // 인증 확인 제외 uri이거나 session이 있고 user 객체가 등록이 되어있으면 doFilter
        if (isExcludedUrl(uri) || (session != null && session.getAttribute("user") != null)) {
            log.info("[SessionFilter] 인증정보 확인 완료");
            filterChain.doFilter(httpRequest, httpResponse);
            return;
        }

        // 아니면 401
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }

    /**
     * 인증 확인 제외 uri인지 확인하는 메서드
     *
     * @param uri
     * @return
     */
    private boolean isExcludedUrl(String uri) {
        PathContainer container = PathContainer.parsePath(uri);
        return excludedUris.stream().anyMatch(p -> p.matches(container));
    }

}
