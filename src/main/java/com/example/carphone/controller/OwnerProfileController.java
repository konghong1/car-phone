package com.example.carphone.controller;

import com.example.carphone.dto.AuthDtos.CreateProfileRequest;
import com.example.carphone.dto.AuthDtos.UpdateOwnerRequest;
import com.example.carphone.model.OwnerProfile;
import com.example.carphone.repository.InMemoryStore;
import com.example.carphone.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profiles")
public class OwnerProfileController {
    private final AuthService authService;
    private final InMemoryStore store;

    public OwnerProfileController(AuthService authService, InMemoryStore store) {
        this.authService = authService;
        this.store = store;
    }

    @GetMapping
    public List<OwnerProfile> listProfiles(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        String openid = authService.requireOpenid(authorization);
        return store.findProfilesByOpenid(openid);
    }

    @PostMapping
    public OwnerProfile createProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody CreateProfileRequest request
    ) {
        String openid = authService.requireOpenid(authorization);
        Instant now = Instant.now();

        List<OwnerProfile.VehicleInfo> vehicles = List.of();
        if (request.vehicles() != null && !request.vehicles().isEmpty()) {
            vehicles = request.vehicles().stream()
                    .map(v -> new OwnerProfile.VehicleInfo(
                            UUID.randomUUID().toString(),
                            v.plateNo().toUpperCase(),
                            v.phone(),
                            v.remark() != null ? v.remark() : "",
                            ""  // stickerId - generated on first sticker creation
                    ))
                    .toList();
        }

        OwnerProfile profile = new OwnerProfile(
                UUID.randomUUID().toString(),
                openid,
                request.nickname(),
                vehicles,
                now,
                now
        );

        return store.saveProfile(profile);
    }

    @PutMapping("/{id}")
    public OwnerProfile updateProfile(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody UpdateOwnerRequest request
    ) {
        String openid = authService.requireOpenid(authorization);
        OwnerProfile existing = store.findProfile(id)
                .orElseThrow(() -> new RuntimeException("档案不存在"));

        if (!existing.openid().equals(openid)) {
            throw new RuntimeException("无权修改此档案");
        }

        List<OwnerProfile.VehicleInfo> vehicles = List.of();
        if (request.vehicles() != null && !request.vehicles().isEmpty()) {
            vehicles = request.vehicles().stream()
                    .map(v -> {
                        String vehicleId = v.id();
                        if (vehicleId == null || vehicleId.isEmpty()) {
                            vehicleId = UUID.randomUUID().toString();
                        }
                        return new OwnerProfile.VehicleInfo(
                                vehicleId,
                                v.plateNo().toUpperCase(),
                                v.phone(),
                                "",
                                ""  // stickerId stays empty
                        );
                    })
                    .toList();
        }

        OwnerProfile updated = new OwnerProfile(
                existing.id(),
                existing.openid(),
                request.nickname() != null ? request.nickname() : existing.nickname(),
                vehicles.isEmpty() ? existing.vehicles() : vehicles,
                existing.createdAt(),
                Instant.now()
        );

        return store.saveProfile(updated);
    }

    @DeleteMapping("/{id}")
    public void deleteProfile(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        String openid = authService.requireOpenid(authorization);
        OwnerProfile existing = store.findProfile(id)
                .orElseThrow(() -> new RuntimeException("档案不存在"));

        if (!existing.openid().equals(openid)) {
            throw new RuntimeException("无权删除此档案");
        }

        store.deleteProfile(id);
    }
}
