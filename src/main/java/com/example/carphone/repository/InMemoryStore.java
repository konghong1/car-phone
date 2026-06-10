package com.example.carphone.repository;

import com.example.carphone.model.OwnerProfile;
import com.example.carphone.model.VehicleCard;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryStore {
    private final ConcurrentHashMap<String, OwnerProfile> ownerProfiles = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, VehicleCard> vehicleCards = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> tokenToOpenid = new ConcurrentHashMap<>();

    // ========== OwnerProfile 操作 ==========
    
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
        // 同时删除该档案创建的所有卡片
        vehicleCards.values().removeIf(card -> card.profileId().equals(id));
    }

    // ========== Token 操作 ==========
    
    public void saveToken(String token, String openid) {
        tokenToOpenid.put(token, openid);
    }

    public Optional<String> findOpenidByToken(String token) {
        return Optional.ofNullable(tokenToOpenid.get(token));
    }

    // ========== VehicleCard 操作 ==========
    
    public VehicleCard saveVehicleCard(VehicleCard card) {
        vehicleCards.put(card.id(), card);
        return card;
    }

    public Optional<VehicleCard> findVehicleCard(String id) {
        return Optional.ofNullable(vehicleCards.get(id));
    }

    public List<VehicleCard> findCardsByProfile(String profileId) {
        return vehicleCards.values().stream()
                .filter(card -> card.profileId().equals(profileId))
                .sorted((left, right) -> right.createdAt().compareTo(left.createdAt()))
                .toList();
    }

    public List<VehicleCard> findCardsByOpenid(String openid) {
        // 先找到该openid的所有profile
        List<String> profileIds = ownerProfiles.values().stream()
                .filter(p -> p.openid().equals(openid))
                .map(OwnerProfile::id)
                .toList();
        
        return vehicleCards.values().stream()
                .filter(card -> profileIds.contains(card.profileId()))
                .sorted((left, right) -> right.createdAt().compareTo(left.createdAt()))
                .toList();
    }

    public void deleteVehicleCard(String id) {
        vehicleCards.remove(id);
    }
}
