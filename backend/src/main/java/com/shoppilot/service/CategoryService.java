package com.shoppilot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shoppilot.entity.Category;
import com.shoppilot.mapper.CategoryMapper;
import com.shoppilot.vo.CategoryOption;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public List<CategoryOption> listEnabledOptions() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, 1)
                        .orderByAsc(Category::getSortOrder)
                        .orderByDesc(Category::getId))
                .stream()
                .map(category -> new CategoryOption(category.getId(), category.getName()))
                .toList();
    }
}
