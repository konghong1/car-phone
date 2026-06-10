package com.example.carphone.service;

import com.example.carphone.dto.VehicleDtos.PublicVehicleResponse;
import com.example.carphone.dto.VehicleDtos.UpsertVehicleRequest;
import com.example.carphone.dto.VehicleDtos.VehicleResponse;
import com.example.carphone.model.OwnerProfile;
import com.example.carphone.model.Template;
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
    private final TemplateService templateService;

    public VehicleService(InMemoryStore store, QrCodeService qrCodeService, TemplateService templateService) {
        this.store = store;
        this.qrCodeService = qrCodeService;
        this.templateService = templateService;
    }

    public VehicleResponse create(String openid, UpsertVehicleRequest request) {
        // 获取车主档案
        OwnerProfile profile = store.findProfile(request.profileId())
                .orElseThrow(() -> new NotFoundException("车主档案不存在"));
        
        if (!profile.openid().equals(openid)) {
            throw new ForbiddenException("无权使用此档案");
        }
        
        // 获取模板
        Template template = templateService.getTemplateById(request.templateId());
        if (template == null) {
            throw new NotFoundException("贴图模板不存在");
        }
        
        Instant now = Instant.now();
        
        // 生成二维码URL
        String cardId = UUID.randomUUID().toString();
        
        // 生成最终贴图URL（实际应该合成图片，这里简化处理）
        String finalImageUrl = generateStickerUrl(cardId, template);
        
        // 从profile复制信息创建卡片
        VehicleCard card = new VehicleCard(
                cardId,
                profile.id(),           // 关联档案ID
                template.id(),          // 模板ID
                profile.nickname(),     // 从档案复制昵称
                profile.plateNo(),      // 从档案复制车牌号
                profile.phone(),        // 从档案复制电话
                comfort(request.comfortMessage()),
                finalImageUrl,          // 最终贴图URL
                now,
                now
        );
        
        return toOwnerResponse(store.saveVehicleCard(card));
    }

    public List<VehicleResponse> listByProfile(String profileId) {
        return store.findCardsByProfile(profileId).stream()
                .map(this::toOwnerResponse)
                .toList();
    }

    public List<VehicleResponse> listMine(String openid) {
        return store.findCardsByOpenid(openid).stream()
                .map(this::toOwnerResponse)
                .toList();
    }

    public void delete(String openid, String id) {
        VehicleCard card = store.findVehicleCard(id)
                .orElseThrow(() -> new NotFoundException("车辆二维码不存在"));
        
        // 验证权限
        OwnerProfile profile = store.findProfile(card.profileId())
                .orElseThrow(() -> new NotFoundException("车主档案不存在"));
        
        if (!profile.openid().equals(openid)) {
            throw new ForbiddenException("无权删除此车辆");
        }
        
        store.deleteVehicleCard(id);
    }

    public PublicVehicleResponse publicInfo(String id) {
        VehicleCard card = store.findVehicleCard(id).orElseThrow(() -> new NotFoundException("车辆二维码不存在"));
        return new PublicVehicleResponse(
                card.id(),
                card.ownerName(),
                card.plateNo(),
                maskPhone(card.phone()),
                card.phone(),
                card.comfortMessage()
        );
    }

    private VehicleResponse toOwnerResponse(VehicleCard card) {
        return new VehicleResponse(
                card.id(),
                card.profileId(),
                card.templateId(),
                card.ownerName(),
                card.plateNo(),
                card.phone(),
                maskPhone(card.phone()),
                card.comfortMessage(),
                card.finalImageUrl(),
                "/api/vehicles/" + card.id() + "/qrcode",
                qrCodeService.publicMoveCarUrl(card.id())
        );
    }

    /**
     * 生成贴图URL（简化版，实际应该合成图片）
     */
    private String generateStickerUrl(String cardId, Template template) {
        // TODO: 实际应该合成背景图 + 二维码 + 文字
        // 这里返回模板URL作为示例
        return template.imageUrl() + "?card=" + cardId;
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

    public static class ForbiddenException extends RuntimeException {
        public ForbiddenException(String message) {
            super(message);
        }
    }
}
