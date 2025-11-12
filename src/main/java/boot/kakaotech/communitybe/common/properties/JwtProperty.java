package boot.kakaotech.communitybe.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperty {

    private String secret;
    private Name name;
    private ExpireTime time;
    private String authorization;
    private List<String> excludedPatterns;

    @Getter
    @Setter
    public static class Name {

        private String accessToken;
        private String refreshToken;

    }

    @Getter
    @Setter
    public static class ExpireTime {

        private long accessTokenExpireTime;
        private long refreshTokenExpireTime;

    }

}
