package com.mybikelog.api.controller;

import com.mybikelog.api.dto.RideDTO;
import com.mybikelog.api.dto.PageDTO;
import com.mybikelog.api.service.RideService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/rides/{bikeId}")
public class RideController {

    private final RideService rideService;

    @GetMapping
    public ResponseEntity<PageDTO> getRides(@AuthenticationPrincipal String id,
                                            @PathVariable String bikeId,
                                            @RequestParam(required = false)  String month,
                                            @RequestParam(required = false)  Integer page,
                                            @RequestParam(required = false)  Integer size){
        int pageNo = page == null ? 0 : page;
        int pageSize = size == null ? 10 : size;

        PageDTO pageDTO = rideService.getAllRides(UUID.fromString(id), UUID.fromString(bikeId), pageNo, pageSize, month);
        return ResponseEntity.ok(pageDTO);
    }

    @PostMapping
    public ResponseEntity<RideDTO> addRide(@AuthenticationPrincipal String id,
                                           @PathVariable String bikeId,
                                           @RequestBody RideDTO rideRequest){
        RideDTO rideDTO = rideService.addRide(UUID.fromString(id),
                UUID.fromString(bikeId), rideRequest);
        return ResponseEntity.ok(rideDTO);
    }

    @DeleteMapping("/{rideId}")
    public ResponseEntity<?> deleteRide(@PathVariable String bikeId,
                                        @PathVariable String rideId){
        rideService.deleteRide(UUID.fromString(bikeId), UUID.fromString(rideId));
        return ResponseEntity.noContent().build();
    }
}
