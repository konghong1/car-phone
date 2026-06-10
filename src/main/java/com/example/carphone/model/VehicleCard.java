package com.example.carphone.model;

import java.time.Instant;

/**
 * 挪车卡片 - 基于车主档案生成的二维码贴图
 */
public record VehicleCard(
        String id,
        String profileId,      // 关联的车主档案ID
        String templateId,     // 使用的贴图模板ID
        String ownerName,      // 车主姓名（从profile复制）
        String plateNo,        // 车牌号（从profile复制）
        String phone,          // 电话（从profile复制）
        String comfortMessage, // 问候语
        String finalImageUrl,  // 最终合成的贴图URL
        Instant createdAt,
        Instant updatedAt
) {
}
