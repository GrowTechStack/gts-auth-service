package com.gts.auth.domain.user.dto;

import com.gts.auth.domain.user.entity.User;

public record UserResponse(
        Long id,
        String email,
        String nickname,
        String role,
        String provider
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name(),
                user.getProvider().name()
        );
    }
}
