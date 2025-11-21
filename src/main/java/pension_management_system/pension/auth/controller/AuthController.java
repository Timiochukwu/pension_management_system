package pension_management_system.pension.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pension_management_system.pension.auth.dto.LoginRequest;
import pension_management_system.pension.auth.dto.LoginResponse;
import pension_management_system.pension.auth.dto.RegisterRequest;
import pension_management_system.pension.auth.service.AuthService;
import pension_management_system.pension.common.dto.ApiResponseDto;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and Authorization APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponseDto<LoginResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/v1/auth/register - Register user: {}", request.getUsername());
        LoginResponse response = authService.register(request);
        ApiResponseDto<LoginResponse> apiResponse = ApiResponseDto.<LoginResponse>builder()
                .success(true)
                .message("User registered successfully")
                .data(response)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ResponseEntity<ApiResponseDto<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/v1/auth/login - Login attempt for user: {}", request.getUsername());
        LoginResponse response = authService.login(request);
        ApiResponseDto<LoginResponse> apiResponse = ApiResponseDto.<LoginResponse>builder()
                .success(true)
                .message("Login successful")
                .data(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}
