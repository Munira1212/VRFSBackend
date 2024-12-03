package com.example.vrfsbackend;
import com.example.vrfsbackend.controller.GroundwaterController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VrfsBackendApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(VrfsBackendApplication.class, args);

        GroundwaterController GWC = new GroundwaterController();
        System.out.println("Testing Groundwater Availability...");
        GWC.getGroundwaterAvailability();
    }

}
