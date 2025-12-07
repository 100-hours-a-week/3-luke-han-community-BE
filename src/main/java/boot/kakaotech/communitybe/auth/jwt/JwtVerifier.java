package boot.kakaotech.communitybe.auth.jwt;

import boot.kakaotech.communitybe.common.exception.BusinessException;
import boot.kakaotech.communitybe.common.exception.ErrorCode;
import boot.kakaotech.communitybe.common.properties.JwtProperty;
import boot.kakaotech.communitybe.user.entity.User;
import io.jsonwebtoken.Claims;
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
public class JwtVerifier {

    private final JwtProperty property;

    /**
     * 토큰 유효성검사 메서드
     *
     * 1. userId 토큰에서 추출
     * 2. userId가 없거나 전달받은 유저의 id와 다르면 throw error
     *
     * @param token
     * @param user
     */
    public void isValidToken(String token, User user) {
        Integer userId = extractUserIdFromToken(token);

        if (userId == null || !userId.equals(user.getId()) || isExpiredToken(token)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * 토큰의 subject인 userId를 Integer로 형변환 후 반환하는 메서드
     *
     * @param token
     * @return
     */
    public Integer extractUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);

        return Integer.parseInt(claims.getSubject());
    }

    /**
     * 만료된 토큰인지 확인하는 메서드
     *
     * @param token
     * @return
     */
    public boolean isExpiredToken(String token) {
        Claims claims = getClaimsFromToken(token);

        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

    /**
     * 토큰에서 모든 Claim들을 추출하여 반환하는 메서드
     *
     * @param token
     * @return
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigninKey())
                .build()
                .parseSignedClaims(token).getPayload();
    }

    private SecretKey getSigninKey() {
        byte[] keyBytes = Decoders.BASE64.decode(property.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
