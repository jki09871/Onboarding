package com.example.domain.login.service;

import com.example.common.ErrorStatus;
import com.example.common.exception.ApiException;
import com.example.domain.entity.User;
import com.example.domain.enums.UserRole;
import com.example.domain.repository.UserRepository;
import com.example.domain.signup.dto.request.SignupRequest;
import com.example.domain.signup.dto.response.SignupResponse;
import com.example.domain.signup.service.SignupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignupServiceTest {

    @InjectMocks
    private SignupService signupService;

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void signup_SuccessForUser() {
        // Given
        SignupRequest request = new SignupRequest(
                "testUser",
                "ValidPassword123!",
                "testNickname",
                UserRole.ROLE_USER,
                null // 일반 사용자
        );

        when(repository.existsByNickname("testNickname")).thenReturn(false);
        when(passwordEncoder.encode("ValidPassword123!")).thenReturn("encodedPassword123");

        User savedUser = User.createUser(
                "testUser",
                "testNickname",
                "encodedPassword123",
                UserRole.ROLE_USER
        );

        when(repository.save(any(User.class))).thenReturn(savedUser);

        // When
        SignupResponse response = signupService.signup(request);

        // Then
        assertNotNull(response);
        assertEquals("testUser", response.getUsername());
        assertEquals("testNickname", response.getNickname());
        assertEquals(UserRole.ROLE_USER, response.getAuthorities().get(0));

        verify(repository, times(1)).existsByNickname("testNickname");
        verify(passwordEncoder, times(1)).encode("ValidPassword123!");
        verify(repository, times(1)).save(any(User.class));
    }

    @Test
    void signup_SuccessForAdmin() {
        // Given
        SignupRequest request = new SignupRequest(
                "adminUser",
                "AdminPassword123!",
                "adminNickname",
                UserRole.ROLE_ADMIN,
                "validAdminToken" // 관리자 토큰
        );

        when(repository.existsByNickname("adminNickname")).thenReturn(false);
        when(passwordEncoder.encode("AdminPassword123!")).thenReturn("encodedAdminPassword");

        User savedAdmin = User.createUser(
                "adminUser",
                "adminNickname",
                "encodedAdminPassword",
                UserRole.ROLE_ADMIN
        );

        when(repository.save(any(User.class))).thenReturn(savedAdmin);

        // Mock adminToken 설정
        ReflectionTestUtils.setField(signupService, "adminToken", "validAdminToken");

        // When
        SignupResponse response = signupService.signup(request);

        // Then
        assertNotNull(response);
        assertEquals("adminUser", response.getUsername());
        assertEquals("adminNickname", response.getNickname());
        assertEquals(UserRole.ROLE_ADMIN,  response.getAuthorities().get(0));

        verify(repository, times(1)).existsByNickname("adminNickname");
        verify(passwordEncoder, times(1)).encode("AdminPassword123!");
        verify(repository, times(1)).save(any(User.class));
    }

    @Test
    void signup_FailsWhenPasswordInvalid() {
        // Given
        SignupRequest request = new SignupRequest(
                "testUser",
                "weakpass",
                "testNickname",
                UserRole.ROLE_USER,
                null
        );

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> signupService.signup(request));
        assertEquals(ErrorStatus.INVALID_REQUEST, exception.getErrorCode());

        verify(repository, never()).existsByNickname(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(repository, never()).save(any(User.class));
    }

    @Test
    void signup_FailsWhenNicknameDuplicate() {
        // Given
        SignupRequest request = new SignupRequest(
                "testUser",
                "ValidPassword123!",
                "duplicateNickname",
                UserRole.ROLE_USER,
                null
        );

        when(repository.existsByNickname("duplicateNickname")).thenReturn(true);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> signupService.signup(request));
        assertEquals(ErrorStatus.DUPLICATE_NICKNAME, exception.getErrorCode());

        verify(repository, times(1)).existsByNickname("duplicateNickname");
        verify(passwordEncoder, never()).encode(anyString());
        verify(repository, never()).save(any(User.class));
    }

    @Test
    void signup_FailsWhenAdminTokenInvalid() {
        // Given
        SignupRequest request = new SignupRequest(
                "adminUser",
                "AdminPassword123!",
                "adminNickname",
                UserRole.ROLE_ADMIN,
                "invalidToken"
        );

        // Mock adminToken 설정
        ReflectionTestUtils.setField(signupService, "adminToken", "validAdminToken");

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> signupService.signup(request));
        assertEquals(ErrorStatus.FORBIDDEN_TOKEN, exception.getErrorCode());

        verify(repository, never()).existsByNickname(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(repository, never()).save(any(User.class));
    }
}
