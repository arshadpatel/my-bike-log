package com.mybikelog.api.controller;

import com.mybikelog.api.dto.BikeDTO;
import com.mybikelog.api.service.BikeService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/bikes")
public class BikesController {

    private final BikeService bikeService;

    @GetMapping
    public ResponseEntity<List<BikeDTO>> getAllBikes(@AuthenticationPrincipal String id){
        List<BikeDTO> bikeList = bikeService.getAllBikes(UUID.fromString(id));
        return ResponseEntity.ok(bikeList);
    }

    @PostMapping
    public ResponseEntity<BikeDTO> addNewBike(@AuthenticationPrincipal String id,
                                              @RequestBody BikeDTO bikeRequest){
        BikeDTO bikeDTO = bikeService.addNewBike(UUID.fromString(id), bikeRequest);
        return ResponseEntity.ok(bikeDTO);
    }

    @GetMapping("/{bikeId}")
    public ResponseEntity<BikeDTO> getBike(@AuthenticationPrincipal String id,
                                           @PathVariable String bikeId){
        BikeDTO bikeDTO = bikeService.getBike(UUID.fromString(id), UUID.fromString(bikeId));
        return ResponseEntity.ok(bikeDTO);
    }

    @PutMapping("/{bikeId}")
    public ResponseEntity<BikeDTO> updateBikeDetails(@AuthenticationPrincipal String id,
                                                     @PathVariable String bikeId,
                                                     @RequestBody BikeDTO bikeRequest){
        BikeDTO bikeDTO = bikeService.updateBikeDetails(UUID.fromString(id),
                UUID.fromString(bikeId), bikeRequest);
        return ResponseEntity.ok(bikeDTO);
    }

    @DeleteMapping("/{bikeId}")
    public ResponseEntity<?> deleteBikeAndRecords(@PathVariable String bikeId){
        return null;
    }
}
