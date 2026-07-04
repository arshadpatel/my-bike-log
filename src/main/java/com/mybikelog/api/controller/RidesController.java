package com.mybikelog.api.controller;

import com.mybikelog.api.dto.RideDTO;
import com.mybikelog.api.dto.RidePageDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rides/{bikeId}")
public class RidesController {

    @GetMapping
    public ResponseEntity<RidePageDTO> getRides(@PathVariable String bikeId,
                                                @RequestParam(required = false)  String month,
                                                @RequestParam(required = false)  String page,
                                                @RequestParam(required = false)  String size){
        return null;
    }

    @PostMapping
    public ResponseEntity<RideDTO> addRide(@PathVariable String bikeId,
                                           @RequestBody RideDTO rideRequest){
        return null;
    }

    @DeleteMapping("/{rideId}")
    public ResponseEntity<?> deleteRide(@PathVariable String bikeId,
                                        @PathVariable String rideId){
        return null;
    }
}
