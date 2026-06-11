package com.example.carphone.service;

import com.example.carphone.model.StickerAsset;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StickerAssetService {
    private static final List<StickerAsset> ASSETS = List.of(
            // emoji文字型
            new StickerAsset("ast_001", "微笑", "", "emoji", "emoji", 48, 48, true),
            new StickerAsset("ast_002", "爱心", "", "emoji", "emoji", 48, 48, true),
            new StickerAsset("ast_003", "星星", "", "emoji", "emoji", 48, 48, true),
            new StickerAsset("ast_004", "花朵", "", "emoji", "emoji", 48, 48, true),
            new StickerAsset("ast_005", "闪电", "", "emoji", "emoji", 48, 48, true),
            new StickerAsset("ast_006", "彩虹", "", "emoji", "emoji", 48, 48, true),
            new StickerAsset("ast_007", "月亮", "", "emoji", "emoji", 48, 48, true),
            new StickerAsset("ast_008", "太阳", "", "emoji", "emoji", 48, 48, true),
            new StickerAsset("ast_009", "火焰", "", "emoji", "emoji", 48, 48, true),
            new StickerAsset("ast_010", "钻石", "", "emoji", "emoji", 48, 48, true),
            // 动物型
            new StickerAsset("ast_011", "小猫", "", "animal", "emoji", 64, 64, true),
            new StickerAsset("ast_012", "小狗", "", "animal", "emoji", 64, 64, true),
            new StickerAsset("ast_013", "小鸟", "", "animal", "emoji", 64, 64, true),
            new StickerAsset("ast_014", "小鱼", "", "animal", "emoji", 64, 64, true),
            // 车型
            new StickerAsset("ast_015", "轿车", "", "car", "emoji", 80, 60, true),
            new StickerAsset("ast_016", "跑车", "", "car", "emoji", 80, 60, true),
            new StickerAsset("ast_017", "卡车", "", "car", "emoji", 80, 60, true),
            // 自然型
            new StickerAsset("ast_018", "樱花", "", "flower", "emoji", 56, 56, true),
            new StickerAsset("ast_019", "枫叶", "", "flower", "emoji", 56, 56, true),
            new StickerAsset("ast_020", "雪花", "", "flower", "emoji", 56, 56, true)
    );

    public List<StickerAsset> getAllAssets() {
        return ASSETS;
    }

    public List<StickerAsset> getAssetsByCategory(String category) {
        if (category == null || category.isEmpty()) {
            return ASSETS;
        }
        return ASSETS.stream()
                .filter(a -> a.category().equals(category))
                .collect(Collectors.toList());
    }

    public List<String> getAllCategories() {
        return List.of("全部", "emoji", "animal", "car", "flower");
    }

    public StickerAsset getAssetById(String id) {
        return ASSETS.stream()
                .filter(a -> a.id().equals(id))
                .findFirst()
                .orElse(null);
    }
}
