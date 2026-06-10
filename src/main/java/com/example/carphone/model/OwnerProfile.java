package com.example.carphone.model;

import java.time.Instant;

/**
 * 车主档案 - 包含完整的挪车信息
 */
public record OwnerProfile(
        String id,
        String openid,         // 微信openid
        String nickname,       // 昵称/姓名
        String plateNo,        // 车牌号
        String phone,          // 手机号
        Instant createdAt,
        Instant updatedAt
) {
}
