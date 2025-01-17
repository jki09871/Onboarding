package com.example.domain.signup.service;

import com.example.common.ErrorStatus;
import com.example.common.exception.ApiException;
import com.example.domain.entity.User;
import com.example.domain.enums.UserRole;
import com.example.domain.signup.dto.request.SignupRequest;
import com.example.domain.signup.dto.response.SignupResponse;
import com.example.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SignupService {

    private final UserRepository repository;

    private final PasswordEncoder passwordEncoder;

    @Value("${admin.token}")
    private String adminToken;
    @Transactional
    public SignupResponse signup(SignupRequest request) {

        validatePassword(request.getPassword());
        UserRole userRole = validateAdminRole(request);
        checkNicknameDuplicate(request.getNickname());

        User user = User.createUser(request.getUsername(),
                                    request.getNickname(),
                                    passwordEncoder.encode(request.getPassword()),
                                    userRole);
        User save = repository.save(user);
        return SignupResponse.signupResponse(save);
    }

    private void validatePassword(String password) {
        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>]{8,}$")) {
            throw new ApiException(ErrorStatus.INVALID_REQUEST);
        }
    }

    private void checkNicknameDuplicate(String nickname) {
        if (repository.existsByNickname(nickname)) {
            throw new ApiException(ErrorStatus.DUPLICATE_NICKNAME);
        }
    }

    private UserRole validateAdminRole(SignupRequest request) {
        // 요청된 역할이 ROLE_ADMIN인지 확인
        if (request.getUserRole() == UserRole.ROLE_ADMIN) {
            // 관리자 토큰이 유효하지 않으면 예외를 던짐
            if (!StringUtils.hasText(request.getAdminToken()) || !request.getAdminToken().equals(adminToken)) {
                throw new ApiException(ErrorStatus.FORBIDDEN_TOKEN);
            }
            return UserRole.ROLE_ADMIN; // 유효하면 ROLE_ADMIN 반환
        }
        // 기본 역할인 ROLE_USER 반환
        return UserRole.ROLE_USER;
    }
}
