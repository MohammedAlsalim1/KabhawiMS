package com.example.productserver.mapper;


import com.example.productserver.data.dto.CategoryDto;
import com.example.productserver.data.dto.ProductDto;
import com.example.productserver.data.entity.Category;
import com.example.productserver.data.entity.Product;
import org.mapstruct.Mapper;
@Mapper(componentModel = "spring")

public interface appMapper {
    ProductDto map (Product product);
    Product map (ProductDto productDto);
    CategoryDto map (Category category);
    Category map (CategoryDto categoryDto);

}
