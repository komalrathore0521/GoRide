package com.project.uber.Uber.controllers.rider;

import com.project.uber.Uber.dto.RideDto;
import com.project.uber.Uber.dto.RiderDto;
import com.project.uber.Uber.services.RiderService;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/rider")
@Secured("ROLE_RIDER")
@Tag(name = "Rider Data", description = "Endpoints for riders to access their profile and ride history")
@SecurityRequirement(name = "bearerAuth")
public class RiderGetMapping {

    private final RiderService riderService;
    private static final int PAGE_SIZE = 4;

    public RiderGetMapping(RiderService riderService) {
        this.riderService = riderService;
    }

    @Operation(
            summary = "Get rider profile",
            description = "Retrieves the logged-in rider’s profile information such as name, contact, and trip statistics."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rider profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = RiderDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Access forbidden for non-rider roles")
    })
    @GetMapping("/getMyProfile")
    public ResponseEntity<RiderDto> getRiderProfile() {
        return ResponseEntity.ok(riderService.getRiderProfile());
    }

    @Operation(
            summary = "Get rider's ride history (paginated)",
            description = "Fetches all rides associated with the logged-in rider, supporting sorting and pagination."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of rides retrieved successfully",
                    content = @Content(schema = @Schema(implementation = RideDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination or sort parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token")
    })
    @GetMapping("/getMyRides")
    public ResponseEntity<List<RideDto>> getAllMyRides(
            @Parameter(description = "Field to sort rides by (default: id)", example = "id")
            @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "Page number (starting from 0)", example = "0")
            @RequestParam(defaultValue = "0") Integer pageNumber) {

        Pageable pageRequest = PageRequest.of(pageNumber, PAGE_SIZE, Sort.by(sortBy).ascending());
        Page<RideDto> rides = riderService.getAllMyRides(pageRequest);
        return ResponseEntity.ok(rides.getContent());
    }
}
