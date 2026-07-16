package com.mybikelog.api.controller;

import com.mybikelog.api.dto.PageDTO;
import com.mybikelog.api.dto.PetrolDTO;
import com.mybikelog.api.service.PetrolService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
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

@RestController
@AllArgsConstructor
@RequestMapping("/petrol-entries/{bikeId}")
public class PetrolController {

    private final PetrolService petrolService;

    @GetMapping
    public ResponseEntity<PageDTO<PetrolDTO>> getPetrolFillUps(@AuthenticationPrincipal String id,
                                                    @PathVariable String bikeId,
                                                    @RequestParam(required = false)  String month,
                                                    @RequestParam(required = false)  Integer page,
                                                    @RequestParam(required = false)  Integer size){
        int pageNo = page == null ? 0 : page;
        int pageSize = size == null ? 10 : size;

        PageDTO<PetrolDTO> pageDTO = petrolService.getAllFillUps(UUID.fromString(id), UUID.fromString(bikeId), pageNo, pageSize, month);
        return ResponseEntity.ok(pageDTO);
    }

    @PostMapping
    public ResponseEntity<PetrolDTO> addPetrolFillUp(@AuthenticationPrincipal String id,
                                                   @PathVariable String bikeId,
                                                   @RequestBody PetrolDTO petrolRequest){
        PetrolDTO petrolDTO = petrolService.addPetrol(UUID.fromString(id),
                UUID.fromString(bikeId), petrolRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(petrolDTO);
    }

    @DeleteMapping("/{petrolEntryId}")
    public ResponseEntity<?> deletePetrolFillUp(@PathVariable String bikeId,
                                                @PathVariable String petrolEntryId){
        petrolService.deletePetrolEntry(UUID.fromString(bikeId), UUID.fromString(petrolEntryId));
        return ResponseEntity.noContent().build();
    }
}
