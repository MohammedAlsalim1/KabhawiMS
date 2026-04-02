package com.example.cartserver.client;

import com.example.cartserver.data.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-forge")

public interface UserClient {
    @GetMapping("/parse-token")
    UserDto getUser(@RequestHeader("Authorization") String token);
}
