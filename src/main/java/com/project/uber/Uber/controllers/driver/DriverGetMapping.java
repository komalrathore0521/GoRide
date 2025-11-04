package com.project.uber.Uber.controllers.driver;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.project.uber.Uber.dto.DriverDto;
import com.project.uber.Uber.dto.DriverRideDto;
import com.project.uber.Uber.services.DriverService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/driver")
@Secured("ROLE_DRIVER")
@Tag(name = "Driver Data", description = "Endpoints for retrieving driver profile and ride history")
@SecurityRequirement(name = "bearerAuth")
public class DriverGetMapping {

    private final DriverService driverService;
    private static final int PAGE_SIZE = 4;

    public DriverGetMapping(DriverService driverService) {
        this.driverService = driverService;
    }

    @Operation(
            summary = "Get driver profile",
            description = "Retrieves the logged-in driver's profile details such as name, rating, vehicle information, and status."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Driver profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DriverDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Access forbidden for non-driver roles")
    })
    @GetMapping("/getMyProfile")
    public ResponseEntity<DriverDto> getDriverProfile() {
        return ResponseEntity.ok(driverService.getDriverProfile());
    }

    @Operation(
            summary = "Get driver's ride history (paginated)",
            description = "Fetches all rides assigned to the logged-in driver, sorted and paginated based on query parameters."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of rides retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DriverRideDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination or sort parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token")
    })
    @GetMapping("/getMyRides")
    public ResponseEntity<List<DriverRideDto>> getAllMyRides(
            @Parameter(description = "Field to sort rides by (default: id)", example = "id")
            @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "Page number (starting from 0)", example = "0")
            @RequestParam(defaultValue = "0") Integer pageNumber) {

        Pageable pageRequest = PageRequest.of(pageNumber, PAGE_SIZE, Sort.by(sortBy).ascending());
        Page<DriverRideDto> rides = driverService.getAllMyRides(pageRequest);
        return ResponseEntity.ok(rides.getContent());
    }
}
