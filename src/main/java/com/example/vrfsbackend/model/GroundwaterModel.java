package com.example.vrfsbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroundwaterModel {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private double maxTemp;
        private double precip;
        private int humidity;
        private double windSpeed;

        @OneToMany(mappedBy = "groundwaterModel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        private List<WaterQuality> waterQualityRecords = new ArrayList<>();

        // Optional: constructors, getters, and setters can be omitted if you use Lombok
}
