package com.example.vrfsbackend.controller;


import com.example.vrfsbackend.service.NasaWaterQualityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nasa-water-quality")
public class nasaWaterQualityController {
    @Autowired
    private NasaWaterQualityService nasaWaterQualityService;

    @GetMapping("/fetch")
    public ResponseEntity<String> fetchWaterQualityData(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String boundingBox) {

        String data = nasaWaterQualityService.fetchWaterQualityData(startDate, endDate, boundingBox);
        return ResponseEntity.ok(data);
    }



}
