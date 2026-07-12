package com.mybikelog.api.mapper;

import com.mybikelog.api.dto.BikeDTO;
import com.mybikelog.api.dto.UsersDTO;
import com.mybikelog.api.entity.BikeEntity;
import com.mybikelog.api.entity.UserEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MapperClass {

    UsersDTO toUsersDto(UserEntity user);

    UserEntity toUsersEntity(UsersDTO dto);

    BikeDTO toBikeDto(BikeEntity bikeEntity);

    List<BikeDTO> toListBikeDto(List<BikeEntity> bikeEntityList);

}
