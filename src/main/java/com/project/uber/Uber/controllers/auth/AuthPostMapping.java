package com.project.uber.Uber.controllers.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import com.project.uber.Uber.dto.*;
import com.project.uber.Uber.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration, login, and JWT generation")
public class AuthPostMapping {

    @Value("${deploy.env}")
    private String deployment;
    private final AuthService authService;

    public AuthPostMapping(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Register a new user",
            description = "Registers a new rider or driver in the system. Provide required profile fields in the body.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Signup payload (email, password, role, etc.)",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SignupDto.class),
                            examples = @ExampleObject(value = "{\"email\":\"user@example.com\",\"password\":\"P@ssw0rd\",\"role\":\"RIDER\"}")
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully registered",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "User already exists")
    })
    @PostMapping(path = "/signup")
    public ResponseEntity<UserDto> signup(@Valid @RequestBody SignupDto signupDto){
        UserDto user = authService.signup(signupDto);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Onboard a driver (admin only)",
            description = "Convert an existing user to a driver by providing driver-specific details. Requires ADMIN role.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Driver onboarding details (license, vehicle info, etc.)",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = OnboardDriverDto.class),
                            examples = @ExampleObject(value = "{\"licenseNumber\":\"DL-12345\",\"vehicleNumber\":\"MP09AB1234\"}")
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Driver onboarded successfully",
                    content = @Content(schema = @Schema(implementation = DriverDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(path = "/onboardDriver/{userId}")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<DriverDto> onboardDriver(
            @Parameter(description = "ID of the user to convert into a driver", required = true) @PathVariable Long userId,
            @Valid @RequestBody OnboardDriverDto onboardDriverDto){
        DriverDto driver = authService.onboardNewDriver(userId, onboardDriverDto);
        return new ResponseEntity<>(driver, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Login and get JWT",
            description = "Authenticates the user and returns an access token (in body) and a httpOnly refresh token cookie (set on /auth/login).",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LoginRequestDto.class),
                            examples = @ExampleObject(value = "{\"email\":\"user@example.com\",\"password\":\"P@ssw0rd\"}")
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful, JWT returned",
                    content = @Content(schema = @Schema(implementation = LoginResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @PostMapping(path = "/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto, HttpServletResponse httpServletResponse){
        String[] tokens = authService.login(loginRequestDto);
        Cookie cookie = new Cookie("refreshToken", tokens[1]);
        cookie.setHttpOnly(true);
        cookie.setSecure(deployment.equals("production"));
        cookie.setPath("/auth/refresh");
        httpServletResponse.addCookie(cookie);

        return ResponseEntity.ok(new LoginResponseDto(tokens[0]));
    }

    @Operation(
            summary = "Refresh access token using refresh cookie",
            description = "Uses the httpOnly refresh token cookie (set at /auth/login) to issue a new access token. Note: httpOnly cookies cannot be set via Swagger UI browser requests; use curl/postman or a non-httpOnly endpoint for testing in the UI."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "New access token issued",
                    content = @Content(schema = @Schema(implementation = LoginResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Refresh token missing or invalid")
    })
    @PostMapping(path = "/refresh")
    public ResponseEntity<LoginResponseDto> refreshToken(HttpServletRequest httpServletRequest){
        String refreshToken = Arrays
                .stream(httpServletRequest.getCookies() != null ? httpServletRequest.getCookies() : new Cookie[]{})
                .filter(cookie -> cookie.getName().equals("refreshToken"))
                .findFirst()
                .map(cookie -> cookie.getValue())
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Refresh token not found"));
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }
}
