package boot.kakaotech.communitybe.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "cloud.aws")
public class S3Property {

    private S3 s3;

    private Credentials credentials;

    private Region region;

    @Getter
    @Setter
    public static class S3 {

        private String bucket;

    }

    @Getter
    @Setter
    public static class Credentials {

        private String accessKey;
        private String secretKey;

    }

    @Getter
    @Setter
    public static class Region {

        private String name;

    }

}
