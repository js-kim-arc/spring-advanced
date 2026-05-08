package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class AuthUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAuthAnnotation = parameter.getParameterAnnotation(Auth.class) != null;
        if (!hasAuthAnnotation) {
            return false;
        }

        // @Auth 가 붙어 있는데 타입이 AuthUser 가 아니면 개발자 실수이므로 명시적으로 알려준다.
        if (!parameter.getParameterType().equals(AuthUser.class)) {
            throw new AuthException("@Auth 는 AuthUser 타입 파라미터에만 사용할 수 있습니다.");
        }

        return true;
    }

    @Override
    public Object resolveArgument(
            @Nullable MethodParameter parameter,
            @Nullable ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            @Nullable WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        // JwtFilter 에서 set 한 userId, email, userRole 값을 가져옴
        Long userId = (Long) request.getAttribute("userId");
        String email = (String) request.getAttribute("email");
        String userRoleAttr = (String) request.getAttribute("userRole");

        if (userId == null || email == null || userRoleAttr == null) {
            throw new AuthException("인증 정보가 없습니다.");
        }

        return new AuthUser(userId, email, UserRole.of(userRoleAttr));
    }
}
