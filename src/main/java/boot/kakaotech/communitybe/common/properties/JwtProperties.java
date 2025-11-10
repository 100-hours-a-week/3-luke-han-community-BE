package boot.kakaotech.communitybe.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private Name name;
    private ExpireTime time;
    private String authorization;

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
