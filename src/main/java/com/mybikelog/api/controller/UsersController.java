package com.mybikelog.api.controller;

import com.mybikelog.api.dto.UsersDTO;
import com.mybikelog.api.service.UsersService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/users/me")
public class UsersController {

    private final UsersService usersService;

    @GetMapping
    public ResponseEntity<UsersDTO> getUserProfile(@AuthenticationPrincipal String id){
        UsersDTO user = usersService.getUser(UUID.fromString(id));
        return ResponseEntity.ok(user);
    }

    @PatchMapping
    public ResponseEntity<UsersDTO> updatePreference(@AuthenticationPrincipal String id,
                                                     UsersDTO usersDTO){
        UsersDTO user= usersService.updateActiveBike(UUID.fromString(id), usersDTO);
        return ResponseEntity.ok(user);
    }
}
