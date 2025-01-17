package com.example.domain.signup.dto.response;

import com.example.domain.entity.User;
import com.example.domain.enums.UserRole;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Getter
@NoArgsConstructor
public class SignupResponse {

    private String username;
    private String nickname;
    private List<UserRole> authorities;

    private SignupResponse(String username, String nickname, UserRole userRole){
        this.username = username;
        this.nickname = nickname;
        this.authorities = Collections.singletonList(userRole);
    }

    public static SignupResponse signupResponse(User user) {
        return new SignupResponse(user.getUsername(), user.getNickname(), user.getUserRole());
    }

}
