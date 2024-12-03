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

    @GetMapping("/getQuality")
    public ResponseEntity<WaterQuality> getWaterQualityByCoordinates(@RequestParam double lat, @RequestParam double lon) {
        try {
            // Use the coordinates provided by the frontend via query parameters
            WaterQuality waterQuality = waterQualityService.assessWaterQuality(lat, lon);
            return ResponseEntity.ok(waterQuality);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }


    @PostMapping("/assess")
    public ResponseEntity<WaterQuality> assessWaterQuality(@RequestBody Map<String, Double> coordinates) {
        try {
            // Extract the latitude and longitude from the request body
            double latitude = coordinates.get("lat");
            double longitude = coordinates.get("lon");

            // Call the service method with the provided coordinates
            WaterQuality waterQuality = waterQualityService.assessWaterQuality(latitude, longitude);

            // Return the calculated water quality
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






