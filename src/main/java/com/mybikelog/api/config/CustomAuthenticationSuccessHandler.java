package com.mybikelog.api.config;

import com.mybikelog.api.entity.UserEntity;
import com.mybikelog.api.service.UserService;
import com.mybikelog.api.util.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@AllArgsConstructor
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JWTUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication)
            throws IOException{
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        String picture = oauthUser.getAttribute("picture");

        UserEntity user = userService.login(email,name,picture);

        String token = jwtUtil.generateToken(String.valueOf(user.getId()));

        response.setContentType("application/json");

        String frontendUrl = "http://127.0.0.1:5500/index.html";
        response.sendRedirect(frontendUrl + "?token=" + token);

    }
}
