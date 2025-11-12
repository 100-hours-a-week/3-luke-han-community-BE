package boot.kakaotech.communitybe.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CookieUtil {

    /**
     * HttpOnly 쿠키를 생성하는 메서드
     * 1. 백엔드 api 공통 uri인 "/api"를 path로 설정
     * 2. 커뮤니티이므로 링크를 통해 들어올 수 있게 same site Lax로 설정
     * 3. Javascript 접근을 막는게 보안 상 좋을 것이라 판단해서 httponly true
     * 4. 일단 배포하기 전에는 https를 사용하지 않을거라 secure false
     * 5. 만료시간 설정
     *
     * @param response
     * @param name
     * @param value
     * @param maxAge
     */
    public void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        log.info("[CookieUtil] 쿠키 생성 시작 - 이름: {}, 만료시간: {}", name, maxAge);

        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")
                .sameSite("Lax")
                .httpOnly(true)
                .secure(false)
                .maxAge(maxAge)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        log.info("[CookieUtil] 쿠키 생성 성공");
    }

    /**
     * request에서 원하는 쿠키를 반환하는 메서드
     * 전체 쿠키 조회 후 헤더에 저장된 이름으로 조회 후 반환
     *
     * @param request
     * @param name
     * @return
     */
    public Cookie getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie;
                }
            }
        }

        return null;
    }

    /**
     * response에 쿠키를 ""와 유지시간을 0으로 세팅 후 반환하는 메서드
     *
     * @param response
     * @param name
     */
    public void deleteCookie(HttpServletResponse response, String name) {
        log.info("[CookieUtil] 쿠키 삭제 시작 - 이름: {}", name);

        ResponseCookie cookie = ResponseCookie.from(name, "")
                .path("/")
                .sameSite("Lax")
                .httpOnly(true)
                .secure(false)
                .maxAge(0)
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        log.info("[CookieUtil] 쿠키 삭제 성공 - 이름: {}", name);
    }

}
