package boot.kakaotech.communitybe.auth.jwt;

import boot.kakaotech.communitybe.common.exception.BusinessException;
import boot.kakaotech.communitybe.common.exception.ErrorCode;
import boot.kakaotech.communitybe.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtVerifier {

    @Value("${jwt.secret}")
    private String secret;

    private final JwtProvider jwtProvider;

    /**
     * 토큰 유효성검사 메서드
     * 1. userId를 토큰으로부터 추출한 뒤 null 여부, 전달받은 user의 id값과 비교
     * 2. 토큰 만료 여부 확인
     *
     * @param token
     * @param user
     * @return
     */
    public boolean isValidToken(String token, User user) {
        Integer userId = extractUserIdFromToken(token);

        if (userId == null || !userId.equals(user.getId())) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        if (isExpiredToken(token)) {
            return false;
        }

        return true;
    }

    /**
     * 만료된 토큰인지 확인하는 메서드
     *
     * @param token
     * @return
     */
    private boolean isExpiredToken(String token) {
        Claims claims = getClaimsFromToken(token);

        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

    /**
     * 토큰의 subject인 userId를 Integer로 형변환 후 반환하는 메서드
     *
     * @param token
     * @return
     */
    private Integer extractUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);

        return Integer.getInteger(claims.getSubject());
    }

    /**
     * 토큰에서 모든 Claim들을 추출하여 반환하는 메서드
     *
     * @param token
     * @return
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(jwtProvider.getSigninKey())
                .build()
                .parseSignedClaims(token).getPayload();
    }

}
