package ext.vnua.tkb_api_lavadev.controller;

import ext.vnua.tkb_api_lavadev.model.dto.LoginResponseToUser;
import ext.vnua.tkb_api_lavadev.model.request.GetTimeableListRequest;
import ext.vnua.tkb_api_lavadev.model.request.LoginRequest;
import ext.vnua.tkb_api_lavadev.model.response.ApiResponse;
import ext.vnua.tkb_api_lavadev.model.response.GetListTermResponse;
import ext.vnua.tkb_api_lavadev.service.TimeableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Timeable Management", description = "APIs for managing timetables and academic schedules")
public class TimeableController {

    private final TimeableService timeableService;

    @PostMapping("/auth/login")
    @Operation(summary = "User authentication", description = "Authenticate user and return access token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<ResponseEntity<ApiResponse<LoginResponseToUser>>> login(
            @Valid @RequestBody LoginRequest loginRequest) {

        log.info("Login request received for user: {}", loginRequest.getUsername());

        return timeableService.login(loginRequest)
                .map(loginResponse -> ResponseEntity.ok(
                        ApiResponse.<LoginResponseToUser>builder()
                                .success(true)
                                .message("Login successful")
                                .data(loginResponse)
                                .build()
                ))
                .onErrorResume(ex -> {
                    log.error("Login failed for user: {}", loginRequest.getUsername(), ex);
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(ApiResponse.<LoginResponseToUser>builder()
                                    .success(false)
                                    .message("Authentication failed: " + ex.getMessage())
                                    .build()));
                });
    }

    @GetMapping("/timeable/terms")
    @Operation(summary = "Get academic terms", description = "Retrieve list of available academic terms")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Terms retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<ResponseEntity<ApiResponse<List<GetListTermResponse>>>> getListTerm(
            @Parameter(description = "Authorization token", required = true)
            @RequestHeader("Authorization") @NotBlank String authHeader) {

        log.info("Get terms request received");

        return timeableService.getListTerm(authHeader)
                .map(terms -> ResponseEntity.ok(
                        ApiResponse.<List<GetListTermResponse>>builder()
                                .success(true)
                                .message("Terms retrieved successfully")
                                .data(terms)
                                .build()
                ))
                .onErrorResume(ex -> {
                    log.error("Failed to retrieve terms", ex);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.<List<GetListTermResponse>>builder()
                                    .success(false)
                                    .message("Failed to retrieve terms: " + ex.getMessage())
                                    .build()));
                });
    }

    @PostMapping("/timeable/schedule")
    @Operation(summary = "Get timetable schedule", description = "Retrieve detailed timetable for a specific term")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Schedule retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> getTimeableList(
            @Parameter(description = "Authorization token", required = true)
            @RequestHeader("Authorization") @NotBlank String authHeader,
            @Valid @RequestBody GetTimeableListRequest request) {

        log.info("Get timeable schedule request received for term: {}", request.getTermCode());

        return timeableService.getTimeableList(authHeader, request)
                .map(schedule -> ResponseEntity.ok(
                        ApiResponse.<Map<String, Object>>builder()
                                .success(true)
                                .message("Schedule retrieved successfully")
                                .data(schedule)
                                .build()
                ))
                .onErrorResume(ex -> {
                    log.error("Failed to retrieve schedule for term: {}", request.getTermCode(), ex);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.<Map<String, Object>>builder()
                                    .success(false)
                                    .message("Failed to retrieve schedule: " + ex.getMessage())
                                    .build()));
                });
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the service is running")
    public Mono<ResponseEntity<ApiResponse<String>>> healthCheck() {
        return Mono.just(ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Service is running")
                        .data("OK")
                        .build()
        ));
    }
}