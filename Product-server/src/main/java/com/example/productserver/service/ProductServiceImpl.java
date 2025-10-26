package com.example.productserver.service;

import com.example.productserver.data.dto.ProductDto;
import com.example.productserver.data.repository.CategoryRepository;
import com.example.productserver.data.repository.ProductRepository;
import com.example.productserver.mapper.appMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final appMapper mapper;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    @Override
    public ProductDto save(ProductDto product) {
        return null;
    }

    @Override
    public ProductDto getProduct(String barcode) {
        return null;
    }

    @Override
    public List<ProductDto> getAllProducts() {
        return List.of();
    }

    @Override
    public List<ProductDto> getAllProductsByCategory(String categoryName) {
        return List.of();
    }

    @Override
    public void deleteProduct(String barcode) {

    }
}
