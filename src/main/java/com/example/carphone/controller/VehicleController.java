package com.example.carphone.controller;

import com.example.carphone.dto.VehicleDtos.PublicVehicleResponse;
import com.example.carphone.dto.VehicleDtos.UpsertVehicleRequest;
import com.example.carphone.dto.VehicleDtos.VehicleResponse;
import com.example.carphone.service.AuthService;
import com.example.carphone.service.QrCodeService;
import com.example.carphone.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
        String openid = authService.requireOpenid(authorization);
        return vehicleService.create(openid, request);
    }

    @GetMapping("/vehicles")
    public List<VehicleResponse> listMine(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String openid = authService.requireOpenid(authorization);
        return vehicleService.listMine(openid);
    }

    @GetMapping("/profiles/{profileId}/stickers")
    public List<VehicleResponse> listByProfile(
            @PathVariable String profileId,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        authService.requireOpenid(authorization); // 验证登录
        return vehicleService.listByProfile(profileId);
    }

    @GetMapping(value = "/vehicles/{id}/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] qrcode(@PathVariable String id) {
        return qrCodeService.createCodeForVehicle(id);
    }

    @GetMapping("/public/vehicles/{id}")
    public PublicVehicleResponse publicInfo(@PathVariable String id) {
        return vehicleService.publicInfo(id);
    }

    @DeleteMapping("/vehicles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        String openid = authService.requireOpenid(authorization);
        vehicleService.delete(openid, id);
    }
}
