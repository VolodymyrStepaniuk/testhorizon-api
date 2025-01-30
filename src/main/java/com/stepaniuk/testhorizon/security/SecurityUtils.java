package com.stepaniuk.testhorizon.security;

import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import com.stepaniuk.testhorizon.user.User;
import jakarta.annotation.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public abstract class SecurityUtils {

    private SecurityUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isOwner(AuthInfo authInfo, Long ownerId) {
        return authInfo.getUserId().equals(ownerId);
    }

    public static boolean hasAuthority(AuthInfo authInfo, String authority) {
        for (GrantedAuthority grantedAuthority : authInfo.getAuthorities()) {
            if (grantedAuthority.getAuthority().equals(authority)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static AuthInfo resolveAuthInfo(@Nullable Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        var authorities = authentication.getAuthorities();

        Long userId;

        User user = (User) authentication.getPrincipal();

        if (user == null) {
            return null;
        }

        userId = user.getId();

        return new AuthInfo(userId, authorities);
    }
}
