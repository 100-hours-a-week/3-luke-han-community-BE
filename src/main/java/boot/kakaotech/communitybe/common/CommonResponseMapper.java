package boot.kakaotech.communitybe.common;

import boot.kakaotech.communitybe.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class CommonResponseMapper {

    public <T> CommonResponseDto<T> createResponse(T data, String message) {
        return CommonResponseDto.<T>builder()
                .data(data)
                .message(message).build();
    }

    public CommonResponseDto<Void> createResponse(String message) {
        return CommonResponseDto.<Void>builder()
                .message(message).build();
    }

    public CommonErrorDto createError(ErrorCode code, String message) {
        return CommonErrorDto.builder()
                .code(code)
                .message(message).build();
    }

}
