package boot.kakaotech.communitybe.term;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TermController {

    @GetMapping("/terms")
    public String getTerms() {
        return "terms";
    }

    @GetMapping("/privacy")
    public String getPrivacy() {
        return "privacy";
    }

}
