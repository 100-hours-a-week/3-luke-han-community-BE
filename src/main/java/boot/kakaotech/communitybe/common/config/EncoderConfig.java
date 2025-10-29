package boot.kakaotech.communitybe.common.config;

import boot.kakaotech.communitybe.common.encoder.PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() { return new PasswordEncoder(); }

}
