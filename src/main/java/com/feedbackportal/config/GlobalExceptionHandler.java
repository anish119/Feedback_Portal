package com.feedbackportal.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final CustomAccessDeniedHandler accessDeniedHandler;

    public GlobalExceptionHandler(CustomAccessDeniedHandler accessDeniedHandler) {
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public void handleAccessDenied(HttpServletRequest request,
                                   HttpServletResponse response,
                                   AccessDeniedException ex) throws IOException, ServletException {
        accessDeniedHandler.handle(request, response, ex);
    }
}
