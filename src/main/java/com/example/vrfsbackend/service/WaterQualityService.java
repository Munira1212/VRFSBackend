package com.example.vrfsbackend.service;

import com.example.vrfsbackend.model.WaterQuality;
import com.example.vrfsbackend.model.WaterSource;
import com.example.vrfsbackend.repository.WaterQualityRepository;
import com.example.vrfsbackend.repository.WaterSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service

public class WaterQualityService {

        @Autowired
        private WaterQualityRepository  waterQualityRepository;

        @Autowired
        private WaterSourceRepository waterSourceRepository;

    public void saveWaterQualityRecord(int sourceId, double pH, double turbidity) {
        // Fetch the water source dynamically
        WaterSource waterSource = waterSourceRepository.findById(sourceId)
                .orElseThrow(() -> new RuntimeException("Water source not found"));

        // Create a new water quality record
        WaterQuality waterQuality = new WaterQuality();
        waterQuality.setPH(pH);
        waterQuality.setTurbidity(turbidity);
        waterQuality.setTimestamp(LocalDateTime.now());
        waterQuality.setWaterSource(waterSource);

        // Save the water quality record to the database
        waterQualityRepository.save(waterQuality);
    }


        public List<WaterQuality> getWaterQualityBySource(int sourceId) {
            WaterSource waterSource = waterSourceRepository.findById(sourceId)
                    .orElseThrow(() -> new RuntimeException("Water source not found"));

            return waterSource.getWaterQualityRecords();
        }
    }


