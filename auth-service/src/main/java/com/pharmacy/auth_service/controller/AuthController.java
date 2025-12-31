package com.pharmacy.auth_service.controller;

import com.pharmacy.auth_service.dto.ApiResponse;
import com.pharmacy.auth_service.dto.LoginRequest;
import com.pharmacy.auth_service.dto.LoginResponse;
import com.pharmacy.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest req) {
        LoginResponse res = authService.login(req);
        return ApiResponse.ok("Đăng nhập thành công", res);
    }
}
