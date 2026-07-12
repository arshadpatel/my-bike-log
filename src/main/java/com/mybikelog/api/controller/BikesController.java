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

@RestController
@AllArgsConstructor
@RequestMapping("/bikes")
public class BikesController {

    private final BikeService bikeService;

    @GetMapping
    public ResponseEntity<List<BikeDTO>> getAllBikes(@AuthenticationPrincipal String id){
        List<BikeDTO> bikeList = bikeService.getAllBikes(id);
        return ResponseEntity.ok(bikeList);
    }

    @PostMapping
    public ResponseEntity<BikeDTO> addNewBike(@RequestBody BikeDTO bikeRequest){
        return null;
    }

    @GetMapping("/{bikeId}")
    public ResponseEntity<BikeDTO> getBike(@PathVariable String bikeId){
        return null;
    }

    @PutMapping("/{bikeId}")
    public ResponseEntity<BikeDTO> updateBikeDetails(@PathVariable String bikeId,
                                                     @RequestBody BikeDTO bikeRequest){
        return null;
    }

    @DeleteMapping("/{bikeId}")
    public ResponseEntity<?> deleteBikeAndRecords(@PathVariable String bikeId){
        return null;
    }
}
