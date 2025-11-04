package com.project.uber.Uber.controllers.rider;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.project.uber.Uber.dto.DriverDto;
import com.project.uber.Uber.dto.RatingDto;
import com.project.uber.Uber.dto.RideDto;
import com.project.uber.Uber.dto.RideRequestDto;
import com.project.uber.Uber.services.RatingManagementService;
import com.project.uber.Uber.services.RiderService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rider")
@Secured("ROLE_RIDER")
@Tag(name = "Rider Actions", description = "Endpoints for riders to request, cancel rides and rate drivers")
@SecurityRequirement(name = "bearerAuth")
public class RiderPostMapping {

    private final RiderService riderService;

    public RiderPostMapping(RiderService riderService, RatingManagementService ratingManagementService) {
        this.riderService = riderService;
    }

    @Operation(
            summary = "Request a ride",
            description = "Creates a new ride request for the logged-in rider.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Ride request details (pickup, destination, and vehicle type)",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RideRequestDto.class),
                            examples = @ExampleObject(value = "{\"pickupLocation\":\"NIT Raipur\",\"destination\":\"Raipur Railway Station\",\"vehicleType\":\"Sedan\"}")
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ride request created successfully",
                    content = @Content(schema = @Schema(implementation = RideRequestDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT")
    })
    @PostMapping("/requestRide")
    public ResponseEntity<RideRequestDto> requestRide(@Valid @RequestBody RideRequestDto rideRequestDto) {
        RideRequestDto requestedRide = riderService.requestRide(rideRequestDto);
        return ResponseEntity.ok(requestedRide);
    }

    @Operation(
            summary = "Cancel a ride",
            description = "Cancels an active or upcoming ride by ride ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ride cancelled successfully",
                    content = @Content(schema = @Schema(implementation = RideDto.class))),
            @ApiResponse(responseCode = "404", description = "Ride not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT")
    })
    @PostMapping("/cancelRide/{rideId}")
    public ResponseEntity<RideDto> cancelRide(
            @Parameter(description = "ID of the ride to cancel", required = true, example = "123")
            @PathVariable Long rideId) {
        return ResponseEntity.ok(riderService.cancelRide(rideId));
    }

    @Operation(
            summary = "Rate a driver",
            description = "Submit a rating for the driver after completing a ride.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Rating payload containing numeric rating (1-5) and optional comments",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RatingDto.class),
                            examples = @ExampleObject(value = "{\"rating\":5,\"comments\":\"Excellent driving and polite behavior.\"}")
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Driver rated successfully",
                    content = @Content(schema = @Schema(implementation = DriverDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid rating value"),
            @ApiResponse(responseCode = "404", description = "Ride or driver not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT")
    })
    @PostMapping("/rateDriver/{rideId}")
    public ResponseEntity<DriverDto> rateDriver(
            @Parameter(description = "ID of the completed ride to rate the driver", required = true, example = "123")
            @PathVariable Long rideId,
            @Valid @RequestBody RatingDto ratingDto) {
        return ResponseEntity.ok(riderService.rateDriver(rideId, ratingDto.getRating()));
    }
}
