package com.authforge.web.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class UserDto {
    private final String username;
    private final String firstName;
    private final String lastName;
    private final String phoneNumber;
    private final UUID uuid;
}
