package boot.kakaotech.communitybe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CommunityBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunityBeApplication.class, args);
    }

}
