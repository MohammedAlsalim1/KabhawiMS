package com.example.productserver.controller;

import com.example.productserver.data.dto.CategoryDto;
import com.example.productserver.service.CategoryService;
import com.example.productserver.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final CloudinaryService cloudinaryService; // 👈 حقن الخدمة

    // 👈 تعديل دالة الإضافة لتستقبل صورة
    @PostMapping(value = "/addCategory", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CategoryDto> addCategory(
            @RequestPart("category") CategoryDto categoryDto,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        try {
            // التحقق من وجود الصورة ورفعها
            if (image != null && !image.isEmpty()) {
                // 💡 ملاحظة: تأكد أن لديك دالة في CloudinaryService ترفع صورة واحدة وترجع String
                String imageUrl = cloudinaryService.uploadImage(image);
                categoryDto.setImageUrl(imageUrl);
            }
            return ResponseEntity.ok(categoryService.addCategory(categoryDto));
        } catch (Exception e) {
            throw new RuntimeException("فشل في حفظ القسم أو رفع الصورة: " + e.getMessage());
        }
    }

    // 👈 تعديل دالة التحديث لتستقبل صورة أيضاً
    @PutMapping(value = "/updateCategory/{categoryName}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable String categoryName,
            @RequestPart("category") CategoryDto categoryDto,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        try {
            if (image != null && !image.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(image);
                categoryDto.setImageUrl(imageUrl);
            }
            return ResponseEntity.ok(categoryService.updateCategory(categoryName, categoryDto));
        } catch (Exception e) {
            throw new RuntimeException("فشل في تحديث القسم: " + e.getMessage());
        }
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
