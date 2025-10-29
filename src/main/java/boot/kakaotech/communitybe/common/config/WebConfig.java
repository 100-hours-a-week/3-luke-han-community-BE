package boot.kakaotech.communitybe.common.config;

import boot.kakaotech.communitybe.auth.filter.CookieFilter;
import boot.kakaotech.communitybe.auth.filter.SessionFilter;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final SessionFilter sessionFilter;

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> filterRegistrationBean() {
        FilterRegistrationBean<OncePerRequestFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(sessionFilter);
        filterRegistrationBean.setOrder(1);
        filterRegistrationBean.addUrlPatterns("/*");

        return filterRegistrationBean;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Cookie", "Set-Cookie")
                .exposedHeaders("Content-Type", "Set-Cookie")
                .allowCredentials(true)
                .maxAge(3600);
    }

}
