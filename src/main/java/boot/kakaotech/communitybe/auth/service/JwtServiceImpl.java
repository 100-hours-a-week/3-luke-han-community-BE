package boot.kakaotech.communitybe.auth.service;

import boot.kakaotech.communitybe.common.exception.BusinessException;
import boot.kakaotech.communitybe.common.exception.ErrorCode;
import boot.kakaotech.communitybe.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expire_time.access_token}")
    private long accessTokenExpireTime;

    @Value("${jwt.expire_time.refresh_token}")
    private long refreshTokenExpireTime;

    /**
     * access token 유효성 검사
     * - 토큰에서 signature 비교 후 subject로 설정해놓은 email 비교하여 유효성 검사
     * - 토큰 만료시간 검사
     *
     * @param accessToken
     * @param user
     * @return
     */
    @Override
    public boolean isValidAccessToken(String accessToken, UserDetails user) {
        String email = extractEmailFromToken(accessToken);

        if (!email.equals(user.getUsername())) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        return true;
    }

    /**
     * refresh token 유효성 검사
     * - 토큰에서 signature 비교 후 subject로 설정해놓은 email 비교
     * - 토큰 만료시간 검사
     * - 레디스에 해당 유저의 리프레시토큰이 저장되어 있는지,
     *   저장되어 있다면 저장된 리프레시토큰과 일치하는지 검사
     *
     * @param refreshToken
     * @param user
     * @return
     */
    @Override
    public boolean isValidRefreshToken(String refreshToken, UserDetails user) {
        String email = extractEmailFromToken(refreshToken);
        if (!email.equals(user.getUsername())) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // TODO: redis에 저장된 리프레시 토큰과 비교 로직 추가

        return true;
    }

    /**
     * User 객체를 받아 액세스토큰을 생성하는 메서드
     * 로그인 성공 시 HTTPONLY COOKIES에 담아 반환 예정
     *
     * @param user
     * @return
     */
    @Override
    public String generateAccessToken(User user) {
        return generateToken(user, accessTokenExpireTime);
    }

    /**
     * User 객체를 받아 리프레시토큰을 생성하는 메서드
     * 로그인 성공 시 redis에 저장 후 헤더에 담아 반환 예정
     *
     * @param user
     * @return
     */
    @Override
    public String generateRefreshToken(User user) {
        return generateToken(user, refreshTokenExpireTime);
    }

    @Override
    public String getEmailFromToken(String token) {
        return extractEmailFromToken(token);
    }

    @Override
    public long getRefreshTokenExpireTime() {
        return refreshTokenExpireTime;
    }

    /**
     * 토큰에 subject로 담긴 이메일을 추출하는 메서드
     *
     * @param token
     * @return
     */
    private String extractEmailFromToken(String token) {
        Claims claims = extractAllClaimsFromToken(token);

        return claims.getSubject();
    }

    /**
     * 토큰 만료 여부를 확인하는 메서드
     *
     * @param token
     * @return
     */
    private boolean isExpiredToken(String token) {
        Claims claims = extractAllClaimsFromToken(token);

        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

    /**
     * 토큰을 받아 signature 비교 후 모든 클레임을 추출하여 반환하는 메서드
     *
     * @param token
     * @return
     */
    private Claims extractAllClaimsFromToken(String token) {
        return Jwts
                .parser()
                .verifyWith(getSigninKey())
                .build()
                .parseClaimsJws(token)
                .getPayload();
    }

    /**
     * User와 만료시간을 받아 토큰을 생성하는 메서드
     * 토큰에는 subject로 unique한 유저의 email과 클레임으로 userId가 들어간다.
     * 클레임에 있는 userId는 추후에 토큰에서 추출하여 api에서 사용할 예정
     *
     * @param user
     * @param expireTime
     * @return
     */
    private String generateToken(User user, long expireTime) {
        return Jwts
                .builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(getSigninKey())
                .compact();
    }

    /**
     * secretKey를 decode해서 반환하는 메서드
     *
     * @return
     */
    private SecretKey getSigninKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
