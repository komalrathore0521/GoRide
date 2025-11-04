package com.project.uber.Uber.controllers.driver;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.project.uber.Uber.dto.DriverRideDto;
import com.project.uber.Uber.dto.RatingDto;
import com.project.uber.Uber.dto.RideStartDto;
import com.project.uber.Uber.dto.RiderDto;
import com.project.uber.Uber.services.DriverService;
import com.project.uber.Uber.services.RatingManagementService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/driver")
@Secured("ROLE_DRIVER")
@Tag(name = "Driver Actions", description = "Endpoints for drivers to manage rides (accept/start/end/cancel) and rate riders")
@SecurityRequirement(name = "bearerAuth")
public class DriverPostMapping {

    private final DriverService driverService;

    // keep RatingManagementService in constructor if your app wires it elsewhere
    public DriverPostMapping(DriverService driverService, RatingManagementService ratingManagementService) {
        this.driverService = driverService;
    }

    @Operation(
            summary = "Accept a ride request",
            description = "Driver accepts a pending ride request specified by rideRequestId."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ride request accepted",
                    content = @Content(schema = @Schema(implementation = DriverRideDto.class))),
            @ApiResponse(responseCode = "404", description = "Ride request not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT")
    })
    @PostMapping("/acceptRide/{rideRequestId}")
    public ResponseEntity<DriverRideDto> acceptRide(
            @Parameter(description = "ID of the ride request to accept", required = true, example = "123")
            @PathVariable Long rideRequestId) {
        return ResponseEntity.ok(driverService.acceptRide(rideRequestId));
    }

    @Operation(
            summary = "Start a ride",
            description = "Mark a ride as started. Supply optional start details (e.g., startTimestamp, initialKm) in the body."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ride started successfully",
                    content = @Content(schema = @Schema(implementation = DriverRideDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid start details"),
            @ApiResponse(responseCode = "404", description = "Ride not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT")
    })
    @PostMapping("/startRide/{rideId}")
    public ResponseEntity<DriverRideDto> startRide(
            @Parameter(description = "ID of the ride to start", required = true, example = "456")
            @PathVariable Long rideId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Ride start details (e.g., startTime, initialKm)",
                    required = false,
                    content = @Content(schema = @Schema(implementation = RideStartDto.class),
                            examples = @ExampleObject(value = "{\"startTimestamp\":\"2025-11-04T10:15:30Z\",\"initialKm\":0}"))
            )
            @Valid @RequestBody(required = false) RideStartDto rideStartDto) {

        return ResponseEntity.ok(driverService.startRide(rideId, rideStartDto));
    }

    @Operation(
            summary = "End a ride",
            description = "Mark a ride as completed. The service will compute fare and finalize ride details."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ride ended successfully",
                    content = @Content(schema = @Schema(implementation = DriverRideDto.class))),
            @ApiResponse(responseCode = "404", description = "Ride not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT")
    })
    @PostMapping("/endRide/{rideId}")
    public ResponseEntity<DriverRideDto> endRide(
            @Parameter(description = "ID of the ride to end", required = true, example = "456")
            @PathVariable Long rideId) {

        return ResponseEntity.ok(driverService.endRide(rideId));
    }

    @Operation(
            summary = "Cancel a ride",
            description = "Cancel an ongoing or accepted ride. Returns the ride state after cancellation."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ride cancelled successfully",
                    content = @Content(schema = @Schema(implementation = DriverRideDto.class))),
            @ApiResponse(responseCode = "404", description = "Ride not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT")
    })
    @PostMapping("/cancelRide/{rideId}")
    public ResponseEntity<DriverRideDto> cancelRide(
            @Parameter(description = "ID of the ride to cancel", required = true, example = "456")
            @PathVariable Long rideId) {
        return ResponseEntity.ok(driverService.cancelRide(rideId));
    }

    @Operation(
            summary = "Rate the rider",
            description = "Driver rates the rider for a completed ride. Provide a rating (e.g., 1-5) in the request body."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rider rated successfully",
                    content = @Content(schema = @Schema(implementation = RiderDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid rating value"),
            @ApiResponse(responseCode = "404", description = "Ride or rider not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT")
    })
    @PostMapping("/rateRider/{rideId}")
    public ResponseEntity<RiderDto> rateRider(
            @Parameter(description = "ID of the completed ride for which to rate the rider", required = true, example = "456")
            @PathVariable Long rideId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Rating payload containing numeric rating (1-5) and optional comments",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RatingDto.class),
                            examples = @ExampleObject(value = "{\"rating\":5,\"comments\":\"Great rider, punctual.\"}"))
            )
            @Valid @RequestBody RatingDto ratingDto) {

        return ResponseEntity.ok(driverService.rateRider(rideId, ratingDto.getRating()));
    }
}
