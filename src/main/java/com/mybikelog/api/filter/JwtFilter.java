package com.mybikelog.api.filter;

import com.mybikelog.api.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

import static com.mybikelog.api.util.AppConstants.AUTHORIZATION_HEADER;
import static com.mybikelog.api.util.AppConstants.TOKEN_PREFIX;

@AllArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if(authHeader != null && authHeader.startsWith(TOKEN_PREFIX)){
            try {
                String token = authHeader.substring(TOKEN_PREFIX.length());
                if(!jwtUtil.isTokenExpired(token) && SecurityContextHolder.getContext().getAuthentication() == null){
                    String id = jwtUtil.extractId(token);
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            id, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                // TODO Need to implement custom Exception
                throw new RuntimeException(e);
            }
        }
        filterChain.doFilter(request, response);
    }

}
