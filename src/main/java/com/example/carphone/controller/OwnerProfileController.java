package com.example.carphone.controller;

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

    /**
     * 获取当前用户的所有车主档案
     */
    @GetMapping
    public List<OwnerProfile> listProfiles(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        String openid = authService.requireOpenid(authorization);
        return store.findProfilesByOpenid(openid);
    }

    /**
     * 创建新的车主档案（昵称+车牌+手机号）
     */
    @PostMapping
    public OwnerProfile createProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody UpdateOwnerRequest request
    ) {
        String openid = authService.requireOpenid(authorization);
        
        Instant now = Instant.now();
        OwnerProfile profile = new OwnerProfile(
                UUID.randomUUID().toString(),
                openid,
                request.nickname() != null ? request.nickname() : "",
                request.plateNo() != null ? request.plateNo() : "",
                request.phone() != null ? request.phone() : "",
                now,
                now
        );
        
        return store.saveProfile(profile);
    }

    /**
     * 更新车主档案
     */
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
        
        OwnerProfile updated = new OwnerProfile(
                existing.id(),
                existing.openid(),
                request.nickname() != null ? request.nickname() : existing.nickname(),
                request.plateNo() != null ? request.plateNo() : existing.plateNo(),
                request.phone() != null ? request.phone() : existing.phone(),
                existing.createdAt(),
                Instant.now()
        );
        
        return store.saveProfile(updated);
    }

    /**
     * 删除车主档案
     */
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
