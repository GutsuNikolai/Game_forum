package com.example.gameforum.security;

import com.example.gameforum.common.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class RestAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper om = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        try {
            response.setStatus(401);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ApiError err = ApiError.of(401, "Unauthorized", "Authentication required", request.getRequestURI());
            om.writeValue(response.getOutputStream(), err);
        } catch (Exception ignored) {}
    }
}
