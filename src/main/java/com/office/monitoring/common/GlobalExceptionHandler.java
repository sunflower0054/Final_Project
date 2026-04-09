package com.office.monitoring.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
/** 여러 기능에서 공통으로 사용하는 모델 구성과 예외 응답을 제공하는 구성 요소. */
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    /** 요청된 공통 작업에 필요한 입력을 반영해 결과 값을 생성한다. */
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("입력값이 올바르지 않습니다.");

        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", message
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    /** 요청된 공통 작업에 필요한 입력을 반영해 결과 값을 생성한다. */
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
        ));
    }

    @ExceptionHandler(IllegalStateException.class)
    /** 요청된 공통 작업에 필요한 입력을 반영해 결과 값을 생성한다. */
    public ResponseEntity<?> handleIllegalStateException(IllegalStateException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    /** 요청된 공통 작업에 필요한 입력을 반영해 결과 값을 생성한다. */
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "success", false,
                "message", e.getMessage()
        ));
    }
}
