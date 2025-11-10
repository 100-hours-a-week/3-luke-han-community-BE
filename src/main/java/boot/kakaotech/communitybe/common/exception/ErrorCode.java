package boot.kakaotech.communitybe.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    EXISTED_EMAIL(HttpStatus.CONFLICT)


    ;

    private final HttpStatus status;

}
