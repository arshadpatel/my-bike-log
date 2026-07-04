package com.mybikelog.api.controller;

import com.mybikelog.api.dto.UsersDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me")
public class UsersController {

    @GetMapping
    public ResponseEntity<UsersDTO> getUserProfile(){
        return null;
    }

    public ResponseEntity<UsersDTO> updatePreference(){
        return null;
    }
}
