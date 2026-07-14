package com.mybikelog.api.mapper;

import com.mybikelog.api.dto.BikeDTO;
import com.mybikelog.api.dto.RideDTO;
import com.mybikelog.api.dto.UsersDTO;
import com.mybikelog.api.entity.BikeEntity;
import com.mybikelog.api.entity.RideEntity;
import com.mybikelog.api.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface MapperClass {

    UsersDTO toUsersDto(UserEntity user);

    UserEntity toUsersEntity(UsersDTO dto);

    BikeDTO toBikeDto(BikeEntity bikeEntity);

    @Mapping(target = "user", ignore = true)
    BikeEntity toBikeEntity(BikeDTO bikeDTO);

    List<BikeDTO> toListBikeDto(List<BikeEntity> bikeEntityList);

    @Mapping(target = "bike", ignore = true)
    RideEntity toRideEntity(RideDTO rideDTO);

    @Mapping(target = "bikeId", source = "bike.id")
    RideDTO toRideDto(RideEntity rideEntity);

    default String map(LocalDate date) {
        return date == null ? null : date.toString();
    }

    default LocalDate map(String date) {
        return date == null ? null : LocalDate.parse(date);
    }

    default String map(LocalTime time) {
        return time == null ? null : time.toString();
    }

    default LocalTime mapTime(String time) {
        return time == null ? null : LocalTime.parse(time);
    }
}
