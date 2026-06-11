package com.example.carphone.dto;

import jakarta.validation.constraints.NotBlank;

public final class VehicleDtos {
    private VehicleDtos() {
    }

    public record CreateStickerRequest(
            @NotBlank String profileId,
            @NotBlank String vehicleId,
            @NotBlank String templateId,
            String customImage,
            String comfortMessage,
            java.util.List<java.util.Map<String, Object>> layers
    ) {
    }

    public record StickerResponse(
            String id,
            String profileId,
            String vehicleId,
            String templateId,
            String customImage,
            String ownerName,
            String plateNo,
            String phone,
            String maskedPhone,
            String comfortMessage,
            String finalImageUrl,
            String qrCodeUrl,
            String publicUrl,
            String profileNickname,
            String profilePhone
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

    public record UploadImageResponse(
            String url
    ) {
    }
}
