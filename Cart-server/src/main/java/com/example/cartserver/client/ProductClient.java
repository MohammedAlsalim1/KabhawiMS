package com.example.cartserver.client;

import com.example.cartserver.data.dto.productDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-server")
public interface ProductClient {

    @GetMapping("/api/getProduct/{barcode}")
    productDto getProduct(@PathVariable String barcode);
}
