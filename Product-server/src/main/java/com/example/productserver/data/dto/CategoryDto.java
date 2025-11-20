package com.example.productserver.data.dto;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryDto {
    private Long id;

    private String name;

    private String imageUrl;

    private List<ProductDto> products = new ArrayList<>();

}
