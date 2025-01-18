package com.example.security;

import com.example.common.ErrorStatus;
import com.example.common.exception.ApiException;
import com.example.domain.enums.TokenType;
import com.example.domain.enums.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "JWT Util")
@Component
public class JwtUtil {

    // Header KEY 값
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String REFRESH_TOKEN_HEADER = "Refresh-Token";
    // 사용자 권한 값의 KEY
    public static final String REDIS_REFRESH_TOKEN_PREFIX = "Refresh_";
    public static final String BEARER_PREFIX = "Bearer ";

    @Value("${jwt.secret.key}")
    private String secretKey;

    private SecretKey key;

    @PostConstruct
    private void init() {
        // 키 설정
        key = getSecretKeyFromBase64(secretKey);
    }

    public void addTokenToHeader(HttpServletResponse response, String token) {
        try {
            token = URLEncoder.encode(token, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            response.addHeader(AUTHORIZATION_HEADER, token);
        } catch (Exception e) {
            log.error("Failed to encode token", e);
        }
    }


    public String substringToken(String token) {
        if (StringUtils.hasText(token) && token.startsWith(BEARER_PREFIX)) {
            return token.substring(BEARER_PREFIX.length());
        }
        log.error("Not Found Token");
        throw new IllegalArgumentException("Not Found Token");
    }

    private JwtParser getJwtParser() {
        return Jwts.parserBuilder().setSigningKey(key).build();
    }

    private SecretKey getSecretKeyFromBase64(String base64) {
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64));
        } catch (Exception e) {
            log.error("Failed to decode secret key", e);
            throw new IllegalArgumentException("Invalid secret key");
        }
    }


    public String createAccessToken(Long userId, String nickname, String userName,UserRole role) {
        Date now = new Date();
        return BEARER_PREFIX + Jwts.builder()
                .claim("category", TokenType.ACCESS.name())
                .setExpiration(new Date(now.getTime() + TokenType.ACCESS.getLifeTime()))
                .setSubject(String.valueOf(userId))
                .claim("nickname", nickname)
                .claim("userName",userName)
                .claim("userRole", role.getUserRole())
                .setIssuedAt(now)
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(String.valueOf(userId)) // 사용자 ID만 포함
                .claim("category", TokenType.REFRESH.name()) // 리프레시 토큰임을 명시
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + TokenType.REFRESH.getLifeTime())) // 더 긴 유효 기간
                .signWith(key)
                .compact();
    }

    public Claims extractClaims(String token) {
            return getJwtParser().parseClaimsJws(token).getBody();
    }

    public boolean isExpired(String token) {
        try {
            Date expiration = extractClaims(token).getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            log.error("Failed to check token expiration", e);
            throw new IllegalArgumentException("Invalid token");
        }
    }

    public String getCategory(String token) {
        try {
            return extractClaims(token).get("category", String.class);
        } catch (Exception e) {
            log.error("Failed to extract category", e);
            throw new IllegalArgumentException("Invalid token");
        }
    }
    

    public String getUserId(String token) {
        try {
            return extractClaims(token).getSubject();
        } catch (Exception e) {
            log.error("Failed to extract user ID", e);
            throw new IllegalArgumentException("Invalid token");
        }
    }

}
