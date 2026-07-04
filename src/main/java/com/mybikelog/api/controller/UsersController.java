package com.mybikelog.api.controller;

import com.mybikelog.api.dto.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me")
public class UsersController {

    @GetMapping
    public ResponseEntity<UserResponse> getUserProfile(){
        return null;
    }

    public ResponseEntity<UserResponse> updatePreference(){
        return null;
    }
}
