package com.example.domain.entity;

import com.example.common.Timestamped;
import com.example.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "users")
public class User extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(length = 150, nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false,  unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    // 유저 생성자
    private User (String username, String nickname,String password, UserRole userRole){
        this.username = username;
        this.nickname = nickname;
        this.password = password;
        this.userRole = userRole;
    }

    public static User createUser(String username, String nickname, String password, UserRole userRole) {
        return new User(username, nickname, password, userRole);
    }
}
