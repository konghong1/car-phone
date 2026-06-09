package com.example.carphone.repository;

import com.example.carphone.model.Owner;
import com.example.carphone.model.VehicleCard;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryStore {
    private final ConcurrentHashMap<String, Owner> owners = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, VehicleCard> vehicles = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> tokenToOwnerId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> openidToOwnerId = new ConcurrentHashMap<>();

    public Optional<Owner> findOwnerByOpenid(String openid) {
        String ownerId = openidToOwnerId.get(openid);
        return ownerId == null ? Optional.empty() : Optional.ofNullable(owners.get(ownerId));
    }

    public Owner saveOwner(Owner owner) {
        owners.put(owner.id(), owner);
        openidToOwnerId.put(owner.openid(), owner.id());
        return owner;
    }

    public void saveToken(String token, String ownerId) {
        tokenToOwnerId.put(token, ownerId);
    }

    public Optional<Owner> findOwnerByToken(String token) {
        String ownerId = tokenToOwnerId.get(token);
        return ownerId == null ? Optional.empty() : Optional.ofNullable(owners.get(ownerId));
    }

    public VehicleCard saveVehicle(VehicleCard vehicle) {
        vehicles.put(vehicle.id(), vehicle);
        return vehicle;
    }

    public Optional<VehicleCard> findVehicle(String id) {
        return Optional.ofNullable(vehicles.get(id));
    }

    public List<VehicleCard> findVehiclesByOwner(String ownerId) {
        return vehicles.values().stream()
                .filter(vehicle -> vehicle.ownerId().equals(ownerId))
                .sorted((left, right) -> right.updatedAt().compareTo(left.updatedAt()))
                .toList();
    }
}
