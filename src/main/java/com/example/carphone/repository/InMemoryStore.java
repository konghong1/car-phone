package com.example.carphone.repository;

import com.example.carphone.model.OwnerProfile;
import com.example.carphone.model.StickerCard;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryStore {
    private final ConcurrentHashMap<String, OwnerProfile> ownerProfiles = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, StickerCard> stickerCards = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> tokenToOpenid = new ConcurrentHashMap<>();

    // ==================== OwnerProfile 操作 ====================

    public Optional<OwnerProfile> findProfile(String id) {
        return Optional.ofNullable(ownerProfiles.get(id));
    }

    public List<OwnerProfile> findProfilesByOpenid(String openid) {
        return ownerProfiles.values().stream()
                .filter(profile -> profile.openid().equals(openid))
                .sorted((left, right) -> right.createdAt().compareTo(left.createdAt()))
                .toList();
    }

    public OwnerProfile saveProfile(OwnerProfile profile) {
        ownerProfiles.put(profile.id(), profile);
        return profile;
    }

    public void deleteProfile(String id) {
        ownerProfiles.remove(id);
        // 级联删除该档案创建的所有贴图
        stickerCards.values().removeIf(card -> card.profileId().equals(id));
    }

    // ==================== Token 操作 ====================

    public void saveToken(String token, String openid) {
        tokenToOpenid.put(token, openid);
    }

    public Optional<String> findOpenidByToken(String token) {
        return Optional.ofNullable(tokenToOpenid.get(token));
    }

    // ==================== StickerCard 操作 ====================

    public StickerCard saveStickerCard(StickerCard card) {
        stickerCards.put(card.id(), card);
        return card;
    }

    public Optional<StickerCard> findStickerCard(String id) {
        return Optional.ofNullable(stickerCards.get(id));
    }

    public List<StickerCard> findStickersByProfile(String profileId) {
        return stickerCards.values().stream()
                .filter(card -> card.profileId().equals(profileId))
                .sorted((left, right) -> right.createdAt().compareTo(left.createdAt()))
                .toList();
    }

    public List<StickerCard> findStickersByOpenid(String openid) {
        List<String> profileIds = ownerProfiles.values().stream()
                .filter(p -> p.openid().equals(openid))
                .map(OwnerProfile::id)
                .toList();

        return stickerCards.values().stream()
                .filter(card -> profileIds.contains(card.profileId()))
                .sorted((left, right) -> right.createdAt().compareTo(left.createdAt()))
                .toList();
    }

    public void deleteStickerCard(String id) {
        stickerCards.remove(id);
    }
}
