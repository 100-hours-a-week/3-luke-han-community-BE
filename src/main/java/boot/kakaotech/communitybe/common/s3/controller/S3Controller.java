package boot.kakaotech.communitybe.common.s3.controller;

import boot.kakaotech.communitybe.common.CommonResponseDto;
import boot.kakaotech.communitybe.common.CommonResponseMapper;
import boot.kakaotech.communitybe.common.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/s3")
@RequiredArgsConstructor
@Slf4j
public class S3Controller {

    private final S3Service s3Service;
    private final CommonResponseMapper mapper;

    @GetMapping
    public ResponseEntity<CommonResponseDto<String>> getPresignedUrl() {
        log.info("[S3Controller] GET용 presigned url 발급 시작");

        String presignedUrl = s3Service.getUserProfileUrl();
        CommonResponseDto<String> response = mapper.createResponse(presignedUrl, "유저 프로필 이미지 반환 성공");
        return ResponseEntity.ok(response);
    }

}
