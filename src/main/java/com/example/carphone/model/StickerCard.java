package com.example.carphone.model;

import java.time.Instant;

/**
 * 挪车贴图 - 基于车主档案生成的二维码贴图成品
 */
public record StickerCard(
        String id,
        String profileId,      // 关联的车主档案ID
        String vehicleId,      // 具体的车辆信息ID（从档案中选择）
        String templateId,     // 使用的贴图模板ID
        String customImage,    // 用户自定义图片URL（可选，优先级高于模板）
        String ownerName,      // 车主姓名（从profile复制）
        String plateNo,        // 车牌号（从profile复制）
        String phone,          // 电话（从profile复制）
        String comfortMessage, // 问候语
        String finalImageUrl,  // 最终合成的贴图URL
        Instant createdAt,
        Instant updatedAt
) {
}
