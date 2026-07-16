package com.mybikelog.api.service;

import com.mybikelog.api.entity.BikeEntity;
import com.mybikelog.api.entity.PetrolEntity;
import com.mybikelog.api.entity.RideEntity;
import com.mybikelog.api.repository.BikeRepository;
import com.mybikelog.api.repository.PetrolRepository;
import com.mybikelog.api.repository.RideRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@AllArgsConstructor
@Service
public class CommonService {

    private final BikeRepository bikeRepository;
    private final RideRepository rideRepository;
    private final PetrolRepository petrolRepository;

    public BikeEntity getBikeDetails(UUID userId, UUID bikeId){
        return bikeRepository.findByIdAndUserId(bikeId, userId)
                .orElseThrow(() -> new RuntimeException("Bike Not Found"));
    }

    public BikeEntity getBikeDetails(UUID bikeId){
        return bikeRepository.findById(bikeId)
                .orElseThrow(() -> new RuntimeException("Bike Not Found"));
    }

    public void updateBikeCurrentOdo(BikeEntity bike) {

        Double latestRideOdo = rideRepository
                .findTopByBikeIdOrderByOdoDesc(bike.getId())
                .map(RideEntity::getOdo)
                .orElse(bike.getInitialOdo());

        Double latestPetrolOdo = petrolRepository
                .findTopByBikeIdOrderByOdoDesc(bike.getId())
                .map(PetrolEntity::getOdo)
                .orElse(bike.getInitialOdo());

        bike.setCurrentOdo(
                Math.max(latestRideOdo, latestPetrolOdo));

        bikeRepository.save(bike);
    }
}
