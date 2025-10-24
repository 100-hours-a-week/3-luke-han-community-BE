package boot.kakaotech.communitybe.term;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class TermController {

    @GetMapping("/terms")
    public String getTerms() {
        return "terms";
    }

}
