package boot.kakaotech.communitybe.auth.service;

import boot.kakaotech.communitybe.user.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

public interface JwtService {

    boolean isValidAccessToken(String accessToken, UserDetails user);

    boolean isValidRefreshToken(String refreshToken, User user);

    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    String getEmailFromToken(String token);

    long getRefreshTokenExpireTime();

    Map<String, String> rotateTokens(String refreshToken, User user);

    void invalidateRefreshToken(String email);

}
