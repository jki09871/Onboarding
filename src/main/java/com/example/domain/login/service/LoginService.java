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
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginService {

    private final RedisTemplate<String, Object> redisTemplate;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    private final UserRepository repository;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = repository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_USER));


        validatePasswordMatch(request.getPassword(), user.getPassword());

        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getNickname(),user.getUsername(),user.getUserRole());
        String refreshToken = jwtUtil.createRefreshToken(user.getId());
        saveRefreshTokenInRedis(user.getId(), refreshToken);



        return new LoginResponse(accessToken, refreshToken);
    }


    private void saveRefreshTokenInRedis(Long userId, String refreshToken) {
        redisTemplate.opsForValue().set(
                JwtUtil.REDIS_REFRESH_TOKEN_PREFIX + userId,
                refreshToken,
                TokenType.REFRESH.getLifeTime(),
                TimeUnit.MILLISECONDS
        );
    }

    public LoginResponse reissue(String refreshToken) {
        // 1. 리프레시 토큰이 null인지 확인
        if (!StringUtils.hasText(refreshToken)) {
            throw new ApiException(ErrorStatus.NOT_FOUND_REFRESH_TOKEN);
        }

        try {
            // 2. "Bearer " 접두사를 제거
            refreshToken = jwtUtil.substringToken(refreshToken);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorStatus.INVALID_TOKEN_FORMAT);
        }

        // 3. 리프레시 토큰인지 확인
        String category = jwtUtil.getCategory(refreshToken);
        if (!TokenType.REFRESH.name().equals(category)) {
            throw new ApiException(ErrorStatus.NOT_REFRESH_TOKEN);
        }

        // 4. 토큰 만료 여부 검사
        try{
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new ApiException(ErrorStatus.EXPIRED_REFRESH_TOKEN);
        }

        // 5. 레디스에서 리프레시 토큰 가져오기
        String key = JwtUtil.REDIS_REFRESH_TOKEN_PREFIX + jwtUtil.getUserId(refreshToken);
        String storedRefreshToken = (String) redisTemplate.opsForValue().get(key);
        if (!StringUtils.hasText(storedRefreshToken)) {
            throw new ApiException(ErrorStatus.EXPIRED_REFRESH_TOKEN);
        }

        // 6. 저장된 리프레시 토큰의 접두사 제거 후 비교
        if (!refreshToken.equals(storedRefreshToken)) {
            throw new ApiException(ErrorStatus.INVALID_REFRESH_TOKEN);
        }

        // 7. 검증 통과 후 새로운 토큰 발급
        Claims claims = jwtUtil.extractClaims(refreshToken);
        Long userId = Long.parseLong(claims.getSubject());

        User user = repository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorStatus.LOG_IN_AGAIN));


        String newAccessToken = jwtUtil.createAccessToken(user.getId(),user.getNickname(),user.getUsername(),user.getUserRole());
        String newRefreshToken = jwtUtil.createRefreshToken(userId);

        // 8. 새로운 리프레시 토큰을 Redis에 저장
        // TTL 새로해서
        String userIdToString = String.valueOf(userId);
        Long ttl = redisTemplate.getExpire(JwtUtil.REDIS_REFRESH_TOKEN_PREFIX + userIdToString, TimeUnit.MILLISECONDS);

        if (ttl == null || ttl <= 0) {
            throw new ApiException(ErrorStatus.EXPIRED_REFRESH_TOKEN);
        }

        redisTemplate.opsForValue().set(JwtUtil.REDIS_REFRESH_TOKEN_PREFIX  + userIdToString, newRefreshToken, ttl, TimeUnit.MILLISECONDS);

        // 9. 새로운 액세스 토큰과 리프레시 토큰 반환
        return new LoginResponse(newAccessToken, newRefreshToken);
    }


    private void validatePasswordMatch(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new ApiException(ErrorStatus.INVALID_CREDENTIALS);
        }
    }
}
