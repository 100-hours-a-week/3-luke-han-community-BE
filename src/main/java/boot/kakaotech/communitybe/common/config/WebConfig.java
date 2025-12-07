package boot.kakaotech.communitybe.common.config;

import boot.kakaotech.communitybe.auth.filter.JwtVerificationFilter;
import boot.kakaotech.communitybe.common.properties.CorsProperty;
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

    private final JwtVerificationFilter jwtVerificationFilter;

    private final CorsProperty corsProperty;

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> filterRegistrationBean() {
        FilterRegistrationBean<OncePerRequestFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(jwtVerificationFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);

        return registrationBean;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(corsProperty.getFrontend())
                .allowedMethods(corsProperty.getMethods().toArray(new String[0]))
                .allowedHeaders(corsProperty.getAllowedHeaders().toArray(new String[0]))
                .exposedHeaders(corsProperty.getExposedHeaders().toArray(new String[0]))
                .allowCredentials(true)
                .maxAge(corsProperty.getAge());
    }

}
