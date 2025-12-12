package com.example.productserver.controller;

import com.example.productserver.data.dto.ProductDto;
import com.example.productserver.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product")
public class ProductController {
    private final ProductService productService;
    @PostMapping("/addProduct")
    public ResponseEntity<ProductDto> addProduct(@RequestBody ProductDto productDto) {
        return ResponseEntity.ok(productService.save(productDto));
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
