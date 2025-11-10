package boot.kakaotech.communitybe.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    EXISTED_EMAIL(HttpStatus.CONFLICT), // 이미 존재하는 이메일

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED), // 토큰이 위변조 되었을 때
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED), // 토큰이 만료되었을 때

    INVALID_USERINFO(HttpStatus.UNAUTHORIZED), // 이메일 혹은 비밀번호가 틀렸을 때

    INVALID_FORMAT(HttpStatus.BAD_REQUEST)
    ;

    private final HttpStatus status;

}
