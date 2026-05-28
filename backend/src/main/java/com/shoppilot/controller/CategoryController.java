package com.shoppilot.controller;

import com.shoppilot.common.ApiResponse;
import com.shoppilot.service.CategoryService;
import com.shoppilot.vo.CategoryOption;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/options")
    public ApiResponse<List<CategoryOption>> options() {
        return ApiResponse.success(categoryService.listEnabledOptions());
    }
}
