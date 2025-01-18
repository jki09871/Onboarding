package com.example.domain.signup.service;

import com.example.common.ErrorStatus;
import com.example.common.exception.ApiException;
import com.example.domain.entity.User;
import com.example.domain.enums.UserRole;
import com.example.domain.repository.UserRepository;
import com.example.domain.signup.dto.request.SignupRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignupServiceTest {

    @InjectMocks
    private SignupService signupService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void signup_Success() {
        // Given
        SignupRequest request = new SignupRequest(
                "testUser",
                "password123",
                "testNickname",
                UserRole.ROLE_USER,
                null // 일반 사용자
        );

        when(userRepository.existsByNickname("testNickname")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");

        // When
        signupService.signup(request);

        // Then
        verify(userRepository, times(1)).existsByNickname("testUser");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void signup_FailsWhenUsernameAlreadyExists() {
        // Given
        SignupRequest request = new SignupRequest(
                "testUser",
                "password123",
                "testNickname",
                UserRole.ROLE_USER,
                null // 일반 사용자
        );        when(userRepository.existsByNickname("existingUser")).thenReturn(true);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> signupService.signup(request));
        assertEquals(ErrorStatus.NOT_FOUND_USER, exception.getErrorCode());

        verify(userRepository, times(1)).existsByNickname("existingUser");
        verify(passwordEncoder, times(0)).encode(anyString());
        verify(userRepository, times(0)).save(any(User.class));
    }
}
