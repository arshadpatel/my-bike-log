package com.mybikelog.api.service;

import com.mybikelog.api.dto.MonthlyDashboradDTO;
import com.mybikelog.api.entity.BikeEntity;
import com.mybikelog.api.entity.PetrolEntity;
import com.mybikelog.api.entity.RideEntity;
import com.mybikelog.api.repository.PetrolRepository;
import com.mybikelog.api.repository.RideRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@AllArgsConstructor
@Service
public class DashboardService {

    private final CommonService commonService;
    private final RideRepository rideRepository;
    private final PetrolRepository petrolRepository;

    public List<String> getAllMonths(UUID userId, UUID bikeId) {
        BikeEntity bike = commonService.getBikeDetails(userId, bikeId);

        Set<String> months = new TreeSet<>(Comparator.reverseOrder());

        rideRepository.findDistinctDateByBikeId(bike.getId())
                .forEach(date -> months.add(YearMonth.from(date).toString()));
        petrolRepository.findDistinctDateByBikeId(bike.getId())
                .forEach(date -> months.add(YearMonth.from(date).toString()));

        months.add(YearMonth.now().toString());

        return new ArrayList<>(months);
    }

    public MonthlyDashboradDTO getMonthlyDashboard(UUID userId, UUID bikeId, String month) {
        BikeEntity bike = commonService.getBikeDetails(userId, bikeId);

        YearMonth yearMonth = YearMonth.parse(month);

        List<RideEntity> rides = rideRepository.findByBikeIdAndDateBetweenOrderByCreatedAtDesc(
                bike.getId(), yearMonth.atDay(1), yearMonth.atEndOfMonth(),
                Pageable.unpaged()).getContent();

        double kmDrivenThisMonth = calculateTotalDistance(rides);
        int ridingDaysThisMonth = countDistinctRidingDays(rides);

        List<PetrolEntity> petrolEntries = petrolRepository.findByBikeIdAndDateBetweenOrderByCreatedAtDesc(
                bike.getId(), yearMonth.atDay(1), yearMonth.atEndOfMonth(),
                Pageable.unpaged()).getContent();

        double litresThisMonth = calculateTotalLitres(petrolEntries);
        double spendThisMonth = calculateTotalSpend(petrolEntries);
        Double bestMileageThisMonth = findBestMileage(petrolEntries);
        Double currentMileage = findFirstNonNullMileage(petrolEntries);

        return MonthlyDashboradDTO.builder()
                .month(month)
                .kmDrivenThisMonth(kmDrivenThisMonth)
                .litresThisMonth(litresThisMonth)
                .spendThisMonth(spendThisMonth)
                .currentMileage(currentMileage)
                .bestMileageThisMonth(bestMileageThisMonth)
                .ridingDaysThisMonth(ridingDaysThisMonth)
                .totalRidesThisMonth(rides.size())
                .totalOdo(bike.getCurrentOdo())
                .oilStatus(null)
                .build();
    }

    private double calculateTotalDistance(List<RideEntity> rides) {
        return rides.stream()
                .mapToDouble(RideEntity::getDistanceKm)
                .sum();
    }

    private int countDistinctRidingDays(List<RideEntity> rides) {
        return (int) rides.stream()
                .map(RideEntity::getDate)
                .distinct()
                .count();
    }

    private double calculateTotalLitres(List<PetrolEntity> petrolEntries) {
        return petrolEntries.stream()
                .mapToDouble(PetrolEntity::getLitres)
                .sum();
    }

    private double calculateTotalSpend(List<PetrolEntity> petrolEntries) {
        return petrolEntries.stream()
                .mapToDouble(PetrolEntity::getAmount)
                .sum();
    }

    private Double findBestMileage(List<PetrolEntity> petrolEntries) {
        return petrolEntries.stream()
                .map(PetrolEntity::getMileageKmPerLitre)
                .filter(Objects::nonNull)
                .max(Double::compareTo)
                .orElse(null);
    }

    private Double findFirstNonNullMileage(List<PetrolEntity> petrolEntries) {
        return petrolEntries.stream()
                .map(PetrolEntity::getMileageKmPerLitre)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
