package com.mybikelog.api.service;

import com.mybikelog.api.dto.BikeDTO;
import com.mybikelog.api.entity.BikeEntity;
import com.mybikelog.api.mapper.MapperClass;
import com.mybikelog.api.repository.BikeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class BikeService {

    private final BikeRepository bikeRepository;
    private final MapperClass mapperClass;

    public List<BikeDTO> getAllBikes(String id) {
        List<BikeEntity> bikeList = bikeRepository.findByUserId(UUID.fromString(id));
        return mapperClass.toListBikeDto(bikeList);
    }
}
