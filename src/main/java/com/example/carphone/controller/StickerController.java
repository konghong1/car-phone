package com.example.carphone.controller;

import com.example.carphone.dto.VehicleDtos.VehicleResponse;
import com.example.carphone.model.VehicleCard;
import com.example.carphone.renderer.StickerRenderer;
import com.example.carphone.service.VehicleService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class StickerController {

    private final VehicleService vehicleService;
    private final StickerRenderer stickerRenderer;

    public StickerController(VehicleService vehicleService, StickerRenderer stickerRenderer) {
        this.vehicleService = vehicleService;
        this.stickerRenderer = stickerRenderer;
    }

    @GetMapping(value = "/stickers/{id}", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] renderSticker(
            @PathVariable String id,
            @RequestParam(defaultValue = "600") int width,
            @RequestParam(defaultValue = "800") int height
    ) {
        VehicleCard vehicle = vehicleService.findById(id);
        VehicleResponse response = vehicleService.toResponse(vehicle);
        try {
            return stickerRenderer.renderSticker(response, width, height);
        } catch (Exception e) {
            throw new RuntimeException("Failed to render sticker", e);
        }
    }
}