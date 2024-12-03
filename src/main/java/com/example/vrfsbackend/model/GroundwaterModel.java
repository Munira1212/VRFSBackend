package com.example.vrfsbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
public class GroundwaterModel {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private double maxTemp;
        private double precip;
        private int humidity;
        private double windSpeed;

        @OneToMany(mappedBy = "groundwaterModel") // Reference to the "groundwaterModel" field in WaterQuality
        private List<WaterQuality> waterQualityRecords = new ArrayList<>();

        // Constructor, getters, and setters
}


