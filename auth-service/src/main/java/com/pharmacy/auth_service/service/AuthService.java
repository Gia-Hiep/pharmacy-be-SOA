package com.pharmacy.auth_service.service;

import com.pharmacy.auth_service.dto.LoginRequest;
import com.pharmacy.auth_service.dto.LoginResponse;
import com.pharmacy.auth_service.repository.UserRepository;
import com.pharmacy.auth_service.repository.UserRoleRepository;
import com.pharmacy.auth_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final UserRoleRepository userRoleRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest req) {
        var user = userRepo.findByUsername(req.username())
                .orElseThrow(() -> new RuntimeException("Invalid username"));

        if (!user.isActive()) {
            throw new RuntimeException("User is disabled");
        }

        if (!encoder.matches(req.password(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        List<String> roles = userRoleRepo.findRoleCodesByUserId(user.getId());
        String token = jwtService.generateToken(user.getId(), user.getUsername(), roles);

        return new LoginResponse(token, user.getId(), roles);
    }

}
