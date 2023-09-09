package com.chung.lifusic.account.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@RequiredArgsConstructor
@Service
public class JwtService {

    @Value("${security.secret.key}")
    private String SECRET_KEY; // jwt 서명 키

    @Value("${security.jwt.expired_after_ms}")
    private int JWT_EXPIRED_AFTER_MS; // jwt 토큰 발급 후 몇 ms 후 만료 되는 지

    private final RedisTemplate<String, String> redisTemplate;

    // token으로 부터 username(이메일)을 추출한다.
    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception exception) {
            return null;
        }
    }

    /**
     *
     * @param token jwt 토큰
     * @param claimsResolver jwt 토큰으로 부터 어떤 정보를 추출할 지 지정하는 함수
     * @return 토큰으로 부터 추출한 정보
     * @param <T> 토큰으로 부터 추출한 정보의 타입
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) throws Exception{
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // userDetails만 가지고 토큰 생성
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    // claim, userDetails를 가지고 토큰 생성
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        final String jwtToken = Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRED_AFTER_MS))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
        // redis에 token 정보 저장. jwt를 강제로 만료시킬 수 없기 때문에, redis로 구현
        final String redisKey = getRedisKey(userDetails.getUsername());
        redisTemplate.opsForValue().set(redisKey, jwtToken);
        redisTemplate.expire(redisKey, JWT_EXPIRED_AFTER_MS, TimeUnit.MILLISECONDS);
        return jwtToken;
    }

    // 토큰이 유효한 지 여부 확인
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // token으로 부터 추출한 유저네임(이메일)과 db로부터 가져온 유저네임이 같은 지 확인한다.
        final boolean isUserNameMatched = username.equals(userDetails.getUsername());

        // redis에 키가 없다면 로그아웃 또는 Redis TTL이 지난 것으로 처리
        final String redisKey = getRedisKey(username);
        final String tokenFromRedis = redisTemplate.opsForValue().get(redisKey);

        return isUserNameMatched && !isTokenExpired(token) && tokenFromRedis != null;
    }

    // 토큰이 만료되었는 지 확인
    public boolean isTokenExpired(String token) {
        // 현재 날짜보다 만료일이 앞에 있다면 만료되었다고 판단한다.
        final Date expiration = extractExpiration(token);
        if (expiration == null) {
            return true;
        }
        return expiration.before(new Date());
    }

    // logout 시 redis에서 토큰을 저장하고 있는 키 삭제
    public void expireToken(String email) {
        final String redisKey = getRedisKey(email);
        if (redisTemplate.opsForValue().get(redisKey) != null) {
            redisTemplate.delete(redisKey); // redis에서 토큰을 삭제한다.
        }
    }

    // 토큰으로부터 토큰 만료일을 가져온다.
    private Date extractExpiration(String token) {
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (Exception exception) {
            return null;
        }
    }

    // 토큰으로부터 모든 정보를 가져온다
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey()) /* jwt가 중간에 변경되지 않았는 지 확인하기 위한 서명키 */
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String getRedisKey(String email) {
        return String.format("JWT_TOKEN:%s", email);
    }
}
