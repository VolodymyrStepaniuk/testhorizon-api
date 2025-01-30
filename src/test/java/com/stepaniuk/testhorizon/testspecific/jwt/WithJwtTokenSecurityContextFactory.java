package com.stepaniuk.testhorizon.testspecific.jwt;

import com.stepaniuk.testhorizon.security.JwtProvider;
import com.stepaniuk.testhorizon.user.User;
import com.stepaniuk.testhorizon.user.authority.Authority;
import com.stepaniuk.testhorizon.user.authority.AuthorityName;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


@Component
public class WithJwtTokenSecurityContextFactory implements WithSecurityContextFactory<WithJwtToken> {

    private final JwtProvider jwtProvider;

    public WithJwtTokenSecurityContextFactory(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public SecurityContext createSecurityContext(WithJwtToken annotation) {
        // Створюємо UserDetails з вказаними параметрами
        User user = new User();

        user.setId(annotation.userId());
        user.setEmail(annotation.username());
        user.setAuthorities(Set.of(
                new Authority(1L, AuthorityName.valueOf(
                        annotation.authorities()[0]
                ))
        ));

        // Генеруємо токен за допомогою generateAccessToken
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("authorities", user.getAuthorities());

        String token;
        token = jwtProvider.generateAccessToken(extraClaims, user);

        // Створюємо Authentication об'єкт
        Authentication authentication = new JwtAuthenticationToken(token, user, user.getAuthorities());

        // Встановлюємо Authentication у SecurityContext
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        return context;
    }
}
