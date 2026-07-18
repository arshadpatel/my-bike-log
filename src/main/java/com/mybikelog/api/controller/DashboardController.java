package com.mybikelog.api.controller;

import com.mybikelog.api.dto.MonthlyDashboradDTO;
import com.mybikelog.api.dto.OilStatusDTO;
import com.mybikelog.api.dto.OverallStatsDTO;
import com.mybikelog.api.dto.TyreStatusDTO;
import com.mybikelog.api.service.DashboardService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/{bikeId}")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/months")
    public ResponseEntity<List<String>> getAllMonths(@AuthenticationPrincipal String id,
                                                     @PathVariable String bikeId){
        List<String> monthsList = dashboardService.getAllMonths(
                UUID.fromString(id), UUID.fromString(bikeId));
        return ResponseEntity.ok(monthsList);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<MonthlyDashboradDTO> getMonthlyDashboard(@AuthenticationPrincipal String id,
                                                                   @RequestParam String month,
                                                                   @PathVariable String bikeId){
        MonthlyDashboradDTO monthlyDashboradDTO = dashboardService.getMonthlyDashboard(
                UUID.fromString(id), UUID.fromString(bikeId), month);
        return ResponseEntity.ok(monthlyDashboradDTO);
    }

    @GetMapping("/overall-stats")
    public ResponseEntity<OverallStatsDTO> getOverallStats(@AuthenticationPrincipal String id,
                                                           @PathVariable String bikeId){
        OverallStatsDTO overallStatsDTO = dashboardService.getOverallStats(
                UUID.fromString(id), UUID.fromString(bikeId));
        return ResponseEntity.ok(overallStatsDTO);
    }

    @GetMapping("/oil-status")
    public ResponseEntity<OilStatusDTO> getOilStatus(@PathVariable String bikeId){
        return null;
    }

    @GetMapping("/tyre-status")
    public ResponseEntity<TyreStatusDTO> getTyreStatus(@PathVariable String bikeId){
        return null;
    }
}