package boot.kakaotech.communitybe.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "prefix")
public class PrefixProperty {

    private String viewCount;

}
