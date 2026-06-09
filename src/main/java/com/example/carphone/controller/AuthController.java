package com.example.carphone.controller;

import com.example.carphone.dto.AuthDtos.LoginResponse;
import com.example.carphone.dto.AuthDtos.WechatLoginRequest;
import com.example.carphone.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/wechat-login")
    public LoginResponse login(@Valid @RequestBody WechatLoginRequest request) {
        return authService.login(request);
    }
}
