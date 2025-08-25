package com.example.login.global.response;

import com.example.login.global.dto.CommonApiResponse;
import com.example.login.global.response.SuccessType.MemberSuccessCode;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * API 응답을 자동으로 CommonApiResponse로 감싸주는 Advice
 */
@RestControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // @AutoApiResponse 어노테이션이 있는 메서드나 클래스만 처리
        return returnType.hasMethodAnnotation(AutoApiResponse.class) 
                || returnType.getDeclaringClass().isAnnotationPresent(AutoApiResponse.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        
        // 이미 CommonApiResponse로 감싸져 있으면 그대로 반환
        if (body instanceof CommonApiResponse) {
            return body;
        }
        
        // Void 타입인 경우 데이터 없이 성공 응답
        if (body == null || returnType.getParameterType() == Void.class) {
            return CommonApiResponse.success(MemberSuccessCode.SUCCESS);
        }
        
        // 일반 데이터인 경우 성공 응답으로 감싸기
        return CommonApiResponse.success(MemberSuccessCode.SUCCESS, body);
    }
}