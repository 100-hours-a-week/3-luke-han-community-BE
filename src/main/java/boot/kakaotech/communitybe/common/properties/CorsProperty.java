package boot.kakaotech.communitybe.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "cors")
public class CorsProperty {

    private String frontend;
    private List<String> methods;
    private List<String> allowedHeaders;
    private List<String> exposedHeaders;
    private long age;

}
