package com.authforge.service;

import com.authforge.data.entity.Role;
import com.authforge.data.entity.User;
import com.authforge.data.repository.UserRepository;
import com.authforge.web.dto.UserDto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthForgeService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.expiration.millis}")
    private long jwtExpirationMillis;

    // تم إضافة حقن المفتاح السري هنا لمنع الـ NullPointerException وضع قيمة افتراضية للاحتياط
    @Value("${jwt.secret:defaultSuperSecretKeyThatIsLongEnoughToSatisfyHMACRequirements}")
    private String jwtSecret;

    @Override
    public String authenticateAndGenerateToken(String username,
                                               String password) throws AuthException {

        Optional<User> optUser = userRepository.findByUsername(username);

        if (optUser.isEmpty() ||
                !passwordEncoder.matches(password,
                        optUser.get().getBcryptPassword())) {

            throw new AuthException("Authentication failed");
        }

        User user = optUser.get();

        return generateJwtToken(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getUuid(),
                user.getRole()
        );
    }

    @Override
    public UserDto parseTokenAndGetUser(String token) {
        // تم استبدال System.getenv بالمتغير المحقن jwtSecret
        Jws<Claims> claimsJws = Jwts.parserBuilder()
                .setSigningKey(jwtSecret.getBytes())
                .build()
                .parseClaimsJws(token);

        Claims claims = claimsJws.getBody();

        String username = claims.get("username", String.class);
        String firstName = claims.get("firstName", String.class);
        String lastName = claims.get("lastName", String.class);
        String phoneNumber = claims.get("phoneNumber", String.class);
        String uuidStr = claims.get("uuid", String.class);
        String role = claims.get("role", String.class);

        return new UserDto(
                username,
                firstName,
                lastName,
                phoneNumber,
                UUID.fromString(uuidStr),
                Role.valueOf(role)
        );
    }

    @Override
    public boolean signUp(String username,
                          String password,
                          String firstName,
                          String lastName,
                          String phoneNumber) {

        Optional<User> optUser = userRepository.findByUsername(username);

        if (optUser.isPresent()) {
            return false;
        }

        User newUser = User.builder()
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .bcryptPassword(passwordEncoder.encode(password))
                .role(Role.USER)
                .build();

        userRepository.save(newUser);

        return true;
    }

    @Override
    public List<UserDto> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(user -> new UserDto(
                        user.getUsername(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getPhoneNumber(),
                        user.getUuid(),
                        user.getRole()
                ))
                .collect(Collectors.toList());
    }

    private String generateJwtToken(
            String username,
            String firstName,
            String lastName,
            String phoneNumber,
            UUID uuid,
            Role role
    ) {

        Date expirationDate =
                new Date(System.currentTimeMillis() + jwtExpirationMillis);

        Map<String, Object> claims = new HashMap<>();

        claims.put("username", username);
        claims.put("firstName", firstName);
        claims.put("lastName", lastName);
        claims.put("phoneNumber", phoneNumber);
        claims.put("uuid", uuid);
        claims.put("role", role.name());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(
                        // تم استبدال System.getenv بالمتغير المحقن jwtSecret
                        Keys.hmacShaKeyFor(jwtSecret.getBytes())
                )
                .compact();
    }
}