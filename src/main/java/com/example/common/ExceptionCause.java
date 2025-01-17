package com.example.common;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Builder
@Getter
public class ExceptionCause {
    private HttpStatus httpStatus;
    private Integer statusCode;
    private String message;
}
