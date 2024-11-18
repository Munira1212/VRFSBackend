package com.example.vrfsbackend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data

public class WaterSource {


        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;

        private String name; // Name of the water source
        private String location; // Location details of the water source
        private String type; // Type of source (e.g., river, lake, well)

        @OneToMany(mappedBy = "waterSource", cascade = CascadeType.ALL)
        private List<WaterQuality> waterQualityRecords; // List of associated water quality records
    }


