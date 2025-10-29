package boot.kakaotech.communitybe.auth.jwt;

import boot.kakaotech.communitybe.user.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire_time.access_token}")
    private long accessTokenExpireTime;

    @Value("${jwt.expire_time.refresh_token}")
    private long refreshTokenExpireTime;

    /**
     * access token 생성하는 메서드
     * subject로 userId, claim으로 nickname 주입(email은 민감정보라 판단하여 뺐음)
     *
     * @param user
     * @return
     */
    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("nickname", user.getNickname())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpireTime))
                .signWith(getSigninKey())
                .compact();
    }

    /**
     * refresh token 생성하는 메서드
     * subject로 userId, claim으로 type, id로 UUID 설정
     *
     * @param user
     * @return
     */
    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("typ", "refresh")
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpireTime))
                .signWith(getSigninKey())
                .compact();
    }

    /**
     * refresh token 만료시간 반환하는 게터
     * secret이나 access token 만료시간은 외부에서 호출할 일이 없어 @Getter 쓰지 않고 얘만 따로 구현
     *
     * @return
     */
    public long getRefreshTokenExpireTime() {
        return refreshTokenExpireTime;
    }

    public SecretKey getSigninKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
