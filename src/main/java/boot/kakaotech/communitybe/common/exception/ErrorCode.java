package boot.kakaotech.communitybe.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    EXISTED_EMAIL(HttpStatus.CONFLICT, "존재하는 이메일입니다."), // 이미 존재하는 이메일

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."), // 토큰이 위변조 되었을 때
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."), // 토큰이 만료되었을 때
    UNAUTHORIZED_REQUEST(HttpStatus.UNAUTHORIZED, "인증되지 않은 요청입니다."), // 인증이 없는 요청일 때

    INVALID_USERINFO(HttpStatus.UNAUTHORIZED, "이메일 혹은 비밀번호가 틀렸습니다."), // 이메일 혹은 비밀번호가 틀렸을 때

    INVALID_FORMAT(HttpStatus.BAD_REQUEST, "형식에 맞지 않은 요청입니다.") // 요청 DTO 형식이 맞지 않을 때
    ;

    private final HttpStatus status;
    private final String message;

}
