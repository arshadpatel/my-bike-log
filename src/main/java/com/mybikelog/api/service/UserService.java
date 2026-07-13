package com.mybikelog.api.service;

import com.mybikelog.api.dto.UsersDTO;
import com.mybikelog.api.entity.UserEntity;
import com.mybikelog.api.mapper.MapperClass;
import com.mybikelog.api.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.time.Instant;

@AllArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final MapperClass mapperClass;

    public UsersDTO getUser(UUID uuid){

        UserEntity userEntity = userRepository.findById(uuid).orElse(null);

        return mapperClass.toUsersDto(userEntity);
    }

    public UsersDTO updateActiveBike(UUID uuid, UsersDTO usersDTO) {

        UserEntity userEntity = userRepository.getReferenceById(uuid);
        userEntity.setActiveBikeId(usersDTO.getActiveBikeId());
        UserEntity updatedUser = userRepository.save(userEntity);

        return mapperClass.toUsersDto(updatedUser);
    }

    public UserEntity login(String email, String name, String picture) {

        return userRepository.findByEmail(email)
                .orElseGet(() -> {

                    UserEntity user = UserEntity.builder()
                            .email(email)
                            .name(name)
                            .pictureUrl(picture)
                            .createdAt(Instant.now())
                            .build();

                    return userRepository.save(user);
                });
    }
}
