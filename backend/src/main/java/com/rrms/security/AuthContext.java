package com.rrms.security;

import com.rrms.common.BusinessException;
import com.rrms.domain.enums.UserRole;

public final class AuthContext {
    private static final ThreadLocal<SessionUser> CURRENT = new ThreadLocal<>();

    private AuthContext() { }

    public static void set(SessionUser user) { CURRENT.set(user); }
    public static void clear() { CURRENT.remove(); }

    public static SessionUser current() {
        SessionUser user = CURRENT.get();
        if (user == null) throw BusinessException.unauthorized("Authentication is required.");
        return user;
    }

    public static SessionUser requireAdmin() {
        SessionUser user = current();
        if (user.role() != UserRole.ADMIN) {
            throw BusinessException.forbidden("Admin access is required.");
        }
        return user;
    }
}
