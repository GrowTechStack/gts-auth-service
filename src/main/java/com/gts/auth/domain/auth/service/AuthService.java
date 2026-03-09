package com.gts.auth.domain.auth.service;

import com.gts.auth.domain.auth.dto.LoginRequest;
import com.gts.auth.domain.auth.dto.RefreshRequest;
import com.gts.auth.domain.auth.dto.SignupRequest;
import com.gts.auth.domain.auth.dto.TokenResponse;

public interface AuthService {

    void signup(SignupRequest request);

    TokenResponse login(LoginRequest request);

    TokenResponse refresh(RefreshRequest request);

    void logout(Long userId);
}
