package com.example.productserver.service;

import com.example.productserver.data.dto.ProductDto;

import java.util.List;

public interface ProductService {
    ProductDto save(ProductDto product);
    ProductDto getProduct(String barcode);
    ProductDto updateProduct(String barcode,ProductDto productDto);
    List<ProductDto> getAllProducts();
    List<ProductDto> getAllProductsByCategory(String categoryName);
    void deleteProduct(String barcode);

}
