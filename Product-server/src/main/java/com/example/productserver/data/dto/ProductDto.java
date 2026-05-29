package com.example.productserver.data.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductDto {
    private String name;
    private String description;
    private double price;
    private String barcode ;
    private int quantity;
    private Long categoryId;
    private double weight;
    private List<String> materials;
    private List<String> imageUrl;
}
