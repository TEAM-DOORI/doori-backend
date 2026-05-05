package com.doori.doori_backend.global.security;

import com.doori.doori_backend.global.error.ErrorCode;
import com.doori.doori_backend.global.error.ErrorResponse;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityErrorResponder {

    private final ObjectMapper objectMapper;

    public void sendErrorResponse(
        HttpServletResponse response,
        HttpServletRequest request,
        ErrorCode errorCode
    ) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ErrorResponse.of(errorCode, request)));
    }
}
