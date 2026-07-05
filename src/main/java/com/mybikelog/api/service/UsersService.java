package com.mybikelog.api.service;

import com.mybikelog.api.entity.Users;
import com.mybikelog.api.repository.UsersRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class UsersService {

    private final UsersRepository usersRepository;

    public UsersService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
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
