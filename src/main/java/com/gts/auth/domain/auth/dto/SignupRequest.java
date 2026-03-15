package com.gts.auth.domain.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Email
        String email,

        @NotBlank
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
                 message = "비밀번호는 영문, 숫자, 특수문자를 포함한 8자 이상이어야 합니다.")
        String password,

        @NotBlank @Size(min = 2, max = 20, message = "닉네임은 2~20자여야 합니다.")
        String nickname,

        @AssertTrue(message = "개인정보처리방침에 동의해야 합니다.")
        boolean agreedToPrivacyPolicy
) {}
