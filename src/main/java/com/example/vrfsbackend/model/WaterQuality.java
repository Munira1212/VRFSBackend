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
        private double pm10;
        private double waterQualityIndex;
        private LocalDateTime timestamp;

        @ManyToOne
        @JoinColumn(name = "groundwater_model_id")
        private GroundwaterModel groundwaterModel;


}
