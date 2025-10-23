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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    private final RedisTemplate<String, String> redisTemplate;
    private static final String REFRESH_TOKEN_PREFIX = "RT:";

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
    public boolean isValidRefreshToken(String refreshToken, User user) {
        String email = extractEmailFromToken(refreshToken);
        if (!email.equals(user.getEmail())) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String storedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + user.getEmail());
        System.out.println("@@@@@@@@@@@@@@@@ storedToken: " + storedToken);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

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
        String refreshToken = generateToken(user, refreshTokenExpireTime);
        saveRefreshToken(user.getEmail(), refreshToken);

        return refreshToken;
    }

    /**
     * 토큰에서 이메일 추출하는 메서드
     *
     * @param token
     * @return
     */
    @Override
    public String getEmailFromToken(String token) {
        return extractEmailFromToken(token);
    }

    /**
     * refresh token 만료시간 반환하는 메서드
     *
     * @return
     */
    @Override
    public long getRefreshTokenExpireTime() {
        return refreshTokenExpireTime;
    }

    /**
     * refresh token rotate 방식을 적용한 새 토큰 발급하는 메서드
     *
     * @param refreshToken
     * @param user
     * @return
     */
    @Override
    public Map<String, String> rotateTokens(String refreshToken, User user) {
        if (!isValidRefreshToken(refreshToken, user)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        invalidateRefreshToken(user.getEmail());

        String newAccessToken = generateToken(user, accessTokenExpireTime);
        String newRefreshToken = generateToken(user, refreshTokenExpireTime);

        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + user.getEmail(),
                newRefreshToken,
                Duration.ofMillis(accessTokenExpireTime)
        );

        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", newAccessToken);
        tokens.put("refresh_token", newRefreshToken);
        return tokens;
    }

    /**
     * redis에 저장된 refresh token을 지우는 메서드
     *
     * @param email
     */
    @Override
    public void invalidateRefreshToken(String email) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + email);
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

    /**
     * refresh token을 레디스에 저장하는 메서드
     * - RT:${user's email}의 키로 저장
     *
     * @param email
     * @param refreshToken
     */
    private void saveRefreshToken(String email, String refreshToken) {
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + email,
                refreshToken,
                refreshTokenExpireTime,
                TimeUnit.MILLISECONDS
        );
    }

}
