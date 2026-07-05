package com.mybikelog.api.mapper;

import com.mybikelog.api.dto.UsersDTO;
import com.mybikelog.api.entity.Users;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MapperClass {
    UsersDTO toUsersDto(Users user);

    Users toUsersEntity(UsersDTO dto);
}
