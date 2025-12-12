package com.example.productserver.controller;

import com.example.productserver.data.dto.CategoryDto;
import com.example.productserver.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    @PostMapping("/addCategory")
    public ResponseEntity<CategoryDto> addCategory(@RequestBody CategoryDto categoryDto) {
        return ResponseEntity.ok(categoryService.addCategory(categoryDto));
    }
    @PutMapping("/updateCategory/{categoryName}")
    public ResponseEntity<CategoryDto> updateCategory(@RequestBody CategoryDto categoryDto, @PathVariable String categoryName) {
        return ResponseEntity.ok(categoryService.updateCategory(categoryName,categoryDto));
    }

    @GetMapping("/getCategory/{categoryName}")
    public ResponseEntity<CategoryDto> getCategoryDto(@PathVariable String categoryName) {
        return ResponseEntity.ok(categoryService.getCategory(categoryName));

    }

    @GetMapping("/getCategories")
    public ResponseEntity<List<CategoryDto>> getCategories() {
        return ResponseEntity.ok(categoryService.getCategories());
    }

    @DeleteMapping("/deleteCategory/{categoryName}")
    public void delete(@PathVariable String categoryName) {
        categoryService.deleteCategory(categoryName);
    }
}
