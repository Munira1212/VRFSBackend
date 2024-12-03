package com.example.vrfsbackend;

import com.example.vrfsbackend.controller.WaterQualityController;
import com.example.vrfsbackend.model.WaterQuality;
import com.example.vrfsbackend.controller.GroundwaterController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;

@SpringBootApplication(scanBasePackages = "com.example.vrfsbackend")
public class VrfsBackendApplication {

    public static void main(String[] args) {
        // Start Spring Boot application and get the application context
        ApplicationContext context = SpringApplication.run(VrfsBackendApplication.class, args);

        // Get the WaterQualityController bean from the context
        WaterQualityController wqc = context.getBean(WaterQualityController.class);

        // Now you can call the method, and dependencies will be properly injected
        System.out.println("Analyzing water quality...");
        ResponseEntity<WaterQuality> response = wqc.assessWaterQuality();
        System.out.println(response.getBody());
  //UGBAAAD))))
        SpringApplication.run(VrfsBackendApplication.class, args);

        GroundwaterController GWC = new GroundwaterController();
        System.out.println("Testing Groundwater Availability...");
        GWC.getGroundwaterAvailability();
    }



}
