package com.example.carphone.model;

import java.time.Instant;
import java.util.List;

/**
 * 车主档案 - 包含完整的挪车信息，支持多车牌
 */
public record OwnerProfile(
        String id,
        String openid,
        String nickname,
        List<VehicleInfo> vehicles,
        Instant createdAt,
        Instant updatedAt
) {
    /**
     * 车辆信息（一个档案可以有多个车牌）
     */
    public record VehicleInfo(
            String id,
            String plateNo,
            String phone,
            String remark,
            String stickerId  // 关联的贴图ID，用于绑定二维码
    ) {
    }
}
