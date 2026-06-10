package com.example.carphone.model;

/**
 * 挪车贴图模板
 */
public record Template(
        String id,
        String name,           // 模板名称
        String category,       // 分类：商务/可爱/简约/卡通
        String imageUrl,       // 背景图URL
        String previewUrl,     // 预览图URL
        Integer width,         // 宽度
        Integer height,        // 高度
        Boolean isPremium      // 是否付费
) {
}
