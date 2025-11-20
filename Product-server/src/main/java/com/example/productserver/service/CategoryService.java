package com.example.productserver.service;

import com.example.productserver.data.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto addCategory(CategoryDto category);
    CategoryDto updateCategory(String name, CategoryDto category);
    CategoryDto getCategory(String name);
    List<CategoryDto> getCategories();
    void deleteCategory(String name);
}
