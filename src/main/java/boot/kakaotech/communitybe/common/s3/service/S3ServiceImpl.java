package boot.kakaotech.communitybe.common.s3.service;

import boot.kakaotech.communitybe.common.properties.S3Property;
import boot.kakaotech.communitybe.common.util.ThreadLocalContext;
import boot.kakaotech.communitybe.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3ServiceImpl implements S3Service {

    private final S3Presigner presigner;
    private final ThreadLocalContext context;
    private final S3Property property;

    @Override
    public String createGETPresignedUrl(String bucketName, String keyName) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(objectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
        log.info("PresignedGetObjectRequest: {}", presignedRequest.url().toString());
        log.info("HttpMethod: {}", presignedRequest.httpRequest().method());

        return presignedRequest.url().toExternalForm();
    }

    @Override
    public String createPUTPresignedUrl(String bucketName, String keyName) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
        String url = presignedRequest.url().toString();
        log.info("PresignedPutObjectRequest: {}", presignedRequest.url().toString());
        log.info("HttpMethod: {}", presignedRequest.httpRequest().method());

        return presignedRequest.url().toExternalForm();
    }

    @Override
    public String makeUserProfileKey(String email, String profileImageName) {
        return "user:" + email + ":" + UUID.randomUUID() + ":" + profileImageName;
    }

    @Override
    public String makePostKey(Integer postId, String fileName) {
        return "post:" + postId + ":" + UUID.randomUUID() + ":" + fileName;
    }

    @Override
    public String getUserProfileUrl() {
        User user = context.getCurrentUser();
        String profileImageKey = user.getProfileImageKey();

        return createGETPresignedUrl(property.getS3().getBucket(), profileImageKey);
    }

}
