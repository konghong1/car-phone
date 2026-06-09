package com.example.carphone.controller;

import com.example.carphone.dto.VehicleDtos.PublicVehicleResponse;
import com.example.carphone.dto.VehicleDtos.UpsertVehicleRequest;
import com.example.carphone.dto.VehicleDtos.VehicleResponse;
import com.example.carphone.model.Owner;
import com.example.carphone.service.AuthService;
import com.example.carphone.service.QrCodeService;
import com.example.carphone.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class VehicleController {
    private final AuthService authService;
    private final VehicleService vehicleService;
    private final QrCodeService qrCodeService;

    public VehicleController(AuthService authService, VehicleService vehicleService, QrCodeService qrCodeService) {
        this.authService = authService;
        this.vehicleService = vehicleService;
        this.qrCodeService = qrCodeService;
    }

    @PostMapping("/vehicles")
    public VehicleResponse create(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody UpsertVehicleRequest request
    ) {
        Owner owner = authService.requireOwner(authorization);
        return vehicleService.create(owner, request);
    }

    @GetMapping("/vehicles")
    public List<VehicleResponse> listMine(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Owner owner = authService.requireOwner(authorization);
        return vehicleService.listMine(owner);
    }

    @GetMapping(value = "/vehicles/{id}/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] qrcode(@PathVariable String id) {
        return qrCodeService.createCodeForVehicle(id);
    }

    @GetMapping("/public/vehicles/{id}")
    public PublicVehicleResponse publicInfo(@PathVariable String id) {
        return vehicleService.publicInfo(id);
    }
}
