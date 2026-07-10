package com.mybikelog.api.mapper;

import com.mybikelog.api.dto.UsersDTO;
import com.mybikelog.api.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MapperClass {
    UsersDTO toUsersDto(UserEntity user);

    UserEntity toUsersEntity(UsersDTO dto);
}
