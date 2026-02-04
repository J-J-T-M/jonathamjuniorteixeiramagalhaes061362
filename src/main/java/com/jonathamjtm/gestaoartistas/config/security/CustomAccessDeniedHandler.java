package com.jonathamjtm.gestaoartistas.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jonathamjtm.gestaoartistas.dto.error.StandardError;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler, AuthenticationEntryPoint {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        montarResposta(response, HttpServletResponse.SC_FORBIDDEN, "Acesso Negado", "Voce nao tem permissao para acessar este recurso.", request.getRequestURI());
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        montarResposta(response, HttpServletResponse.SC_UNAUTHORIZED, "Nao Autorizado", "Token invalido ou nao informado.", request.getRequestURI());
    }

    private void montarResposta(HttpServletResponse response, int status, String error, String message, String path) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        StandardError err = new StandardError();
        err.setTimestamp(LocalDateTime.now());
        err.setStatus(status);
        err.setError(error);
        err.setMessage(message);
        err.setPath(path);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        response.getWriter().write(mapper.writeValueAsString(err));
    }
}