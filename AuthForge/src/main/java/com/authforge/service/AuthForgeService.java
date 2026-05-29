package com.authforge.service;

import com.authforge.web.dto.UserDto;

import jakarta.security.auth.message.AuthException;

import java.util.List;

public interface AuthForgeService {
    String authenticateAndGenerateToken(String username, String password) throws AuthException;

    UserDto parseTokenAndGetUser(String token);

    boolean signUp(String username, String password,String firstName,String lastName,String phoneNumber);

    List<UserDto> getAllUsers();
}
