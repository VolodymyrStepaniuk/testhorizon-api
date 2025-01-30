package com.stepaniuk.testhorizon.security.authinfo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
public class AuthInfo {

    private final Long userId;

    private final Collection<? extends GrantedAuthority> authorities;

    public AuthInfo(Long userId, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.authorities = List.copyOf(authorities);
    }

}
