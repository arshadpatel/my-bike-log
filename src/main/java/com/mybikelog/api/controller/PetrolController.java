package com.mybikelog.api.controller;

import com.mybikelog.api.dto.PageDTO;
import com.mybikelog.api.dto.RideDTO;
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
@RequestMapping("/petrol-entries/{bikeId}")
public class PetrolController {

    @GetMapping
    public ResponseEntity<PageDTO> getPetrolFillUps(@PathVariable String bikeId,
                                                    @RequestParam(required = false)  String month,
                                                    @RequestParam(required = false)  String page,
                                                    @RequestParam(required = false)  String size){
        return null;
    }

    @PostMapping
    public ResponseEntity<RideDTO> addPetrolFillUp(@PathVariable String bikeId,
                                                   @RequestBody RideDTO petrolRequest){
        return null;
    }

    @DeleteMapping("/{petrolEntryId}")
    public ResponseEntity<?> deletePetrolFillUp(@PathVariable String bikeId,
                                                @PathVariable String petrolEntryId){
        return null;
    }
}
