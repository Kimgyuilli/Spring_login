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
        return returnType.hasMethodAnnotation(AutoApiResponse.class) 
                || returnType.getDeclaringClass().isAnnotationPresent(AutoApiResponse.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        if (body instanceof CommonApiResponse) {
            return body;
        }

        if (body == null || returnType.getParameterType() == Void.class) {
            return CommonApiResponse.success(MemberSuccessCode.SUCCESS);
        }

        return CommonApiResponse.success(MemberSuccessCode.SUCCESS, body);
    }
}