package com.authforge.web.dto;

import java.util.UUID;

import com.authforge.data.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDto {
    private final String username;
    private final String firstName;
    private final String lastName;
    private final String phoneNumber;
    private final UUID uuid;
    private final Role role;

}
