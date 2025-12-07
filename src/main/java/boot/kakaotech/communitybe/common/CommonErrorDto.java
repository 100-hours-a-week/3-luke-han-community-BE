package boot.kakaotech.communitybe.common;

import boot.kakaotech.communitybe.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonErrorDto {

    private ErrorCode code;

    private String message;

}
