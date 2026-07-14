package com.mybikelog.api.service;

import com.mybikelog.api.dto.RideDTO;
import com.mybikelog.api.entity.BikeEntity;
import com.mybikelog.api.entity.RideEntity;
import com.mybikelog.api.mapper.MapperClass;
import com.mybikelog.api.repository.BikeRepository;
import com.mybikelog.api.repository.RideRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Service
public class RideService {

    private final BikeRepository bikeRepository;
    private final RideRepository rideRepository;
    private final MapperClass mapperClass;

    public RideDTO addRide(UUID userId, UUID bikeId, RideDTO rideRequest) {
        BikeEntity bike = getBikeDetails(userId, bikeId);
        if(rideRequest.getOdo() < bike.getCurrentOdo()) throw new RuntimeException("Enter Correct Odometer Reading");
        Optional<RideEntity> previousRide = rideRepository.findTopByBikeIdOrderByOdoDesc(bikeId);
        Double newOdo = rideRequest.getOdo();
        Double distance = null;
        if (previousRide.isPresent()){
            Double previousRideOdo = previousRide.get().getOdo();
            distance = newOdo - previousRideOdo;
        }else {
            Double bikeInitialOdo = bike.getInitialOdo();
            distance = newOdo - bikeInitialOdo;
        }
        RideEntity rideEntity = mapperClass.toRideEntity(rideRequest);
        rideEntity.setBike(bike);
        rideEntity.setDistanceKm(distance);
        rideEntity.setCreatedAt(Instant.now());
        RideEntity savedRide = rideRepository.save(rideEntity);

        bike.setCurrentOdo(rideRequest.getOdo());
        saveBikeDetails(bike);

        return mapperClass.toRideDto(savedRide);
    }

    private void saveBikeDetails(BikeEntity bike) {
        bikeRepository.save(bike);
    }

    public BikeEntity getBikeDetails(UUID userId, UUID bikeId){
        return bikeRepository.findByIdAndUserId(bikeId, userId)
                .orElseThrow(() -> new RuntimeException("Bike Not Found"));
    }
}
