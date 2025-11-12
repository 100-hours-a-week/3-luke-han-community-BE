package boot.kakaotech.communitybe.common.config;

import boot.kakaotech.communitybe.common.properties.JwtProperty;
import boot.kakaotech.communitybe.common.properties.PrefixProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(
        {JwtProperty.class,
        PrefixProperty.class}
)
public class PropertyConfig {
}
