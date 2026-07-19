package com.mybikelog.api.service;

import com.mybikelog.api.dto.PageDTO;
import com.mybikelog.api.dto.RideDTO;
import com.mybikelog.api.entity.BikeEntity;
import com.mybikelog.api.entity.RideEntity;
import com.mybikelog.api.mapper.MapperClass;
import com.mybikelog.api.repository.BikeRepository;
import com.mybikelog.api.repository.RideRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Service
public class RideService {

    private final BikeRepository bikeRepository;
    private final RideRepository rideRepository;
    private final MapperClass mapperClass;
    private final CommonService commonService;

    public RideDTO addRide(UUID userId, UUID bikeId, RideDTO rideRequest) {
        BikeEntity bike = commonService.getBikeDetails(userId, bikeId);
        if(rideRequest.getOdo() < ((bike.getCurrentOdo() != null) ?
                bike.getCurrentOdo() : 0.0)) throw new RuntimeException("Enter Correct Odometer Reading");
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

        commonService.updateBikeCurrentOdo(bike);

        return mapperClass.toRideDto(savedRide);
    }

    public PageDTO<RideDTO> getAllRides(UUID userId, UUID bikeId, int pageNo, int pageSize, String month) {
        commonService.getBikeDetails(userId, bikeId);
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<RideEntity> rides;
        if(month == null){
            rides = rideRepository.findByBikeIdOrderByCreatedAtDesc(bikeId, pageable);
        }else{
            YearMonth yearMonth = YearMonth.parse(month);
            rides = rideRepository.findByBikeIdAndDateBetweenOrderByCreatedAtDesc(bikeId,
                    yearMonth.atDay(1), yearMonth.atEndOfMonth(), pageable);
        }
        return mapperClass.toPageDto(rides, mapperClass::toRideDto);
    }

    public void deleteRide(UUID bikeId, UUID rideId) {
        BikeEntity bike = commonService.getBikeDetails(bikeId);

        RideEntity rideEntity = rideRepository.findByIdAndBikeId(rideId, bikeId)
                .orElseThrow(() -> new RuntimeException("Ride Not Found"));

        RideEntity latestRide = rideRepository.findTopByBikeIdOrderByOdoDesc(bikeId)
                .orElseThrow(() -> new RuntimeException("No Latest Ride Found"));

        if (!rideEntity.getId().equals(latestRide.getId()))
            throw new RuntimeException("Only Latest ride can be deleted");

        rideRepository.delete(latestRide);

        commonService.updateBikeCurrentOdo(bike);
    }
}
