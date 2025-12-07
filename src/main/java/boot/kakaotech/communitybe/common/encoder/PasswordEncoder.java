package boot.kakaotech.communitybe.common.encoder;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
public class PasswordEncoder {

    private final String ALGORITHM = "PBKDF2WithHmacSHA256"; // 해싱 알고리즘
    private final int KEY_LENGTH = 256; // 출력 해시 길이(bit단위)
    private final int SALT_LENGTH = 32;
    private final int ITERATION_COUNT = 600_000; //
    private final int MIN_PASSWORD_LENGTH = 8;

    /**
     * SHA256 알고리즘으로 인코딩하는 메서드
     * salt 값과 비밀번호로 암호화한 byte[]를 base64로 인코딩하여 반환
     *
     * @param rawPassword
     * @return
     */
    public String encode(String rawPassword) {
        if (rawPassword == null || !isEnoughLength(rawPassword)) {
            throw new IllegalArgumentException("rawPassword length should be at least " + MIN_PASSWORD_LENGTH);
        }

        var salt = createSalt();
        var hash = hashPassword(rawPassword, salt);
        byte[] saltedHash = new byte[salt.length + hash.length];
        System.arraycopy(salt, 0, saltedHash, 0, salt.length);
        System.arraycopy(hash, 0, saltedHash, salt.length, hash.length);

        return Base64.getEncoder().encodeToString(saltedHash);
    }

    /**
     * 비밀번호 인코딩 후 인코딩된 비밀번호와 맞는지 확인하는 메서드
     * 기존의 인코딩 된 비밀번호를 base64 디코딩 후 rawPassword를 새로 인코딩 해
     *
     * @param rawPassword
     * @param encodedPassword
     * @return
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || !isEnoughLength(rawPassword)) {
            throw new IllegalArgumentException("rawPassword length should be at least " + MIN_PASSWORD_LENGTH);
        }

        byte[] hash;
        try {
            hash = Base64.getDecoder().decode(encodedPassword);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid base64 encoded password", e);
        }

        var salt = new byte[SALT_LENGTH];
        var storedHash = new byte[hash.length - SALT_LENGTH];
        System.arraycopy(hash, 0, salt, 0, SALT_LENGTH); // 기존 salt값 복사
        System.arraycopy(hash, SALT_LENGTH, storedHash, 0, storedHash.length);
        var newHash = hashPassword(rawPassword, salt);

        return Arrays.equals(storedHash, newHash);
    }

    /**
     * salt 값 무작위로 생성하는 메서드
     * OWASP라는 곳에서 암호학적으로 안전한 해시함수로 SecureRandom을 지목했다고 하여 사용
     *
     * @return
     */
    private byte[] createSalt() {
        byte[] salt = new byte[SALT_LENGTH];

        var random = new SecureRandom();
        random.nextBytes(salt);

        return salt;
    }

    /**
     * 전달받은 비밀번호 길이 검증하는 메서드
     * NIST SP800-63B라는 규칙에 따라 8자 이상이어야 한다고 함
     * 위 내용을 자세하게 보진 않았음
     *
     * @param password
     * @return
     */
    private boolean isEnoughLength(String password) {
        return password.length() >= MIN_PASSWORD_LENGTH;
    }

    /**
     * 해시할 비밀번호와 salt를 전달받아 인코딩하는 메서드
     * byte[]가 반환되어 base64로 인코딩하지 않으면 문자열로 변환 시 문제 발생 가능하기 때문에 인코딩 필수
     *
     * @param rawPassword
     * @param salt
     * @return
     */
    private byte[] hashPassword(String rawPassword, byte[] salt) {
        var keySpec = new PBEKeySpec(
                rawPassword.toCharArray(),
                salt,
                ITERATION_COUNT,
                KEY_LENGTH
        );

        try {
            var secretKeyFactory = SecretKeyFactory.getInstance(ALGORITHM);

            return secretKeyFactory.generateSecret(keySpec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

}
