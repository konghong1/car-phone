package com.example.carphone.controller;

import com.example.carphone.model.Template;
import com.example.carphone.service.TemplateService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TemplateController {
    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    /**
     * 获取所有模板
     */
    @GetMapping("/templates")
    public List<Template> getAllTemplates(
            @RequestParam(required = false) String category
    ) {
        if (category != null && !category.isEmpty()) {
            return templateService.getTemplatesByCategory(category);
        }
        return templateService.getAllTemplates();
    }

    /**
     * 获取模板详情
     */
    @GetMapping("/templates/{id}")
    public Template getTemplate(@PathVariable String id) {
        Template template = templateService.getTemplateById(id);
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }
        return template;
    }

    /**
     * 获取所有分类
     */
    @GetMapping("/template-categories")
    public List<String> getCategories() {
        return templateService.getAllCategories();
    }
}
