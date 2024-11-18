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
        private LocalDateTime timestamp; // Timestamp when the data was recorded

        @ManyToOne
        @JoinColumn(name = "water_source_id", nullable = false) // Foreign key to WaterSource
        private WaterSource waterSource; // Link to the associated water source

}
