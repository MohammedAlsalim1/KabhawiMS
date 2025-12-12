package com.example.productserver.service;

import com.example.productserver.data.dto.ProductDto;
import com.example.productserver.data.entity.Category;
import com.example.productserver.data.entity.Product;
import com.example.productserver.data.repository.CategoryRepository;
import com.example.productserver.data.repository.ProductRepository;
import com.example.productserver.mapper.appMapper;
import com.example.productserver.service.ex.AlreadyException;
import com.example.productserver.service.ex.InvalidException;
import com.example.productserver.service.ex.NotExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final appMapper mapper;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public ProductDto save(ProductDto productDto) {
        if (productDto == null) {
            throw new InvalidException("Product is null");
        }
        if (productRepository.findByBarcode(productDto.getBarcode()).isPresent()) {
            throw new AlreadyException("Barcode already exists");
        }
        Category category = categoryRepository.findById(productDto.getCategoryId())
                .orElseThrow(() -> new NotExistException("Category not found"));

        // 3. Map DTO to entity
        Product product = mapper.map(productDto);

        // 4. Set the category on the product
        product.setCategory(category);

        // 5. Save the product
        Product savedProduct = productRepository.save(product);

        // 6. Return mapped DTO
        return mapper.map(savedProduct);
    }

    @Override
    public ProductDto getProduct(String barcode) {
        if (barcode == null) {
            throw new InvalidException("Barcode is null");
        }

        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new NotExistException("Product does not exist"));

        return mapper.map(product);
    }

    @Override
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(mapper::map)
                .collect(Collectors.toList());

    }

    @Override
    public List<ProductDto> getAllProductsByCategory(String categoryName) {
        return productRepository.findByCategoryName(categoryName).get()
                .stream()
                .map(mapper::map)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteProduct(String barcode) {
        if (barcode == null) {
            throw new InvalidException("Barcode is null");
        }
        if (productRepository.findByBarcode(barcode).isEmpty()) {
            throw new NotExistException("product does not exist");
        }
        productRepository.delete(productRepository.findByBarcode(barcode).get());

    }
}
