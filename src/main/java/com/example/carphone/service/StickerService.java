package com.example.carphone.service;

import com.example.carphone.dto.VehicleDtos.CreateStickerRequest;
import com.example.carphone.dto.VehicleDtos.PublicVehicleResponse;
import com.example.carphone.dto.VehicleDtos.StickerResponse;
import com.example.carphone.model.OwnerProfile;
import com.example.carphone.model.StickerCard;
import com.example.carphone.model.Template;
import com.example.carphone.repository.InMemoryStore;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class StickerService {
    private static final String DEFAULT_COMFORT = "您好，给您添麻烦了，车主已开启挪车电话，请点击下方按钮联系车主，感谢您的理解。";

    private final InMemoryStore store;
    private final QrCodeService qrCodeService;
    private final TemplateService templateService;
    private final ImageComposerService imageComposer;

    public StickerService(InMemoryStore store, QrCodeService qrCodeService, TemplateService templateService, ImageComposerService imageComposer) {
        this.store = store;
        this.qrCodeService = qrCodeService;
        this.templateService = templateService;
        this.imageComposer = imageComposer;
    }

    // ==================== 二维码管理 ====================

    /**
     * 为车辆生成/获取二维码ID
     */
    public String getOrCreateVehicleQrId(String profileId, String vehicleId) {
        OwnerProfile profile = store.findProfile(profileId)
                .orElseThrow(() -> new NotFoundException("车主档案不存在"));

        OwnerProfile.VehicleInfo vehicleInfo = findVehicleInProfile(profile, vehicleId);
        if (vehicleInfo == null) {
            throw new NotFoundException("车辆信息不存在");
        }

        // 如果车辆已有stickerId，直接返回
        if (vehicleInfo.stickerId() != null && !vehicleInfo.stickerId().isEmpty()) {
            return vehicleInfo.stickerId();
        }

        // 生成新的stickerId并更新profile
        String stickerId = UUID.randomUUID().toString();

        // 重新构建vehicles列表，更新二维码
        List<OwnerProfile.VehicleInfo> updatedVehicles = profile.vehicles().stream()
                .map(v -> v.id().equals(vehicleId)
                        ? new OwnerProfile.VehicleInfo(v.id(), v.plateNo(), v.phone(), v.remark(), stickerId)
                        : v)
                .toList();

        store.saveProfile(new OwnerProfile(
                profile.id(),
                profile.openid(),
                profile.nickname(),
                updatedVehicles,
                profile.createdAt(),
                Instant.now()
        ));

        return stickerId;
    }

    /**
     * 获取车辆的二维码
     */
    public byte[] getVehicleQrcode(String profileId, String vehicleId) {
        String stickerId = getOrCreateVehicleQrId(profileId, vehicleId);
        return qrCodeService.createCodeForVehicle(stickerId);
    }

    /**
     * 获取车辆二维码的公开链接
     */
    public String getVehicleQrUrl(String profileId, String vehicleId) {
        String stickerId = getOrCreateVehicleQrId(profileId, vehicleId);
        return qrCodeService.publicMoveCarUrl(stickerId);
    }

    /**
     * 通过 stickerId 获取二维码（兼容旧接口）
     */
    public byte[] getVehicleQrcodeByStickerId(String stickerId) {
        return qrCodeService.createCodeForVehicle(stickerId);
    }

    // ==================== 贴图制作 ====================

    /**
     * 编辑器提交最终贴图
     */
    public StickerResponse createFromEditor(String openid, String profileId, String vehicleId,
                                              MultipartFile image, String comfortMessage,
                                              List<java.util.Map<String, Object>> layers) {
        OwnerProfile profile = store.findProfile(profileId)
                .orElseThrow(() -> new NotFoundException("车主档案不存在"));

        if (!profile.openid().equals(openid)) {
            throw new ForbiddenException("无权使用此档案");
        }

        OwnerProfile.VehicleInfo vehicleInfo = findVehicleInProfile(profile, vehicleId);
        if (vehicleInfo == null) {
            throw new NotFoundException("车辆信息不存在");
        }

        // 获取/生成车辆二维码ID
        String stickerId = getOrCreateVehicleQrId(profileId, vehicleId);

        Instant now = Instant.now();

        // 生成二维码
        String qrCodeBase64 = null;
        try {
            byte[] qrBytes = qrCodeService.createCodeForVehicle(stickerId);
            qrCodeBase64 = Base64.getEncoder().encodeToString(qrBytes);
        } catch (Exception ignored) {
        }

        // 后端合成：用户拼图 + 二维码 + 问候语
        String finalImageUrl;
        try {
            byte[] composedBytes = imageComposer.composeStickerWithQrOverlays(
                    image.getBytes(),
                    profile.nickname(),
                    vehicleInfo.plateNo(),
                    maskPhone(vehicleInfo.phone()),
                    comfortMessage != null ? comfortMessage : "",
                    qrCodeBase64
            );
            finalImageUrl = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(composedBytes);
        } catch (Exception e) {
            try {
                finalImageUrl = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(image.getBytes());
            } catch (Exception ex) {
                finalImageUrl = "";
            }
        }

        StickerCard card = new StickerCard(
                stickerId,
                profile.id(),
                vehicleId,
                null,
                null,
                profile.nickname(),
                vehicleInfo.plateNo(),
                vehicleInfo.phone(),
                comfortMessage != null && !comfortMessage.isBlank() ? comfortMessage.trim() : "",
                finalImageUrl,
                now,
                now
        );

        return toOwnerResponse(store.saveStickerCard(card));
    }

    // ==================== 旧接口（兼容） ====================

    public StickerResponse create(String openid, CreateStickerRequest request) {
        OwnerProfile profile = store.findProfile(request.profileId())
                .orElseThrow(() -> new NotFoundException("车主档案不存在"));

        if (!profile.openid().equals(openid)) {
            throw new ForbiddenException("无权使用此档案");
        }

        OwnerProfile.VehicleInfo vehicleInfo = findVehicleInProfile(profile, request.vehicleId());
        if (vehicleInfo == null) {
            throw new NotFoundException("车辆信息不存在");
        }

        Template template = null;
        String customImage = request.customImage();
        if (customImage == null || customImage.isEmpty()) {
            template = templateService.getTemplateById(request.templateId());
            if (template == null) {
                throw new NotFoundException("贴图模板不存在");
            }
        }

        Instant now = Instant.now();
        String cardId = UUID.randomUUID().toString();

        String qrCodeBase64 = null;
        try {
            byte[] qrBytes = qrCodeService.createCodeForVehicle(cardId);
            qrCodeBase64 = Base64.getEncoder().encodeToString(qrBytes);
        } catch (Exception ex) {
        }

        String finalImageUrl = generateStickerUrl(cardId, template, customImage, vehicleInfo,
                profile.nickname(), request.comfortMessage(), qrCodeBase64, request.layers());

        StickerCard card = new StickerCard(
                cardId,
                profile.id(),
                request.vehicleId(),
                template != null ? template.id() : null,
                customImage,
                profile.nickname(),
                vehicleInfo.plateNo(),
                vehicleInfo.phone(),
                comfort(request.comfortMessage()),
                finalImageUrl,
                now,
                now
        );

        return toOwnerResponse(store.saveStickerCard(card));
    }

    public List<StickerResponse> listByProfile(String profileId) {
        return store.findStickersByProfile(profileId).stream()
                .map(this::toOwnerResponse)
                .toList();
    }

    public List<StickerResponse> listMine(String openid) {
        return store.findStickersByOpenid(openid).stream()
                .map(this::toOwnerResponse)
                .toList();
    }

    public StickerResponse getById(String openid, String stickerId) {
        StickerCard card = store.findStickerCard(stickerId)
                .orElseThrow(() -> new NotFoundException("贴图不存在"));
        if (!isOwner(card, openid)) {
            throw new ForbiddenException("无权查看此贴图");
        }
        return toOwnerResponse(card);
    }

    public void delete(String openid, String id) {
        StickerCard card = store.findStickerCard(id)
                .orElseThrow(() -> new NotFoundException("贴图不存在"));
        if (!isOwner(card, openid)) {
            throw new ForbiddenException("无权删除此贴图");
        }
        store.deleteStickerCard(id);
    }

    public PublicVehicleResponse publicInfo(String id) {
        StickerCard card = store.findStickerCard(id).orElseThrow(() -> new NotFoundException("贴图不存在"));
        return new PublicVehicleResponse(
                card.id(),
                card.ownerName(),
                card.plateNo(),
                maskPhone(card.phone()),
                card.phone(),
                card.comfortMessage()
        );
    }

    // ==================== 私有方法 ====================

    private OwnerProfile.VehicleInfo findVehicleInProfile(OwnerProfile profile, String vehicleId) {
        if (profile.vehicles() == null) return null;
        return profile.vehicles().stream()
                .filter(v -> v.id().equals(vehicleId))
                .findFirst()
                .orElse(null);
    }

    private boolean isOwner(StickerCard card, String openid) {
        OwnerProfile profile = store.findProfile(card.profileId())
                .orElse(null);
        return profile != null && profile.openid().equals(openid);
    }

    private StickerResponse toOwnerResponse(StickerCard card) {
        OwnerProfile profile = store.findProfile(card.profileId()).orElse(null);
        String profileNickname = profile != null ? profile.nickname() : "";
        String profilePhone = profile != null ? (profile.vehicles() != null ? profile.vehicles().get(0).phone() : "") : "";

        return new StickerResponse(
                card.id(),
                card.profileId(),
                card.vehicleId(),
                card.templateId(),
                card.customImage(),
                card.ownerName(),
                card.plateNo(),
                card.phone(),
                maskPhone(card.phone()),
                card.comfortMessage(),
                card.finalImageUrl(),
                "/api/stickers/" + card.id() + "/qrcode",
                qrCodeService.publicMoveCarUrl(card.id()),
                profileNickname,
                profilePhone
        );
    }

    private String generateStickerUrl(String cardId, Template template, String customImage,
                                        OwnerProfile.VehicleInfo vehicleInfo, String ownerName,
                                        String comfortMessage, String qrCodeBase64,
                                        List<java.util.Map<String, Object>> layers) {
        try {
            String baseUrl = customImage != null && !customImage.isEmpty()
                    ? customImage
                    : (template != null ? template.imageUrl() : null);
            Integer width = template != null ? template.width() : 800;
            Integer height = template != null ? template.height() : 1200;

            byte[] composed = imageComposer.composeSticker(
                    baseUrl, ownerName, vehicleInfo.plateNo(),
                    maskPhone(vehicleInfo.phone()), comfortMessage,
                    qrCodeBase64, layers, width, height
            );

            return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(composed);
        } catch (Exception e) {
            if (customImage != null && !customImage.isEmpty()) {
                return customImage + "?card=" + cardId;
            }
            if (template != null) {
                return template.imageUrl() + "?card=" + cardId;
            }
            return "";
        }
    }

    private String comfort(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_COMFORT;
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

    public static class ForbiddenException extends RuntimeException {
        public ForbiddenException(String message) {
            super(message);
        }
    }
}
