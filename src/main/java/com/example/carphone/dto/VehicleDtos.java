package com.example.carphone.dto;

import jakarta.validation.constraints.NotBlank;

public final class VehicleDtos {
    private VehicleDtos() {
    }

    public record UpsertVehicleRequest(
            @NotBlank String profileId,  // 车主档案ID（必填）
            @NotBlank String templateId, // 贴图模板ID（必填）
            String comfortMessage
    ) {
    }

    public record VehicleResponse(
            String id,
            String profileId,
            String templateId,
            String ownerName,
            String plateNo,
            String phone,
            String maskedPhone,
            String comfortMessage,
            String finalImageUrl,  // 最终合成的贴图URL
            String qrCodeUrl,
            String publicUrl
    ) {
    }

    public record PublicVehicleResponse(
            String id,
            String ownerName,
            String plateNo,
            String maskedPhone,
            String phone,
            String comfortMessage
    ) {
    }
}
