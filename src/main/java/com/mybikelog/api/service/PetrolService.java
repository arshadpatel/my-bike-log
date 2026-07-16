package com.mybikelog.api.service;

import com.mybikelog.api.dto.PageDTO;
import com.mybikelog.api.dto.PetrolDTO;
import com.mybikelog.api.entity.BikeEntity;
import com.mybikelog.api.entity.PetrolEntity;
import com.mybikelog.api.mapper.MapperClass;
import com.mybikelog.api.repository.BikeRepository;
import com.mybikelog.api.repository.PetrolRepository;
import com.mybikelog.api.util.NumberUtil;
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
public class PetrolService {

    private final PetrolRepository petrolRepository;
    private final BikeRepository bikeRepository;
    private final MapperClass mapperClass;

    public PetrolDTO addPetrol(UUID userId, UUID bikeId, PetrolDTO petrolRequest) {
        BikeEntity bike = getBikeDetails(userId, bikeId);
        if(petrolRequest.getOdo() < bike.getCurrentOdo()) throw new RuntimeException("Enter Correct Odometer Reading");

        Optional<PetrolEntity> previousPetrolEntity = petrolRepository.findTopByBikeIdOrderByOdoDesc(bikeId);

        double cumulative = 0.0;

        if(previousPetrolEntity.isPresent()){
            cumulative = previousPetrolEntity.get().getCumulativeLitres();
            Double currentOdo = petrolRequest.getOdo();
            Double previousOdo = previousPetrolEntity.get().getOdo();
            Double distanceDriven = currentOdo - previousOdo;
            Double previousLitres = previousPetrolEntity.get().getLitres();
            previousPetrolEntity.get().setDistanceKm(distanceDriven);
            previousPetrolEntity.get().setMileageKmPerLitre( NumberUtil.roundTo2Decimals(
                    distanceDriven / previousLitres));
            petrolRepository.save(previousPetrolEntity.get());
        }

        PetrolEntity petrolEntity = mapperClass.toPetrolEntity(petrolRequest);

        double litres = NumberUtil.roundTo2Decimals(
                petrolRequest.getAmount() / petrolRequest.getPricePerLitre());
        petrolEntity.setLitres(litres);
        petrolEntity.setCumulativeLitres(litres + cumulative);
        petrolEntity.setCreatedAt(Instant.now());
        petrolEntity.setBike(bike);

        PetrolEntity savedPetrolEntity = petrolRepository.save(petrolEntity);

        bike.setCurrentOdo(petrolRequest.getOdo());
        saveBikeDetails(bike);

        return mapperClass.toPetrolDto(savedPetrolEntity);
    }

    public BikeEntity getBikeDetails(UUID userId, UUID bikeId){
        return bikeRepository.findByIdAndUserId(bikeId, userId)
                .orElseThrow(() -> new RuntimeException("Bike Not Found"));
    }

    private void saveBikeDetails(BikeEntity bike) {
        bikeRepository.save(bike);
    }

    public PageDTO<PetrolDTO> getAllFillUps(UUID userId, UUID bikeId, int pageNo, int pageSize, String month) {
        getBikeDetails(userId, bikeId);
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<PetrolEntity> petrolEntities;
        if(month == null){
            petrolEntities = petrolRepository.findByBikeIdOrderByCreatedAtDesc(bikeId, pageable);
        }else {
            YearMonth yearMonth = YearMonth.parse(month);
            petrolEntities = petrolRepository.findByBikeIdAndDateBetweenOrderByCreatedAtDesc(bikeId,
                    yearMonth.atDay(1), yearMonth.atEndOfMonth(), pageable);
        }
        return mapperClass.toPageDto(petrolEntities, mapperClass::toPetrolDto);
    }
}
