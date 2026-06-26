package com.rrms.security;

import com.rrms.domain.entity.UserAccount;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {
    private final Map<String, SessionUser> sessions = new ConcurrentHashMap<>();

    public String create(UserAccount user) {
        String token = UUID.randomUUID().toString();
        Long tenantId = user.getTenant() == null ? null : user.getTenant().getId();
        sessions.put(token, new SessionUser(user.getId(), tenantId, user.getRole(), user.getUsername()));
        return token;
    }

    public Optional<SessionUser> get(String token) {
        return Optional.ofNullable(sessions.get(token));
    }

    public void invalidate(String token) {
        sessions.remove(token);
    }
}
