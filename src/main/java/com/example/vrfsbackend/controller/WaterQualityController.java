package com.example.vrfsbackend.controller;
import com.example.vrfsbackend.model.WaterQuality;
import com.example.vrfsbackend.service.WaterQualityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/water-quality")


public class WaterQualityController {

    @Autowired
    private WaterQualityService waterQualityService;


    @PostMapping("/assess")
    public ResponseEntity<WaterQuality> assessWaterQuality(@RequestBody Map<String, Double> coordinates) {
        try {

            double latitude = coordinates.get("lat");
            double longitude = coordinates.get("lon");


            WaterQuality waterQuality = waterQualityService.assessWaterQuality(latitude, longitude);

            return ResponseEntity.ok(waterQuality);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }


    @GetMapping("/getQuality")
    public ResponseEntity<WaterQuality> getWaterQualityByCoordinates(@RequestParam double lat, @RequestParam double lon) {
        try {

            WaterQuality waterQuality = waterQualityService.assessWaterQuality(lat, lon);
            return ResponseEntity.ok(waterQuality);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}







