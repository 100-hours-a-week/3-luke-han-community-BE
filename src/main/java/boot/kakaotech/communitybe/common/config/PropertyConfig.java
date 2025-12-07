package boot.kakaotech.communitybe.common.config;

import boot.kakaotech.communitybe.common.properties.CorsProperty;
import boot.kakaotech.communitybe.common.properties.JwtProperty;
import boot.kakaotech.communitybe.common.properties.PrefixProperty;
import boot.kakaotech.communitybe.common.properties.S3Property;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(
        { JwtProperty.class,
        PrefixProperty.class,
        S3Property.class,
        CorsProperty.class }
)
public class PropertyConfig {
}
