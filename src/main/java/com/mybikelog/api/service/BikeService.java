package com.mybikelog.api.service;

import com.mybikelog.api.dto.BikeDTO;
import com.mybikelog.api.entity.BikeEntity;
import com.mybikelog.api.entity.UserEntity;
import com.mybikelog.api.mapper.MapperClass;
import com.mybikelog.api.repository.BikeRepository;
import com.mybikelog.api.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Service
public class BikeService {

    private final UserRepository userRepository;
    private final BikeRepository bikeRepository;
    private final MapperClass mapperClass;

    public List<BikeDTO> getAllBikes(UUID id) {
        List<BikeEntity> bikeList = bikeRepository.findByUserId(id);
        return mapperClass.toListBikeDto(bikeList);
    }

    public BikeDTO addNewBike(UUID uuid, BikeDTO bikeRequest) {
        Optional<UserEntity> user = userRepository.findById(uuid);
        BikeEntity bike = mapperClass.toBikeEntity(bikeRequest);
        bike.setCreatedAt(Instant.now());
        bike.setUser(user.get());
        BikeEntity savedBike = bikeRepository.save(bike);
        return mapperClass.toBikeDto(savedBike);
    }

    public BikeDTO getBike(UUID userId, UUID bikeId) {
        BikeEntity bikeEntity = getBikeEntity(userId, bikeId);
        return mapperClass.toBikeDto(bikeEntity);
    }

    public BikeDTO updateBikeDetails(UUID userId, UUID bikeId, BikeDTO bikeRequest) {
        BikeEntity fetchBikeDetails = getBikeEntity(userId, bikeId);
        fetchBikeDetails.setName(bikeRequest.getName());
        fetchBikeDetails.setCurrentOdo(bikeRequest.getCurrentOdo());
        fetchBikeDetails.setInitialOdo(bikeRequest.getInitialOdo());
        fetchBikeDetails.setOilChangeIntervalKm(bikeRequest.getOilChangeIntervalKm());
        BikeEntity updatedBikeDetails = bikeRepository.save(fetchBikeDetails);
        return mapperClass.toBikeDto(updatedBikeDetails);
    }

    public BikeEntity getBikeEntity(UUID userId, UUID bikeId){
        return bikeRepository.findByIdAndUserId(bikeId, userId)
                .orElseThrow(() -> new RuntimeException("Bike Not Found"));
    }
}
