package com.example.vrfsbackend;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.vrfsbackend")
public class VrfsBackendApplication {

    public static void main(String[] args) {
        // Start the Spring Boot application
        SpringApplication.run(VrfsBackendApplication.class, args);
    }
}


     /*   // Start Spring Boot application and get the application context
        ApplicationContext context = SpringApplication.run(VrfsBackendApplication.class, args);

        // Get the GroundwaterController and WaterQualityController beans from the context
        GroundwaterController GWC = context.getBean(GroundwaterController.class);
        WaterQualityController wqc = context.getBean(WaterQualityController.class);

        // Testing Groundwater Availability
        System.out.println("Testing Groundwater Availability...");
        try {
            GWC.getGroundwaterAvailability();
        } catch (Exception e) {
            System.out.println("Error testing groundwater availability: "+  e);
        }

        // Analyzing water quality
        System.out.println("Analyzing water quality...");
        try {
            ResponseEntity<WaterQuality> response = wqc.assessWaterQuality()
            System.out.println("Water Quality Response: {}" + response.getBody());
        } catch (Exception e) {
            System.out.println("Error analyzing water quality: " + e);
        }
    }*
}
*/