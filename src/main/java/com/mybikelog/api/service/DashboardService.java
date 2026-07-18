package com.mybikelog.api.service;

import com.mybikelog.api.entity.BikeEntity;
import com.mybikelog.api.repository.PetrolRepository;
import com.mybikelog.api.repository.RideRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
}
