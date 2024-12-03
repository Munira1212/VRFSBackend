package com.example.vrfsbackend.controller;
import com.example.vrfsbackend.model.WaterQuality;
import com.example.vrfsbackend.service.WaterQualityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/water-quality")


public class WaterQualityController {

        @Autowired
        private WaterQualityService waterQualityService;


    @PostMapping("/assess") // Denne metode er en POST-mapping
    public ResponseEntity<WaterQuality> assessWaterQuality() {
        try {
            WaterQuality waterQuality = waterQualityService.assessWaterQuality();
            return ResponseEntity.ok(waterQuality);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    }

   /* @PostMapping("/save")
    public ResponseEntity<String> saveWaterQualityRecord(
            @RequestParam int sourceId,
            @RequestParam double pH,
            @RequestParam double turbidity) {

        waterQualityService.saveWaterQualityRecord(sourceId, pH, turbidity);
        return ResponseEntity.ok("Water quality record saved successfully.");
    }

    @GetMapping("/{sourceId}")
    public ResponseEntity<List<WaterQuality>> getWaterQualityBySource(@PathVariable int sourceId) {
        List<WaterQuality> waterQualityList = waterQualityService.getWaterQualityBySource(sourceId);
        return ResponseEntity.ok(waterQualityList);
    }*/






