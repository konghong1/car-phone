package com.example.carphone.controller;

import com.example.carphone.dto.VehicleDtos.UpsertVehicleRequest;
import com.example.carphone.dto.VehicleDtos.VehicleResponse;
import com.example.carphone.model.Owner;
import com.example.carphone.service.AuthService;
import com.example.carphone.service.QrCodeService;
import com.example.carphone.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class VehicleController {
    private final AuthService authService;
    private final VehicleService vehicleService;

    public VehicleController(AuthService authService, VehicleService vehicleService) {
        this.authService = authService;
        this.vehicleService = vehicleService;
    }

    @PostMapping("/vehicles")
    public VehicleResponse create(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody UpsertVehicleRequest request
    ) {
        Owner owner = authService.requireOwner(authorization);
        return vehicleService.create(owner, request);
    }

    // Demo endpoint: creates vehicle without auth (for web frontend)
    @PostMapping("/vehicles-demo")
    public VehicleResponse createDemo(@Valid @RequestBody UpsertVehicleRequest request) {
        Owner owner = new Owner(
                UUID.randomUUID().toString(),
                "demo-" + UUID.randomUUID().toString().substring(0, 8),
                request.ownerName(),
                Instant.now()
        );
        return vehicleService.create(owner, request);
    }

    @GetMapping("/vehicles")
    public List<VehicleResponse> listMine(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Owner owner = authService.requireOwner(authorization);
        return vehicleService.listMine(owner);
    }

    @GetMapping(value = "/vehicles/{id}/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] qrcode(@PathVariable String id) {
        QrCodeService qrCodeService = vehicleService.qrCodeService();
        return qrCodeService.createCodeForVehicle(id);
    }

    @GetMapping("/public/vehicles/{id}")
    public com.example.carphone.dto.VehicleDtos.PublicVehicleResponse publicInfo(@PathVariable String id) {
        return vehicleService.publicInfo(id);
    }
}