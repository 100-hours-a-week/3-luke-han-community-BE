package boot.kakaotech.communitybe.common.handler;

import boot.kakaotech.communitybe.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusinessException(BusinessException e) {
        log.error("[BusinessException] {}", e.getMessage());
        String errorMsg = e.getMessage();

        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(new ErrorResponse(
                        e.getErrorCode().getStatus().value(),
                        errorMsg,
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.error("[ValidationException] {}", e.getMessage());
        String errorMsg = e.getBindingResult().getFieldError().getDefaultMessage();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        400,
                        errorMsg,
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(UserPrincipalNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserPrincipalNotFoundException(UserPrincipalNotFoundException e) {
        log.error("[UserPrincipalNotFoundException] {}", e.getMessage());
        String errorMsg = e.getMessage();

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(
                        401,
                        errorMsg,
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("[Exception] {}", e.getMessage());
        String errorMsg = e.getMessage();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        500,
                        errorMsg,
                        LocalDateTime.now()
                ));
    }

}

record ErrorResponse(
    int status,
    String message,
    LocalDateTime timestamp
) {}