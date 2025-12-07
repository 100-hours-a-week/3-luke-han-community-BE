package boot.kakaotech.communitybe.common.config;

import boot.kakaotech.communitybe.common.properties.S3Property;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;


@Configuration
@RequiredArgsConstructor
public class S3Config {

    private final S3Property property;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(property.getRegion().getName()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        property.getCredentials().getAccessKey(),
                                        property.getCredentials().getSecretKey()
                                )
                        )
                )
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(property.getRegion().getName()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        property.getCredentials().getAccessKey(),
                                        property.getCredentials().getSecretKey()
                                )
                        )
                )
                .build();
    }

}
