package org.example.subchecker.web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramAuthFilter extends OncePerRequestFilter {

    private final OtpService otpService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/api/web/users/login") || !path.startsWith("/api/web/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Признаюсь этот сектор сплошной вайбкод, но я уже устал пытаться парсить так,
        // чтоб хэш телеграма совпадал, потому решил сделать двухфакторку просто :)

        // Ищем и криптографически проверяем токен сессии (выдается после ввода OTP)
        // 1. Ищем и криптографически проверяем токен сессии (с учетом версии!)
        String sessionToken = request.getHeader("X-Session-Token");

        if (sessionToken != null && sessionToken.contains(":")) {
            try {
                String[] parts = sessionToken.split(":");
                Long userId = Long.valueOf(parts[0]);
                String signature = parts[1];

                // Делегируем проверку в OtpService
                if (otpService.validateSessionToken(userId, signature)) {
                    request.setAttribute("validatedTgId", userId);
                    filterChain.doFilter(request, response);
                    return;
                }
            } catch (Exception e) {
                log.error("Ошибка верификации сессионного токена: {}", e.getMessage());
            }
        }

        // Если нет активной сессии — отклоняем доступ
        log.warn("Отклонен доступ без активной сессии к {}", request.getRequestURI());
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\": \"Доступ запрещен. Требуется авторизация.\"}");
    }
}