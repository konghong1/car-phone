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
    private static final String DEFAULT_COMFORT = "您好，给您添麻烦了。车主已开启挪车电话，请点击下方按钮联系车主，感谢您的理解。";

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

    public PublicVehicleResponse publicInfo(String id) {
        VehicleCard vehicle = store.findVehicle(id).orElseThrow(() -> new NotFoundException("车辆二维码不存在"));
        return new PublicVehicleResponse(
                vehicle.id(),
                vehicle.ownerName(),
                vehicle.plateNo(),
                maskPhone(vehicle.phone()),
                vehicle.phone(),
                vehicle.comfortMessage()
        );
    }

    private VehicleResponse toOwnerResponse(VehicleCard vehicle) {
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
