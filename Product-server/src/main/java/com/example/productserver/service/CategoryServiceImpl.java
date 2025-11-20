package com.example.productserver.service;

import com.example.productserver.data.dto.CategoryDto;
import com.example.productserver.data.entity.Category;
import com.example.productserver.data.repository.CategoryRepository;
import com.example.productserver.mapper.appMapper;
import com.example.productserver.service.ex.AlreadyException;
import com.example.productserver.service.ex.InvalidException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final appMapper mapper;
    private final CategoryRepository categoryRepository;

    @Override
    public CategoryDto addCategory(CategoryDto categoryDto) {
        if (categoryDto == null) {
            throw new InvalidException("CategoryDto is null");
        }

        if (categoryRepository.findByName(categoryDto.getName()).isPresent()) {
            throw new AlreadyException(
                    String.format("Category with name %s already exists", categoryDto.getName())
            );
        }

        // Map DTO → Entity
        Category categoryEntity = mapper.map(categoryDto);

        // Save entity
        Category savedCategory = categoryRepository.save(categoryEntity);

        // Map Entity → DTO
        return mapper.map(savedCategory);
    }


    @Override
    public CategoryDto updateCategory(String name, CategoryDto categoryDto) {
        if (categoryDto == null) {
            throw new InvalidException("CategoryDto is null");
        }

        Category existingCategory = categoryRepository.findByName(name)
                .orElseThrow(() -> new InvalidException("Category not found with name: " + name));

        // Update fields
        existingCategory.setName(categoryDto.getName());
        existingCategory.setImageUrl(categoryDto.getImageUrl());

        // Save the updated category
        Category updatedCategory = categoryRepository.save(existingCategory);

        // Return the mapped DTO
        return mapper.map(updatedCategory);
    }


    @Override
    public CategoryDto getCategory(String name) {
        return mapper.map(categoryRepository.findByName(name).orElseThrow(() -> new InvalidException("Category not found with name: " + name)));
    }

    @Override
    public List<CategoryDto> getCategories() {
        return categoryRepository.findAll().stream().map(mapper::map).collect(Collectors.toList());
    }

    @Override
    public void deleteCategory(String name) {
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() ->
                        new InvalidException
                                ("Category not found with name: " + name));
        categoryRepository.delete(category);


    }
}
