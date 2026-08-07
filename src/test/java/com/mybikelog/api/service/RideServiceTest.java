package com.mybikelog.api.service;

import com.mybikelog.api.dto.PageDTO;
import com.mybikelog.api.dto.RideDTO;
import com.mybikelog.api.entity.BikeEntity;
import com.mybikelog.api.entity.RideEntity;
import com.mybikelog.api.mapper.MapperClass;
import com.mybikelog.api.repository.BikeRepository;
import com.mybikelog.api.repository.RideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock
    private BikeRepository bikeRepository;

    @Mock
    private RideRepository rideRepository;

    @Mock
    private MapperClass mapperClass;

    @Mock
    private CommonService commonService;

    @InjectMocks
    private RideService rideService;

    private UUID userId;
    private UUID bikeId;
    private BikeEntity bike;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        bikeId = UUID.randomUUID();
        bike = BikeEntity.builder()
                .id(bikeId)
                .initialOdo(1000.0)
                .currentOdo(1000.0)
                .build();
    }

    // ---------- addRide ----------

    @Test
    void addRide_whenNoPreviousRide_calculatesDistanceFromInitialOdo() {
        RideDTO request = new RideDTO();
        request.setOdo(1200.0);

        RideEntity mappedEntity = new RideEntity();
        RideEntity savedEntity = RideEntity.builder().id(UUID.randomUUID()).build();
        RideDTO responseDto = new RideDTO();

        when(commonService.getBikeDetails(userId, bikeId)).thenReturn(bike);
        when(rideRepository.findTopByBikeIdOrderByOdoDesc(bikeId)).thenReturn(Optional.empty());
        when(mapperClass.toRideEntity(request)).thenReturn(mappedEntity);
        when(rideRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(mapperClass.toRideDto(savedEntity)).thenReturn(responseDto);

        RideDTO result = rideService.addRide(userId, bikeId, request);

        assertThat(result).isEqualTo(responseDto);
        assertThat(mappedEntity.getDistanceKm()).isEqualTo(200.0); // 1200 - initialOdo(1000)
        assertThat(mappedEntity.getBike()).isEqualTo(bike);
        verify(commonService).updateBikeCurrentOdo(bike);
    }

    @Test
    void addRide_whenPreviousRideExists_calculatesDistanceFromPreviousRide() {
        RideDTO request = new RideDTO();
        request.setOdo(1500.0);

        RideEntity previousRide = RideEntity.builder().odo(1200.0).build();
        RideEntity mappedEntity = new RideEntity();
        RideEntity savedEntity = RideEntity.builder().id(UUID.randomUUID()).build();

        when(commonService.getBikeDetails(userId, bikeId)).thenReturn(bike);
        when(rideRepository.findTopByBikeIdOrderByOdoDesc(bikeId)).thenReturn(Optional.of(previousRide));
        when(mapperClass.toRideEntity(request)).thenReturn(mappedEntity);
        when(rideRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(mapperClass.toRideDto(savedEntity)).thenReturn(new RideDTO());

        rideService.addRide(userId, bikeId, request);

        assertThat(mappedEntity.getDistanceKm()).isEqualTo(300.0); // 1500 - 1200
    }

    @Test
    void addRide_whenOdoLessThanBikeCurrentOdo_throwsRuntimeException() {
        RideDTO request = new RideDTO();
        request.setOdo(500.0); // less than bike.currentOdo (1000)

        when(commonService.getBikeDetails(userId, bikeId)).thenReturn(bike);

        assertThatThrownBy(() -> rideService.addRide(userId, bikeId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Enter Correct Odometer Reading");

        verify(rideRepository, never()).save(any());
    }

    // ---------- getAllRides ----------

    @Test
    void getAllRides_whenMonthIsNull_fetchesOrderedByCreatedAt() {
        Page<RideEntity> page = new PageImpl<>(List.of(RideEntity.builder().build()));
        PageDTO<RideDTO> expected = PageDTO.<RideDTO>builder().build();

        when(commonService.getBikeDetails(userId, bikeId)).thenReturn(bike);
        when(rideRepository.findByBikeIdOrderByCreatedAtDesc(eq(bikeId), any(Pageable.class)))
                .thenReturn(page);
        doReturn(expected).when(mapperClass).toPageDto(eq(page), any());

        PageDTO<RideDTO> result = rideService.getAllRides(userId, bikeId, 0, 10, null);

        assertThat(result).isEqualTo(expected);
        verify(rideRepository, never()).findByBikeIdAndDateBetweenOrderByCreatedAtDesc(
                any(), any(), any(), any());
    }

    @Test
    void getAllRides_whenMonthProvided_fetchesRidesForThatMonth() {
        Page<RideEntity> page = new PageImpl<>(List.of(RideEntity.builder().build()));
        PageDTO<RideDTO> expected = PageDTO.<RideDTO>builder().build();

        when(commonService.getBikeDetails(userId, bikeId)).thenReturn(bike);
        when(rideRepository.findByBikeIdAndDateBetweenOrderByCreatedAtDesc(
                eq(bikeId), eq(LocalDate.of(2026, 7, 1)), eq(LocalDate.of(2026, 7, 31)), any(Pageable.class)))
                .thenReturn(page);
        doReturn(expected).when(mapperClass).toPageDto(eq(page), any());

        PageDTO<RideDTO> result = rideService.getAllRides(userId, bikeId, 0, 10, "2026-07");

        assertThat(result).isEqualTo(expected);
    }

    // ---------- deleteRide ----------

    @Test
    void deleteRide_whenRideIsLatest_deletesAndUpdatesOdo() {
        UUID rideId = UUID.randomUUID();
        RideEntity ride = RideEntity.builder().id(rideId).build();

        when(commonService.getBikeDetails(bikeId)).thenReturn(bike);
        when(rideRepository.findByIdAndBikeId(rideId, bikeId)).thenReturn(Optional.of(ride));
        when(rideRepository.findTopByBikeIdOrderByOdoDesc(bikeId)).thenReturn(Optional.of(ride));

        rideService.deleteRide(bikeId, rideId);

        verify(rideRepository).delete(ride);
        verify(commonService).updateBikeCurrentOdo(bike);
    }

    @Test
    void deleteRide_whenRideNotFound_throwsRuntimeException() {
        UUID rideId = UUID.randomUUID();

        when(commonService.getBikeDetails(bikeId)).thenReturn(bike);
        when(rideRepository.findByIdAndBikeId(rideId, bikeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rideService.deleteRide(bikeId, rideId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ride Not Found");

        verify(rideRepository, never()).delete(any());
    }

    @Test
    void deleteRide_whenNoLatestRideFound_throwsRuntimeException() {
        UUID rideId = UUID.randomUUID();
        RideEntity ride = RideEntity.builder().id(rideId).build();

        when(commonService.getBikeDetails(bikeId)).thenReturn(bike);
        when(rideRepository.findByIdAndBikeId(rideId, bikeId)).thenReturn(Optional.of(ride));
        when(rideRepository.findTopByBikeIdOrderByOdoDesc(bikeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rideService.deleteRide(bikeId, rideId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No Latest Ride Found");

        verify(rideRepository, never()).delete(any());
    }

    @Test
    void deleteRide_whenRideIsNotLatest_throwsRuntimeException() {
        UUID rideId = UUID.randomUUID();
        RideEntity ride = RideEntity.builder().id(rideId).build();
        RideEntity latestRide = RideEntity.builder().id(UUID.randomUUID()).build();

        when(commonService.getBikeDetails(bikeId)).thenReturn(bike);
        when(rideRepository.findByIdAndBikeId(rideId, bikeId)).thenReturn(Optional.of(ride));
        when(rideRepository.findTopByBikeIdOrderByOdoDesc(bikeId)).thenReturn(Optional.of(latestRide));

        assertThatThrownBy(() -> rideService.deleteRide(bikeId, rideId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only Latest ride can be deleted");

        verify(rideRepository, never()).delete(any());
    }
}