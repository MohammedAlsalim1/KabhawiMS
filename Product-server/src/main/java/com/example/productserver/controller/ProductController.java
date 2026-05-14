package com.example.productserver.controller;

import com.example.productserver.data.dto.ProductDto;
import com.example.productserver.service.ProductService;
import com.example.productserver.service.CloudinaryService; // 👈 لا تنسَ استيراد الخدمة
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;
    private final CloudinaryService cloudinaryService; // 👈 حقن خدمة الكلاوديناري

    // 👈 التعديل هنا: تحديد أننا نستقبل ملفات وبيانات
    @PostMapping(value = "/addProduct", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDto> addProduct(
            @RequestPart("product") ProductDto productDto,
            @RequestPart(value = "images", required = false) MultipartFile[] images) {

        try {
            // التحقق مما إذا كان هناك صور تم إرفاقها
            if (images != null && images.length > 0) {
                // 1. رفع الصور للسحابة والحصول على الروابط
                List<String> imageUrls = cloudinaryService.uploadMultipleImages(images);

                // 2. وضع قائمة الروابط داخل كائن المنتج (DTO)
                productDto.setImageUrl(imageUrls);
            }

            // 3. حفظ المنتج بالروابط الجديدة في قاعدة البيانات
            return ResponseEntity.ok(productService.save(productDto));

        } catch (Exception e) {
            // في حال فشل الرفع لأي سبب
            throw new RuntimeException("فشل في حفظ المنتج أو رفع الصور: " + e.getMessage());
        }
    }

    @GetMapping("/getAllProducts")
    public ResponseEntity<List<ProductDto>> getProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/getAllProductsByCategory/{categoryName}")
    public ResponseEntity<List<ProductDto>> getProductsBYCategory(@PathVariable String categoryName) {
        return ResponseEntity.ok(productService.getAllProductsByCategory(categoryName));
    }

    @GetMapping("/getProduct/{barcode}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable String barcode) {
        return ResponseEntity.ok(productService.getProduct(barcode));
    }

    @DeleteMapping("/deleteProduct/{barcode}")
    public void delete(@PathVariable String barcode) {
        productService.deleteProduct(barcode);
    }
}