package com.gts.auth.domain.auth.service.impl;

import com.gts.auth.domain.auth.dto.LoginRequest;
import com.gts.auth.domain.auth.dto.RefreshRequest;
import com.gts.auth.domain.auth.dto.SignupRequest;
import com.gts.auth.domain.auth.dto.TokenResponse;
import com.gts.auth.domain.auth.service.AuthService;
import com.gts.auth.domain.token.entity.RefreshToken;
import com.gts.auth.domain.token.repository.RefreshTokenRepository;
import com.gts.auth.domain.user.dto.UserResponse;
import com.gts.auth.domain.user.entity.User;
import com.gts.auth.domain.user.repository.UserRepository;
import com.gts.auth.global.error.ErrorCode;
import com.gts.auth.global.error.exception.BusinessException;
import com.gts.auth.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        User user = User.createLocal(request.email(), passwordEncoder.encode(request.password()), request.nickname());
        userRepository.save(user);
        log.info("[Auth] 회원가입 완료: email={}", request.email());
    }

    @Override
    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (user.getPassword() == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return issueTokens(user);
    }

    @Override
    @Transactional
    public TokenResponse refresh(RefreshRequest request) {
        String tokenHash = hash(request.refreshToken());
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        }

        User user = userRepository.findByIdAndDeletedAtIsNull(refreshToken.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        refreshTokenRepository.delete(refreshToken);
        return issueTokens(user);
    }

    @Override
    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
        log.info("[Auth] 로그아웃: userId={}", userId);
    }

    @Override
    public UserResponse getMe(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.withdraw();
        refreshTokenRepository.deleteByUserId(userId);
        log.info("[Auth] 회원탈퇴: userId={}", userId);
    }

    private TokenResponse issueTokens(User user) {
        refreshTokenRepository.deleteByUserId(user.getId());

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getRole().name());
        String rawRefreshToken = jwtProvider.generateRefreshToken(user.getId());

        long expiryMs = jwtProvider.getRefreshTokenExpiry();
        RefreshToken refreshToken = RefreshToken.of(
                user.getId(),
                hash(rawRefreshToken),
                LocalDateTime.now().plusSeconds(expiryMs / 1000)
        );
        refreshTokenRepository.save(refreshToken);

        return new TokenResponse(accessToken, rawRefreshToken);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("[Auth] 만료된 리프레시 토큰 정리 완료");
    }
}
