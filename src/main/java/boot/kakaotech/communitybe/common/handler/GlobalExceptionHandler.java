package boot.kakaotech.communitybe.common.handler;

import boot.kakaotech.communitybe.common.CommonErrorDto;
import boot.kakaotech.communitybe.common.CommonResponseMapper;
import boot.kakaotech.communitybe.common.exception.BusinessException;
import boot.kakaotech.communitybe.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final CommonResponseMapper mapper;

    /**
     * Business Exception으로 감싼 에러 처리하는 핸들러
     * 공통적으로 CommonErrorDto 반환
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CommonErrorDto> handleBusinessException(BusinessException ex) {
        log.error(ex.getMessage());

        CommonErrorDto response = mapper.createError(ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(ex.getErrorCode().getStatus()).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CommonErrorDto> handleRuntimeException(RuntimeException ex) {
        log.error(ex.getMessage());

        CommonErrorDto response = mapper.createError(ErrorCode.ILLEGAL_ARGUMENT, ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonErrorDto> handleException(Exception ex) {
        log.error(ex.getMessage());

        CommonErrorDto response = mapper.createError(ErrorCode.ILLEGAL_ARGUMENT, ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
