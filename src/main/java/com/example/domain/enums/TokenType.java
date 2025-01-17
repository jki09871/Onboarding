package com.example.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TokenType {
    ACCESS(4 * 60 * 60 * 1000), // 4hour
    REFRESH(24 * 60 * 60 * 1000); // 24hour
    private final long lifeTime;
}
