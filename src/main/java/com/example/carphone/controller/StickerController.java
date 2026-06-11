package com.example.carphone.controller;

import com.example.carphone.dto.VehicleDtos.CreateStickerRequest;
import com.example.carphone.dto.VehicleDtos.PublicVehicleResponse;
import com.example.carphone.dto.VehicleDtos.StickerResponse;
import com.example.carphone.service.AuthService;
import com.example.carphone.service.StickerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StickerController {
    private final AuthService authService;
    private final StickerService stickerService;

    public StickerController(AuthService authService, StickerService stickerService) {
        this.authService = authService;
        this.stickerService = stickerService;
    }

    // ==================== 贴图 CRUD ====================

    @PostMapping("/stickers")
    public StickerResponse createSticker(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody CreateStickerRequest request
    ) {
        String openid = authService.requireOpenid(authorization);
        return stickerService.create(openid, request);
    }

    @GetMapping("/stickers")
    public List<StickerResponse> listMine(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String openid = authService.requireOpenid(authorization);
        return stickerService.listMine(openid);
    }

    @GetMapping("/profiles/{profileId}/stickers")
    public List<StickerResponse> listByProfile(
            @PathVariable String profileId,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        authService.requireOpenid(authorization);
        return stickerService.listByProfile(profileId);
    }

    @GetMapping("/stickers/{id}")
    public StickerResponse getSticker(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        String openid = authService.requireOpenid(authorization);
        return stickerService.getById(openid, id);
    }

    @DeleteMapping("/stickers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSticker(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        String openid = authService.requireOpenid(authorization);
        stickerService.delete(openid, id);
    }

    // ==================== 二维码管理 ====================

    // 新接口：通过 profileId + vehicleId 获取二维码
    @GetMapping(value = "/profiles/{profileId}/vehicles/{vehicleId}/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getVehicleQrcode(@PathVariable String profileId, @PathVariable String vehicleId) {
        return stickerService.getVehicleQrcode(profileId, vehicleId);
    }

    // 兼容旧接口：通过 stickerId 获取二维码
    @GetMapping(value = "/stickers/{id}/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getStickerQrcode(@PathVariable String id) {
        return stickerService.getVehicleQrcodeByStickerId(id);
    }

    @GetMapping("/profiles/{profileId}/vehicles/{vehicleId}/qrcode-info")
    public java.util.Map<String, Object> getVehicleQrInfo(@PathVariable String profileId, @PathVariable String vehicleId) {
        String qrUrl = stickerService.getVehicleQrUrl(profileId, vehicleId);
        java.util.Map<String, Object> info = new java.util.HashMap<>();
        info.put("stickerId", stickerService.getOrCreateVehicleQrId(profileId, vehicleId));
        info.put("qrcodeUrl", "/api/profiles/" + profileId + "/vehicles/" + vehicleId + "/qrcode");
        info.put("publicUrl", qrUrl);
        return info;
    }

    // ==================== 公开接口 ====================

    @GetMapping("/public/stickers/{id}")
    public PublicVehicleResponse publicInfo(@PathVariable String id) {
        return stickerService.publicInfo(id);
    }

    // ==================== 贴图制作（编辑器直接提交） ====================

    @PostMapping(value = "/stickers/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public StickerResponse createStickerFromEditor(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("image") MultipartFile file,
            @RequestParam("profileId") String profileId,
            @RequestParam("vehicleId") String vehicleId,
            @RequestParam(value = "comfortMessage", required = false, defaultValue = "") String comfortMessage
    ) {
        String openid = authService.requireOpenid(authorization);
        return stickerService.createFromEditor(openid, profileId, vehicleId, file, comfortMessage, null);
    }
}
