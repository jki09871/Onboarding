package com.example.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseCode {
    //예외 예시
    EXAMPLE_ERROR(HttpStatus.BAD_REQUEST, 400, "ApiException 예외 처리 예시"),
    NOT_FOUND_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, 404, "재발급하려면 리프레쉬 토큰이 필요합니다."),
    INVALID_TOKEN_FORMAT(HttpStatus.BAD_REQUEST, 400, "잘못된 토큰 형식 입니다."),
    NOT_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, 400, "리프레쉬 토큰이 아닙니다."),
    LOG_IN_AGAIN(HttpStatus.BAD_REQUEST, 400, "로그인 시간 만료."),

    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,401,"만료된 리프레쉬 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, 400, "잘못된 리프레쉬 토큰입니다"),
    // 유저 관련 예외

    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, 400, "중복된 닉네임 입니다."),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, 404, "유저를 찾을 수 없습니다."),

    INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST,400,"잘못된 비밀번호 입니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, 400, "비밀번호는 대소문자 포함 영문 + 숫자 + 특수문자를 최소 1글자씩 포함해야 하며, 최소 8글자 이상이어야 합니다."),


    // DB 관련 예외
    SQL_EXCEPTION_OCCURRED(HttpStatus.INTERNAL_SERVER_ERROR, 500, "데이터베이스 작업 처리 중 예외가 발생했습니다."),


    private final HttpStatus httpStatus;
    private final Integer statusCode;
    private final String message;

    @Override
    public ExceptionCause getCauseHttpStatus() {
        return ExceptionCause.builder()
                .httpStatus(httpStatus)
                .statusCode(statusCode)
                .message(message)
                .build();
    }
}
