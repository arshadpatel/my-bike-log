package com.mybikelog.api.controller;

import com.mybikelog.api.dto.MonthlyDashboradDTO;
import com.mybikelog.api.dto.OilStatusDTO;
import com.mybikelog.api.dto.OverallStatsDTO;
import com.mybikelog.api.dto.TyreStatusDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/{bikeId}")
public class DashboardController {

    @GetMapping("/months")
    public ResponseEntity<List<String>> getAllMonths(@PathVariable String bikeId){
        return null;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<MonthlyDashboradDTO> getMonthlyDashboard(@RequestParam String month,
                                                                   @PathVariable String bikeId){
        return null;
    }

    @GetMapping("/overall-stats")
    public ResponseEntity<OverallStatsDTO> getOverallStats(@PathVariable String bikeId){
        return null;
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