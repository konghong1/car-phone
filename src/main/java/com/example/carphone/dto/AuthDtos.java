package com.example.carphone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

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

    public record UpdateOwnerRequest(
            String nickname,
            @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String phone,
            String plateNo  // 车牌号
    ) {
    }
}
