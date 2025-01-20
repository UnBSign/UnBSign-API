package com.sign.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.method.HandlerMethod;
import com.sign.utils.JwtUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor{
    
    // @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
    throws Exception {
        // Captura o cabeçalho Authorization
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7); // Remove o "Bearer "

            // Verifica se o token é válido
            if (JwtUtils.validateToken(token)) {
                // O token é válido, continua a execução
                return true;
            } else {
                // Token inválido, retorna erro 401 (Unauthorized)
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido ou expirado");
                return false; // Bloqueia a requisição
            }
        } else {
            // Caso o token não esteja presente, retorna erro 401
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token não fornecido");
            return false; // Bloqueia a requisição
        }
    }
}
