package com.example.login.global.interceptor;

import com.example.login.global.dto.CommonApiResponse;
import com.example.login.global.response.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private final ConcurrentHashMap<String, RateLimiter> rateLimiterMap;
    private final ObjectMapper objectMapper;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIp(request);
        String endpoint = request.getRequestURI();
        String key = clientIp + ":" + endpoint;
        
        // 로그인 엔드포인트에 대해서만 Rate Limiting 적용
        if (endpoint.contains("/member/login") || endpoint.contains("/oauth2/")) {
            RateLimiter rateLimiter = rateLimiterMap.computeIfAbsent(key, 
                k -> RateLimiter.create(5.0 / 60.0)); // 분당 5회
            
            if (!rateLimiter.tryAcquire()) {
                log.warn("Rate limit exceeded for IP: {} on endpoint: {}", clientIp, endpoint);
                
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                
                CommonApiResponse<Void> errorResponse = CommonApiResponse.fail(ErrorCode.TOO_MANY_REQUESTS);
                response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                
                return false;
            }
        }
        
        return true;
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}