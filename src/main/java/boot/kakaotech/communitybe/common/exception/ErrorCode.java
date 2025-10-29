package boot.kakaotech.communitybe.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    INVALID_EMAIL(HttpStatus.UNAUTHORIZED),
    PASSWORD_NOT_MATCHED(HttpStatus.UNAUTHORIZED),
    INVALID_FORMAT(HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED),
    REQUEST_FROM_OTHERS(HttpStatus.FORBIDDEN),

    ILLEGAL_ARGUMENT(HttpStatus.BAD_REQUEST);

    private final HttpStatus status;

}
