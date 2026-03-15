package com.gts.auth.domain.auth.controller;

import com.gts.auth.domain.auth.dto.LoginRequest;
import com.gts.auth.domain.auth.dto.RefreshRequest;
import com.gts.auth.domain.auth.dto.SignupRequest;
import com.gts.auth.domain.auth.dto.TokenResponse;
import com.gts.auth.domain.auth.service.AuthService;
import com.gts.auth.domain.user.dto.UserResponse;
import com.gts.auth.global.common.response.ApiResult;
import com.gts.auth.global.jwt.JwtProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResult<Void> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ApiResult.success(null);
    }

    @PostMapping("/login")
    public ApiResult<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResult.success(authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResult<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResult.success(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ApiResult<Void> logout(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtProvider.getUserId(authHeader.substring(7));
        authService.logout(userId);
        return ApiResult.success(null);
    }

    @GetMapping("/me")
    public ApiResult<UserResponse> me(@RequestHeader("X-User-Id") Long userId) {
        return ApiResult.success(authService.getMe(userId));
    }

    @DeleteMapping("/withdraw")
    public ApiResult<Void> withdraw(@RequestHeader("X-User-Id") Long userId) {
        authService.withdraw(userId);
        return ApiResult.success(null);
    }
}
