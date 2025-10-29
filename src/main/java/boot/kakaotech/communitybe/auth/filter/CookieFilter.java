package boot.kakaotech.communitybe.auth.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CookieFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        filterChain.doFilter(httpServletRequest, httpServletResponse);
        Collection<String> cookies = httpServletResponse.getHeaders("Set-Cookie");
        for (String cookie : cookies) {
            if (cookie.startsWith("JSESSIONID=")) {
                System.out.println("@@@@@@@@@@@@@@@@@@@@@ cookie: " + cookie);
                cookie += "; SameSite=None; Secure";
                System.out.println("@@@@@@@@@@@@@@@@@@@@@ cookie: " + cookie);
                httpServletResponse.setHeader("Set-Cookie", cookie);
                System.out.println("@@@@@@@@@@@@@@@@@@@@@ cookies: " + httpServletResponse.getHeader("Set-Cookie"));
            }
        }

//        ServletContext servletContext = httpServletResponse.getServletContext();
////        servletContext.getSessionCookieConfig().setAttribute("SameSite", "None");
//        servletContext.getSessionCookieConfig().setSecure(true);

        System.out.println("@@@@@@@@@@@@@@@@@@@@@ cookies: " + httpServletResponse.getHeader("Set-Cookie"));
    }

}
