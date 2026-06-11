package com.example.carphone.service;

import com.example.carphone.model.Template;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TemplateService {
    private static final List<Template> TEMPLATES = List.of(
            new Template("tpl_001", "商务蓝", "商务",
                    "https://cdn.example.com/templates/business_blue.png",
                    "https://cdn.example.com/templates/business_blue_preview.png",
                    800, 1200, false, "preset"),

            new Template("tpl_002", "可爱粉", "可爱",
                    "https://cdn.example.com/templates/cute_pink.png",
                    "https://cdn.example.com/templates/cute_pink_preview.png",
                    800, 1200, false, "preset"),

            new Template("tpl_003", "简约白", "简约",
                    "https://cdn.example.com/templates/minimal_white.png",
                    "https://cdn.example.com/templates/minimal_white_preview.png",
                    800, 1200, false, "preset"),

            new Template("tpl_004", "卡通黄", "卡通",
                    "https://cdn.example.com/templates/cartoon_yellow.png",
                    "https://cdn.example.com/templates/cartoon_yellow_preview.png",
                    800, 1200, false, "preset"),

            new Template("tpl_005", "经典黑", "商务",
                    "https://cdn.example.com/templates/classic_black.png",
                    "https://cdn.example.com/templates/classic_black_preview.png",
                    800, 1200, false, "preset"),

            new Template("tpl_006", "清新绿", "简约",
                    "https://cdn.example.com/templates/fresh_green.png",
                    "https://cdn.example.com/templates/fresh_green_preview.png",
                    800, 1200, false, "preset"),

            new Template("tpl_007", "梦幻紫", "可爱",
                    "https://cdn.example.com/templates/dream_purple.png",
                    "https://cdn.example.com/templates/dream_purple_preview.png",
                    800, 1200, false, "preset"),

            new Template("tpl_008", "活力橙", "卡通",
                    "https://cdn.example.com/templates/energy_orange.png",
                    "https://cdn.example.com/templates/energy_orange_preview.png",
                    800, 1200, false, "preset")
    );

    public List<Template> getAllTemplates() {
        return TEMPLATES;
    }

    public List<Template> getTemplatesByCategory(String category) {
        if (category == null || category.isEmpty() || "全部".equals(category)) {
            return TEMPLATES;
        }
        return TEMPLATES.stream()
                .filter(t -> t.category().equals(category))
                .collect(Collectors.toList());
    }

    public List<Template> getPresetTemplates() {
        return TEMPLATES.stream()
                .filter(t -> "preset".equals(t.type()))
                .toList();
    }

    public Template getTemplateById(String id) {
        return TEMPLATES.stream()
                .filter(t -> t.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<String> getAllCategories() {
        return TEMPLATES.stream()
                .map(Template::category)
                .distinct()
                .collect(Collectors.toList());
    }
}
