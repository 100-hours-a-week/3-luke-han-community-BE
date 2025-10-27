package boot.kakaotech.communitybe.common.s3.service;

public interface S3Service {

    String createGETPresignedUrl(String bucketName, String keyName);

    String createPUTPresignedUrl(String bucketName, String keyName);

}
