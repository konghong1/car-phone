package com.example.carphone.service;

import com.example.carphone.dto.VehicleDtos.PublicVehicleResponse;
import com.example.carphone.dto.VehicleDtos.UpsertVehicleRequest;
import com.example.carphone.dto.VehicleDtos.VehicleResponse;
import com.example.carphone.model.Owner;
import com.example.carphone.model.VehicleCard;
import com.example.carphone.repository.InMemoryStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class VehicleService {
    private static final String DEFAULT_COMFORT = "\u60a8\u597d\uff0c\u7ed9\u60a8\u6dfb\u9ebb\u7e66\u4e86\u3002\u8f66\u4e3b\u5df2\u5f00\u542f\u632f\u8f66\u7535\u8bdd\uff0c\u8bf7\u70b9\u51fb\u4e0b\u65b9\u6309\u94ae\u8054\u7cfb\u8f66\u4e3b\uff0c\u611f\u8c22\u60a8\u7684\u7406\u89e3\u3002";

    private final InMemoryStore store;
    private final QrCodeService qrCodeService;

    public VehicleService(InMemoryStore store, QrCodeService qrCodeService) {
        this.store = store;
        this.qrCodeService = qrCodeService;
    }

    public VehicleResponse create(Owner owner, UpsertVehicleRequest request) {
        Instant now = Instant.now();
        VehicleCard vehicle = new VehicleCard(
                UUID.randomUUID().toString(),
                owner.id(),
                request.ownerName(),
                normalizeBlank(request.plateNo()),
                request.phone(),
                comfort(request.comfortMessage()),
                now,
                now
        );
        return toOwnerResponse(store.saveVehicle(vehicle));
    }

    public List<VehicleResponse> listMine(Owner owner) {
        return store.findVehiclesByOwner(owner.id()).stream().map(this::toOwnerResponse).toList();
    }

    public VehicleCard findById(String id) {
        return store.findVehicle(id).orElseThrow(() -> new NotFoundException("\u8f66\u8f86\u4e8c\u7ef4\u7801\u4e0d\u5b58\u5728"));
    }

    public PublicVehicleResponse publicInfo(String id) {
        VehicleCard vehicle = findById(id);
        return new PublicVehicleResponse(
                vehicle.id(),
                vehicle.ownerName(),
                vehicle.plateNo(),
                maskPhone(vehicle.phone()),
                vehicle.phone(),
                vehicle.comfortMessage()
        );
    }

    public QrCodeService qrCodeService() {
        return qrCodeService;
    }

    public VehicleResponse toResponse(VehicleCard vehicle) {
        return new VehicleResponse(
                vehicle.id(),
                vehicle.ownerName(),
                vehicle.plateNo(),
                vehicle.phone(),
                maskPhone(vehicle.phone()),
                vehicle.comfortMessage(),
                "/api/vehicles/" + vehicle.id() + "/qrcode",
                qrCodeService.publicMoveCarUrl(vehicle.id())
        );
    }

    private VehicleResponse toOwnerResponse(VehicleCard vehicle) {
        return toResponse(vehicle);
    }

    private String comfort(String value) {
        String normalized = normalizeBlank(value);
        return normalized == null ? DEFAULT_COMFORT : normalized;
    }

    private String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return "";
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }
}