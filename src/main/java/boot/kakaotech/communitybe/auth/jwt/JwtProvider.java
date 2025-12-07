package boot.kakaotech.communitybe.auth.jwt;

import boot.kakaotech.communitybe.auth.dto.Token;
import boot.kakaotech.communitybe.common.exception.BusinessException;
import boot.kakaotech.communitybe.common.exception.ErrorCode;
import boot.kakaotech.communitybe.common.properties.JwtProperty;
import boot.kakaotech.communitybe.common.util.KeyValueStore;
import boot.kakaotech.communitybe.user.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtProvider {

    private final JwtProperty jwtProperty;
    private final JwtVerifier verifier;
    private final KeyValueStore kvStore;

    /**
     * 토큰 생성하는 메서드, Enum으로 정의하여 access token인지 refresh token인지 판별하여 생성
     * 굳이 클레임에 차별화를 둘 필요는 없을 것 같아서 하나의 메서드로 처리
     *
     * @param user
     * @param name
     * @return
     */
    public String generateToken(User user, TokenName name) {
        long expirationTime = name.equals(
                TokenName.ACCESS_TOKEN) ?
                jwtProperty.getExpireTime().getAccessTokenExpireTime() :
                jwtProperty.getExpireTime().getRefreshTokenExpireTime();

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("nickname", user.getNickname())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigninKey())
                .compact();
    }

    /**
     * 리프레시토큰 생성 후 인메모리에 저장 후 반환하는 메서드
     *
     * @param user
     * @return
     */
    public String generateRefreshToken(User user) {
        String token = generateToken(user, TokenName.REFRESH_TOKEN);
        kvStore.saveRefreshToken(user.getId(), token);
        return token;
    }

    public Token rotateRefreshToken(String refreshToken, User user) {
        verifier.isValidToken(refreshToken, user);

        int userId = user.getId();
        String stored = kvStore.getRefreshToken(userId);

        if (stored == null || !stored.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        kvStore.deleteRefreshToken(userId);

        String newAccessToken = generateToken(user, TokenName.ACCESS_TOKEN);
        String newRefreshToken = generateToken(user, TokenName.REFRESH_TOKEN);

        kvStore.saveRefreshToken(userId, newRefreshToken);

        return new Token(newAccessToken, newRefreshToken);
    }

    /**
     * JWT 사인용 키값 반환하는 메서드
     *
     * @return
     */
    public SecretKey getSigninKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperty.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
