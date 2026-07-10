package com.mybikelog.api.service;

import com.mybikelog.api.dto.UsersDTO;
import com.mybikelog.api.entity.UserEntity;
import com.mybikelog.api.mapper.MapperClass;
import com.mybikelog.api.repository.UsersRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.time.Instant;

@AllArgsConstructor
@Service
public class UsersService {

    private final UsersRepository usersRepository;
    private final MapperClass mapperClass;

    public UsersDTO getUser(UUID uuid){

        UserEntity userEntity = usersRepository.getReferenceById(uuid);

        return mapperClass.toUsersDto(userEntity);
    }

    public UsersDTO updateActiveBike(UUID uuid, UsersDTO usersDTO) {

        UserEntity userEntity = usersRepository.getReferenceById(uuid);
        userEntity.setActiveBikeId(usersDTO.getActiveBikeId());
        UserEntity updatedUser = usersRepository.save(userEntity);

        return mapperClass.toUsersDto(updatedUser);
    }


    public Users login(String email, String name, String picture) {

        return usersRepository.findByEmail(email)
                .orElseGet(() -> {

                    Users user = Users.builder()
                            .email(email)
                            .name(name)
                            .pictureUrl(picture)
                            .createdAt(Instant.now())
                            .build();

                    return usersRepository.save(user);
                });
    }
}
