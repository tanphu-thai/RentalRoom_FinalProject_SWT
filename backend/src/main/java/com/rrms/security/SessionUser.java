package com.rrms.security;

import com.rrms.domain.enums.UserRole;

public record SessionUser(Long userId, Long tenantId, UserRole role, String username) { }
