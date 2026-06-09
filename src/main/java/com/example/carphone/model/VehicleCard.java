package com.example.carphone.model;

import java.time.Instant;

public record VehicleCard(
        String id,
        String ownerId,
        String ownerName,
        String plateNo,
        String phone,
        String comfortMessage,
        Instant createdAt,
        Instant updatedAt
) {
}
