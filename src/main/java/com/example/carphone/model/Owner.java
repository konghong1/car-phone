package com.example.carphone.model;

import java.time.Instant;

public record Owner(
        String id,
        String openid,
        String nickname,
        Instant createdAt
) {
}
