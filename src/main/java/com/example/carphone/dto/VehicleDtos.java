package com.example.carphone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public final class VehicleDtos {
    private VehicleDtos() {
    }

    public record UpsertVehicleRequest(
            @NotBlank String ownerName,
            String plateNo,
            @NotBlank @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String phone,
            String comfortMessage
    ) {
    }

    public record VehicleResponse(
            String id,
            String ownerName,
            String plateNo,
            String phone,
            String maskedPhone,
            String comfortMessage,
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
