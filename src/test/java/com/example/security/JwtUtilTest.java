package com.example.security;

import com.example.domain.enums.TokenType;
import com.example.domain.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String TEST_SECRET_KEY = "dXNlcktleUluQmFzZTY0Rm9ybWF0MTIzNDU2Nzg5MDEyMzQ1Njc4OTA=";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // secretKey 및 초기화 설정
        ReflectionTestUtils.setField(jwtUtil, "secretKey", TEST_SECRET_KEY);
        ReflectionTestUtils.invokeMethod(jwtUtil, "init"); // @PostConstruct 호출
    }

    @Test
    void testCreateAccessToken() {
        // Given
        Long userId = 1L;
        String nickname = "testUser";
        String username = "testUsername";
        UserRole role = UserRole.ROLE_USER;

        // When
        String accessToken = jwtUtil.createAccessToken(userId, nickname, username, role);

        // Then
        assertNotNull(accessToken);
        assertTrue(accessToken.startsWith(JwtUtil.BEARER_PREFIX));

        // Verify Claims
        Claims claims = jwtUtil.extractClaims(accessToken.substring(JwtUtil.BEARER_PREFIX.length()));
        assertEquals(userId.toString(), claims.getSubject());
        assertEquals(nickname, claims.get("nickname", String.class));
        assertEquals(username, claims.get("userName", String.class));
        assertEquals(role.getUserRole(), claims.get("userRole", String.class));
        assertEquals(TokenType.ACCESS.name(), claims.get("category", String.class));
    }

    @Test
    void testCreateRefreshToken() {
        // Given
        Long userId = 1L;

        // When
        String refreshToken = jwtUtil.createRefreshToken(userId);

        // Then
        assertNotNull(refreshToken);

        // Verify Claims
        Claims claims = jwtUtil.extractClaims(refreshToken);
        assertEquals(userId.toString(), claims.getSubject());
        assertEquals(TokenType.REFRESH.name(), claims.get("category", String.class));
    }

    @Test
    void testIsExpired() {
        // Given
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET_KEY));
        String expiredToken = Jwts.builder()
                .setExpiration(new Date(System.currentTimeMillis() - 1000)) // 1초 전으로 만료된 토큰 생성
                .setSubject("1") // 사용자 ID
                .claim("category", TokenType.ACCESS.name())
                .signWith(key)
                .compact();

        // When & Then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> jwtUtil.isExpired(expiredToken));
        assertTrue(exception.getMessage().contains("Invalid token"), "Exception should indicate invalid token");
    }



    @Test
    void testGetCategory() {
        // Given
        Long userId = 1L;
        String refreshToken = jwtUtil.createRefreshToken(userId);

        // When
        String category = jwtUtil.getCategory(refreshToken);

        // Then
        assertEquals(TokenType.REFRESH.name(), category);
    }

    @Test
    void testGetUserId() {
        // Given
        Long userId = 1L;
        String refreshToken = jwtUtil.createRefreshToken(userId);

        // When
        String extractedUserId = jwtUtil.getUserId(refreshToken);

        // Then
        assertEquals(userId.toString(), extractedUserId);
    }

    @Test
    void testInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        Exception extractClaimsException = assertThrows(Exception.class, () -> jwtUtil.extractClaims(invalidToken));
        assertTrue(
                extractClaimsException instanceof MalformedJwtException || extractClaimsException instanceof IllegalArgumentException,
                "Unexpected exception type for extractClaims"
        );

        Exception isExpiredException = assertThrows(Exception.class, () -> jwtUtil.isExpired(invalidToken));
        assertTrue(
                isExpiredException instanceof MalformedJwtException || isExpiredException instanceof IllegalArgumentException,
                "Unexpected exception type for isExpired"
        );

        Exception getCategoryException = assertThrows(Exception.class, () -> jwtUtil.getCategory(invalidToken));
        assertTrue(
                getCategoryException instanceof MalformedJwtException || getCategoryException instanceof IllegalArgumentException,
                "Unexpected exception type for getCategory"
        );

        Exception getUserIdException = assertThrows(Exception.class, () -> jwtUtil.getUserId(invalidToken));
        assertTrue(
                getUserIdException instanceof MalformedJwtException || getUserIdException instanceof IllegalArgumentException,
                "Unexpected exception type for getUserId"
        );
    }

}
