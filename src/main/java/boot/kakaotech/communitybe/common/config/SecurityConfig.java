package boot.kakaotech.communitybe.common.config;

import boot.kakaotech.communitybe.auth.filter.JwtVerificationFilter;
import boot.kakaotech.communitybe.auth.handler.CustomLogoutHandler;
import boot.kakaotech.communitybe.auth.handler.LoginFailureHandler;
import boot.kakaotech.communitybe.auth.handler.LoginSuccessHandler;
import boot.kakaotech.communitybe.auth.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final CustomLogoutHandler logoutHandler;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Bean
    UrlBasedCorsConfigurationSource corsConfigurationSource() {
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@ frontendUrl: " + frontendUrl);
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(frontendUrl));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Set-Cookie",
                "Access-Control-Allow-Credentials",
                "Access-Control-Allow-Origin"
        ));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Cookie",
                "Access-Control-Allow-Credentials",
                "Access-Control-Allow-Origin"
        ));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider(CustomUserDetailsService customUserDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtVerificationFilter jwtVerificationFilter) throws Exception {
        log.info("[SecurityConfig] 보안 필터 구성 시작");
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(requests -> {
                    requests
                                    .requestMatchers(
                                            "/api/**"
                                    ).permitAll()
                                    .anyRequest().authenticated();
                    log.info("[SecurityConfig] URL 인가 구성 완료");
                })
                .addFilterBefore(jwtVerificationFilter, UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(authenticationProvider(userDetailsService, passwordEncoder))
                .formLogin(form -> {
                    form
                            .loginProcessingUrl("/api/auth/signin")
                            .successHandler(loginSuccessHandler)
                            .failureHandler(loginFailureHandler);
                    ;
                })
                .logout(logout -> {
                    logout
                            .logoutUrl("/api/auth/logout")
                            .addLogoutHandler(logoutHandler)
                            .addLogoutHandler((request, response, authentication) -> {
                                response.setStatus(HttpServletResponse.SC_OK);
                            })
                            .permitAll();
                })
                .sessionManagement(session -> {
                    log.info("[SecurityConfig] 세션 관리 정책: STATELESS");
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                });

        return http.build();
    }

}
