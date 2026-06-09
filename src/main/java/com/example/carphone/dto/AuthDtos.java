package com.example.carphone.dto;

import jakarta.validation.constraints.NotBlank;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record WechatLoginRequest(
            @NotBlank String code,
            String nickname
    ) {
    }

    public record LoginResponse(
            String token,
            String ownerId,
            String openid
    ) {
    }
}
