package com.example.productserver.data.repository;

import com.example.productserver.data.entity.Category;
import com.example.productserver.data.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByBarcode(String barcode);

    Optional<List<Product>> findByCategoryName(String categoryName);
}
