package com.gts.auth.domain.auth.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {}
