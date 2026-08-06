package com.mybikelog.api.service;

import com.mybikelog.api.dto.UsersDTO;
import com.mybikelog.api.entity.UserEntity;
import com.mybikelog.api.exception.ResourceNotFoundException;
import com.mybikelog.api.mapper.MapperClass;
import com.mybikelog.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MapperClass mapperClass;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private UserEntity userEntity;
    private UsersDTO usersDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userEntity = UserEntity.builder()
                .id(userId)
                .email("test@example.com")
                .name("Test User")
                .createdAt(Instant.now())
                .build();
        usersDTO = new UsersDTO();
        usersDTO.setId(userId);
        usersDTO.setEmail("test@example.com");
        usersDTO.setName("Test User");
    }

    @Test
    void getUser_whenUserExists_returnsUsersDto() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(mapperClass.toUsersDto(userEntity)).thenReturn(usersDTO);

        UsersDTO result = userService.getUser(userId);

        assertThat(result).isEqualTo(usersDTO);
        verify(userRepository).findById(userId);
    }

    @Test
    void getUser_whenUserDoesNotExist_throwsResourceNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(userId.toString());
    }

    @Test
    void updateActiveBike_whenUserExists_updatesAndReturnsDto() {
        UUID activeBikeId = UUID.randomUUID();
        UsersDTO request = new UsersDTO();
        request.setActiveBikeId(activeBikeId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(mapperClass.toUsersDto(userEntity)).thenReturn(usersDTO);

        UsersDTO result = userService.updateActiveBike(userId, request);

        assertThat(result).isEqualTo(usersDTO);
        assertThat(userEntity.getActiveBikeId()).isEqualTo(activeBikeId);
        verify(userRepository).save(userEntity);
    }

    @Test
    void updateActiveBike_whenUserDoesNotExist_throwsResourceNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateActiveBike(userId, usersDTO))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_whenEmailExists_returnsExistingUser() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userEntity));

        UserEntity result = userService.login("test@example.com", "Test User", "pic.jpg");

        assertThat(result).isEqualTo(userEntity);
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_whenEmailDoesNotExist_createsAndSavesNewUser() {
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UserEntity result = userService.login("new@example.com", "New User", "pic.jpg");

        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getName()).isEqualTo("New User");
        verify(userRepository).save(any(UserEntity.class));
    }

}