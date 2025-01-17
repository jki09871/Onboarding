package com.example.domain.signup.controller;

import com.example.common.ApiResponse;
import com.example.domain.signup.dto.request.SignupRequest;
import com.example.domain.signup.dto.response.SignupResponse;
import com.example.domain.signup.service.SignupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class SignupController {

    private final SignupService signupService;

    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest authRequest) {
        SignupResponse response = signupService.signup(authRequest);
        return ApiResponse.ok("회원가입이 완료되었습니다.", response);
    }

}
