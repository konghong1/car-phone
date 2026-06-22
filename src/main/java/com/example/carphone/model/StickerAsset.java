package com.example.carphone.model;

/**
 * 贴图素材库 - 表情包、装饰元素
 */
public record StickerAsset(
        String id,
        String name,        // 素材名称
        String imageUrl,    // 素材图片URL
        String category,    // 分类：emoji/animal/car/flower/other
        String type,        // 类型：emoji(文字型)/image(图片型)
        Integer width,      // 推荐宽度
        Integer height,     // 推荐高度
        Boolean isFree      // 是否免费
) {
}
