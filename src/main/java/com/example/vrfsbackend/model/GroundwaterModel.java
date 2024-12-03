package com.example.vrfsbackend.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroundwaterModel {
        private double maxTemp;
        private double precip;
        private int humidity;
        private double windSpeed;

        // Constructor
        public GroundwaterModel(double maxTemp, double precip, int humidity, double windSpeed) {
                this.maxTemp = maxTemp;
                this.precip = precip;
                this.humidity = humidity;
                this.windSpeed = windSpeed;
        }
}
