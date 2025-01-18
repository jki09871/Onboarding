package com.example.domain.login.service;

import com.example.common.ErrorStatus;
import com.example.common.exception.ApiException;
import com.example.domain.entity.User;
import com.example.domain.enums.TokenType;
import com.example.domain.enums.UserRole;
import com.example.domain.login.dto.request.LoginRequest;
import com.example.domain.login.dto.response.LoginResponse;
import com.example.domain.repository.UserRepository;
import com.example.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


class LoginServiceTest {

    @InjectMocks
    private LoginService loginService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Spy // spy로 변경
    private JwtUtil jwtUtil = new JwtUtil(); // 초기화 필요

    @Mock
    private UserRepository userRepository;

    private static final String TEST_SECRET_KEY = "dXNlcktleUluQmFzZTY0Rm9ybWF0MTIzNDU2Nzg5MDEyMzQ1Njc4OTA=";

    @BeforeEach
    void setUp() throws Exception {
        // Mockito 초기화
        MockitoAnnotations.openMocks(this);

        // 리플렉션으로 secretKey 필드에 테스트용 키 주입
        Field secretKeyField = JwtUtil.class.getDeclaredField("secretKey");
        secretKeyField.setAccessible(true);
        secretKeyField.set(jwtUtil, TEST_SECRET_KEY);

        // init() 호출해서 내부적으로 keyBytes 초기화
        Method initMethod = JwtUtil.class.getDeclaredMethod("init");
        initMethod.setAccessible(true);
        initMethod.invoke(jwtUtil);

        // LoginService 내부의 jwtUtil 필드를 실제 Spy 객체로 주입
        Field jwtUtilField = LoginService.class.getDeclaredField("jwtUtil");
        jwtUtilField.setAccessible(true);
        jwtUtilField.set(loginService, jwtUtil);
    }

    @Test
    void testLogin_Success() {
        // Given
        LoginRequest request = new LoginRequest("testUser", "password");

        User user = User.createUser("username", "nickname", "encodedPassword", UserRole.ROLE_ADMIN);

        String refreshToken = Jwts.builder()
                .setSubject("1") // subject 강제 설정
                .claim("category", "REFRESH")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 600000)) // 10분 유효
                .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(TEST_SECRET_KEY)))
                .compact();

        ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(userRepository.findByUsername(eq("testUser"))).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq("password"), eq("encodedPassword"))).thenReturn(true);
        doReturn(refreshToken).when(jwtUtil).createRefreshToken(anyLong());

        // When
        LoginResponse response = loginService.login(request);

        // Then
        assertNotNull(response);

        // Verify AccessToken Claims
        Claims accessTokenClaims = jwtUtil.extractClaims(response.getAccessToken().substring(JwtUtil.BEARER_PREFIX.length()));
        assertEquals("nickname", accessTokenClaims.get("nickname", String.class));
        assertEquals("username", accessTokenClaims.get("userName", String.class));
        assertEquals("ROLE_ADMIN", accessTokenClaims.get("userRole", String.class));
    }


    @Test
    void testLogin_UserNotFound() {
        // Given
        LoginRequest request = new LoginRequest("nonExistentUser", "password");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> loginService.login(request));
        assertEquals(ErrorStatus.NOT_FOUND_USER, exception.getErrorCode());
    }



    @Test
    void testReissue_ExpiredToken() {
        // Given
        String tokenWithBearer = "Bearer expiredRefreshToken";

        // substringToken(...)도 Spy에서 꼭 doReturn(...)으로
        doReturn("expiredRefreshToken")
                .when(jwtUtil).substringToken(anyString());

        // getCategory(...)도 doReturn(...)으로 완전 Stub
        doReturn(TokenType.REFRESH.name())
                .when(jwtUtil).getCategory(anyString());

        // 만료된 토큰이라고 가정해서 isExpired(...) 시 예외 던짐
        doThrow(new ApiException(ErrorStatus.EXPIRED_REFRESH_TOKEN))
                .when(jwtUtil).isExpired(anyString());

        // When & Then
        ApiException ex = assertThrows(ApiException.class,
                () -> loginService.reissue(tokenWithBearer)
        );
        assertEquals(ErrorStatus.EXPIRED_REFRESH_TOKEN, ex.getErrorCode());
    }


    @Test
    void testValidatePasswordMatch_InvalidPassword() {
        // Given
        LoginRequest loginRequest = new LoginRequest("username", "password");
        User user = User.createUser("username","nickname", "password", UserRole.ROLE_USER);
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> loginService.login(loginRequest));
        assertEquals(ErrorStatus.INVALID_CREDENTIALS, exception.getErrorCode());
    }
}
