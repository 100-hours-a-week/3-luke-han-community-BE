package boot.kakaotech.communitybe.common.encoder;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class PasswordEncoder {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int KEY_LENGTH = 256;
    private static final int ITERATION_COUNT = 600_000;
    private static final int SALT_LENGTH = 16;
    private static final int MIN_PASSWORD_LENGTH = 8;

    public String encode(String rawPassword) {
        if (rawPassword == null || isPasswordEnoughLength(rawPassword)) {
            throw new IllegalArgumentException("Invalid password");
        }

        var salt = createSalt();
        var hash = hashPassword(rawPassword, salt);

        var encodedPassword = new byte[SALT_LENGTH + hash.length];
        System.arraycopy(salt, 0, encodedPassword, 0, SALT_LENGTH);
        System.arraycopy(hash, 0, encodedPassword, SALT_LENGTH, hash.length);

        return Base64.getEncoder().encodeToString(encodedPassword);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || isPasswordEnoughLength(rawPassword)) {
            throw new IllegalArgumentException("Invalid password");
        }

        byte[] hash;
        try {
            hash = Base64.getDecoder().decode(encodedPassword);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid encoded password");
        }

        var salt = new byte[SALT_LENGTH];
        var storedHash = new byte[hash.length - SALT_LENGTH];
        System.arraycopy(hash, 0, salt, 0, SALT_LENGTH);
        System.arraycopy(hash, SALT_LENGTH, storedHash, 0, storedHash.length);
        var newHash = hashPassword(rawPassword, salt);

        return Arrays.equals(storedHash, newHash);
    }

    private boolean isPasswordEnoughLength(String rawPassword) {
        return rawPassword.length() >= MIN_PASSWORD_LENGTH;
    }

    private byte[] createSalt() {
        var salt = new byte[SALT_LENGTH];

        var random = new SecureRandom();
        random.nextBytes(salt);

        return salt;
    }

    private byte[] hashPassword(String password, byte[] salt) {
        var keySpec = new PBEKeySpec(
                password.toCharArray(),
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
