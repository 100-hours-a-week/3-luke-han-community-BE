package boot.kakaotech.communitybe.term;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
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
