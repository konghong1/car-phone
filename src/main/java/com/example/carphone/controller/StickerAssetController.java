package com.example.carphone.controller;

import com.example.carphone.model.StickerAsset;
import com.example.carphone.service.StickerAssetService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class StickerAssetController {
    private final StickerAssetService assetService;

    public StickerAssetController(StickerAssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping
    public List<StickerAsset> getAllAssets(
            @RequestParam(required = false) String category
    ) {
        if (category != null && !category.isEmpty()) {
            return assetService.getAssetsByCategory(category);
        }
        return assetService.getAllAssets();
    }

    @GetMapping("/categories")
    public List<String> getCategories() {
        return assetService.getAllCategories();
    }
}
