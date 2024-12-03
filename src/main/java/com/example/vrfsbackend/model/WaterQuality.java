package com.example.vrfsbackend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data

public class WaterQuality {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private double pH; // pH level of the water
        private double turbidity; // Turbidity of the water
        private double pm10; // Air quality parameter (PM10)
        private double waterQualityIndex; // Calculated water quality index
        private LocalDateTime timestamp; // Timestamp when the data was recorded

        @ManyToOne
        @JoinColumn(name = "groundwater_model_id") // Foreign key column
        private GroundwaterModel groundwaterModel; // Relationship to the GroundwaterModel entity


}
