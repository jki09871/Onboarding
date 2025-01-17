package com.example.domain.login.controller;

import com.example.common.ApiResponse;
import com.example.domain.login.dto.request.LoginRequest;
import com.example.domain.login.dto.response.LoginResponse;
import com.example.domain.login.service.LoginService;
import com.example.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = loginService.login(loginRequest);
        return ApiResponse.ok("로그인 성공", response);
    }

    @PostMapping("/reissue")
    public ApiResponse<LoginResponse> reissue(@RequestHeader(JwtUtil.REFRESH_TOKEN_HEADER) String refreshToken) {
        LoginResponse response = loginService.reissue(refreshToken);
        return ApiResponse.createSuccess(HttpStatus.OK.value(), "토큰 재발급 성공", response);
    }
}
