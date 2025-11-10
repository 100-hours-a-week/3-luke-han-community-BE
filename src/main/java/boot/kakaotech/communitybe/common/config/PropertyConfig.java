package boot.kakaotech.communitybe.common.config;

import boot.kakaotech.communitybe.common.properties.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(
        JwtProperties.class
)
public class PropertyConfig {
}
