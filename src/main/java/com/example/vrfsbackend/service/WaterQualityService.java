package com.example.vrfsbackend.service;

import com.example.vrfsbackend.model.WaterQuality;
import com.example.vrfsbackend.repository.WaterQualityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Locale;

@Service
public class WaterQualityService {

    @Autowired
    WaterQualityRepository waterQualityRepository;

    RestTemplate restTemplate = new RestTemplate();

    public WaterQuality assessWaterQuality() {
        // Fetch air quality data

        //coordinate et random sted i kenya
        double latitude = -1.2921;
        double longitude = 36.8219;

        //double latitude = 55.6761;
        // double longitude = 12.5683;   denmark københavn random gade

        /*Breddegrad (latitude): 55.1039° N
        Længdegrad (longitude): 14.7060° E  ... bornholm rønne, meget rent drikkevand*/

        Map<String, Double> airQualityData = fetchAirQualityData(latitude, longitude);

        // Fetch pH value from SoilGrids API
        double pH = fetchPHFromSoilGridsAPI(latitude, longitude);
        if (pH == -1.0) {
            System.err.println("Warning: pH data could not be retrieved from SoilGrids API for the given location. Using fallback pH estimation.");
            pH = estimatePHFromAirQuality(airQualityData); // Estimate the pH value
        }



        // Calculate turbidity based on air quality data
        double turbidity = calculateTurbidityBasedOnAirQuality(airQualityData);

        // Prepare parameters for water quality index calculation
        Map<String, Double> parameters = new HashMap<>();
        parameters.put("pH", pH);
        parameters.put("turbidity", turbidity);

        // Calculate water quality index
        double waterQualityIndex = calculateWaterQualityIndex(parameters);

        // Create and save WaterQuality entity
        WaterQuality waterQuality = new WaterQuality();
        waterQuality.setPH(pH);
        waterQuality.setTurbidity(turbidity);
        waterQuality.setPm10(airQualityData.getOrDefault("pm10", 0.0));
        parameters.put("pm2_5", airQualityData.getOrDefault("pm2_5", 0.0));
        parameters.put("no2", airQualityData.getOrDefault("no2", 0.0));
        parameters.put("o3", airQualityData.getOrDefault("o3", 0.0));
        parameters.put("nh3", airQualityData.getOrDefault("nh3", 0.0));

        waterQuality.setWaterQualityIndex(waterQualityIndex);
        waterQuality.setTimestamp(LocalDateTime.now());

        System.out.printf(waterQuality.toString());

        String waterQualityStatus;
        String reasonForUnsafety = "";

        if (turbidity > 5.0) {
            reasonForUnsafety += "High turbidity level (" + turbidity + " NTU), which exceeds the safe limit of 5.0 NTU. ";
        }
        if (pH < 6.5 || pH > 8.5) {
            reasonForUnsafety += "pH level (" + pH + ") is outside the safe range of 6.5 to 8.5. ";
        }
        if (airQualityData.getOrDefault("pm2_5", 0.0) > 25.0) {
            reasonForUnsafety += "PM2.5 level (" + airQualityData.get("pm2_5") + " µg/m³) is higher than the safe limit of 25.0 µg/m³. ";
        }
        if (airQualityData.getOrDefault("no2", 0.0) > 20.0) {
            reasonForUnsafety += "NO2 level (" + airQualityData.get("no2") + " µg/m³) exceeds the acceptable safe limit of 20.0 µg/m³. ";
        }
        if (airQualityData.getOrDefault("nh3", 0.0) > 1.0) {
            reasonForUnsafety += "Ammonia (NH3) level (" + airQualityData.get("nh3") + " mg/L) is significantly above the safe limit of 1.0 mg/L. ";
        }

        // Set water quality status based on the safety assessment
        if (reasonForUnsafety.isEmpty()) {
            waterQualityStatus = "Clean and safe for drinking.";
        } else {
            waterQualityStatus = "Not safe for drinking. Reasons: " + reasonForUnsafety;
        }

        // Log the water quality status
        System.out.println("Water Quality Status: " + waterQualityStatus);

        // Optionally, set the status in the entity if you want to store it
        //waterQuality.setWaterSource(waterQualityStatus); // Assuming you can use this field

        System.out.println("Saving WaterQuality: " + waterQuality);

        return waterQualityRepository.save(waterQuality);
    }

    public Map<String, Double> fetchAirQualityData(double latitude, double longitude) {
        String url = String.format("https://api.openweathermap.org/data/2.5/air_pollution?lat=%f&lon=%f&appid=f75e27c406ca930d9caa9f6ef57661e4", latitude, longitude);
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            System.out.println("API Response: " + response); // Log the entire response for debugging

            if (response != null && response.containsKey("list")) {
                List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");
                if (!list.isEmpty()) {
                    Map<String, Object> components = (Map<String, Object>) list.get(0).get("components");
                    if (components != null) {
                        Map<String, Double> airQualityComponents = new HashMap<>();
                        for (Map.Entry<String, Object> entry : components.entrySet()) {
                            // Convert value to Double
                            try {
                                airQualityComponents.put(entry.getKey(), Double.parseDouble(entry.getValue().toString()));
                            } catch (NumberFormatException e) {
                                System.err.println("Failed to convert value for key: " + entry.getKey());
                            }
                        }
                        return airQualityComponents;
                    }
                }
            }
            System.err.println("Air quality data is missing or malformed");
        } catch (Exception e) {
            System.err.println("Failed to fetch air quality data: " + e.getMessage());
        }
        return new HashMap<>();
    }


    public double fetchPHFromSoilGridsAPI(double latitude, double longitude) {
        String url = String.format(Locale.US,
                "https://rest.isric.org/soilgrids/v2.0/properties/query?lon=%.6f&lat=%.6f&property=phh2o,ocd,cec&depth=0-5cm,5-15cm,15-30cm,30-60cm&value=mean",
                longitude, latitude);
        System.out.println("API Request URL: " + url);

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null) {
                System.out.println("API Response: " + response);
            }

            if (response != null && response.containsKey("properties")) {
                Map<String, Object> properties = (Map<String, Object>) response.get("properties");
                Double pH = extractPHValue(properties);
                if (pH != null) {
                    return pH;
                }
            }
            System.err.println("pH data is missing or malformed for the given location and depth combination.");
        } catch (Exception e) {
            System.err.println("Failed to fetch pH data: " + e.getMessage());
        }

        return -1.0; // Indicate that the pH could not be retrieved
    }

    public double estimatePHFromAirQuality(Map<String, Double> airQualityData) {
        double pH = 7.0;

        double no2 = airQualityData.getOrDefault("no2", 0.0);
        double so2 = airQualityData.getOrDefault("so2", 0.0);
        double nh3 = airQualityData.getOrDefault("nh3", 0.0);

        // Adjust pH based on air quality pollutants
        pH -= (no2 * 0.02) + (so2 * 0.03);
        pH += nh3 * 0.01;

        // Ensure pH stays within the valid range of 0 to 14
        if (pH < 0) pH = 0;
        if (pH > 14) pH = 14;

        System.out.println("Estimated pH value: " + pH);
        return pH;
    }

    private Double extractPHValue(Map<String, Object> properties) {
        try {
            Map<String, Object> phh2o = (Map<String, Object>) properties.get("phh2o");
            List<Double> values = (List<Double>) phh2o.get("values");
            if (!values.isEmpty()) {
                double pH = values.get(0);
                System.out.println("Retrieved pH value: " + pH);
                return pH;
            }
        } catch (Exception e) {
            System.err.println("Failed to extract pH value: " + e.getMessage());
        }
        return null;
    }

    public double calculateTurbidityBasedOnAirQuality(Map<String, Double> airQualityData) {
        // Generic logic to derive turbidity based on air quality values.
        // Example: using PM10 value to determine the turbidity. Adapt the logic based on the provided data.
        if (airQualityData.containsKey("pm10")) {
            return airQualityData.get("pm10") / 10.0; // Example: scale factor to derive turbidity
        } else {
            return 0.0; // Default value if the required data is missing
        }
    }

    public double calculateWaterQualityIndex(Map<String, Double> parameters) {
        // Generic water quality assessment logic.
        double pH = parameters.getOrDefault("pH", 7.0);
        double turbidity = parameters.getOrDefault("turbidity", 0.0);

        // Calculating the water quality index based on provided parameters.
        double index = (14 - Math.abs(7 - pH)) + (10 - turbidity);

        return index;
    }











  /*  public Map<String, Double> getFallbackSoilData() {
        Map<String, Double> fallbackSoilData = new HashMap<>();
        fallbackSoilData.put("ocd", 1.5);  // Example average organic carbon density
        fallbackSoilData.put("cec", 10.0); // Example average cation exchange capacity
        return fallbackSoilData;
    }



    public double fetchPHFromSoilGridsAPI(double latitude, double longitude) {
        if (!isSoilGridsAvailable()) {
            System.err.println("SoilGrids API is down. Using calculated pH value based on available factors.");
            return -1.0; // Indicate that the pH could not be retrieved
        }

        String url = String.format(Locale.US,
                "https://rest.isric.org/soilgrids/v2.0/properties/query?lon=%.6f&lat=%.6f&property=phh2o,ocd,cec&depth=0-5cm,5-15cm,15-30cm,30-60cm&value=mean",
                longitude, latitude
        );
        System.out.println("API Request URL: " + url);

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null) {
                System.out.println("API Response: " + response);
            }

            if (response != null && response.containsKey("properties")) {
                Map<String, Object> properties = (Map<String, Object>) response.get("properties");
                Double pH = extractPHValue(properties);
                if (pH != null) {
                    return pH;
                }
            }
            System.err.println("pH data is missing or malformed for the given location and depth combination.");
        } catch (Exception e) {
            System.err.println("Failed to fetch pH data: " + e.getMessage());
        }

        return -1.0; // Indicate that the pH could not be retrieved
    }

    public Map<String, Double> fetchSoilData(double latitude, double longitude) {
        String url = String.format(Locale.US,
                "https://rest.isric.org/soilgrids/v2.0/properties/query?lon=%.6f&lat=%.6f&property=ocd,cec&depth=0-5cm,5-15cm,15-30cm,30-60cm&value=mean",
                longitude, latitude
        );
        System.out.println("Fetching soil data: " + url);

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("properties")) {
                Map<String, Object> properties = (Map<String, Object>) response.get("properties");
                Map<String, Double> soilData = new HashMap<>();
                soilData.put("ocd", extractMeanValue(properties, "ocd"));
                soilData.put("cec", extractMeanValue(properties, "cec"));
                return soilData; // Return the fetched soil data if successful
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch soil data: " + e.getMessage());
        }

        // Fallback to static average soil data
        System.err.println("Warning: Soil data could not be retrieved from SoilGrids API. Using fallback soil data.");
        return getFallbackSoilData();
    }

    public Double extractPHValue(Map<String, Object> properties) {
        try {
            Map<String, Object> phh2o = (Map<String, Object>) properties.get("phh2o");
            List<Double> values = (List<Double>) phh2o.get("values");
            if (!values.isEmpty()) {
                double pH = values.get(0);
                System.out.println("Retrieved pH value: " + pH);
                return pH;
            }
        } catch (Exception e) {
            System.err.println("Failed to extract pH value: " + e.getMessage());
        }
        return null;
    }

   /* private Map<String, Double> fetchSoilData(double latitude, double longitude) {
        String url = String.format(Locale.US,
                "https://rest.isric.org/soilgrids/v2.0/properties/query?lon=%.6f&lat=%.6f&property=ocd,cec&depth=0-5cm,5-15cm,15-30cm,30-60cm&value=mean",
                longitude, latitude
        );
        System.out.println("Fetching soil data: " + url);

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("properties")) {
                Map<String, Object> properties = (Map<String, Object>) response.get("properties");
                Map<String, Double> soilData = new HashMap<>();
                soilData.put("ocd", extractMeanValue(properties, "ocd"));
                soilData.put("cec", extractMeanValue(properties, "cec"));
                return soilData; // Return the fetched soil data if successful
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch soil data: " + e.getMessage());
        }

        // Fallback to static average soil data
        System.err.println("Warning: Soil data could not be retrieved from SoilGrids API. Using fallback soil data.");
        return getFallbackSoilData();
    }*/
   /* public double estimatePHFromAirQualityAndFallbackSoilData(Map<String, Double> airQualityData) {
        // Start with a neutral base value of pH
        double pH = 7.0;

        // Use fallback soil data for ocd and cec if actual soil data is unavailable
        Map<String, Double> soilData = getFallbackSoilData();
        double organicCarbon = soilData.getOrDefault("ocd", 0.0);
        double cec = soilData.getOrDefault("cec", 0.0);

        // Organic carbon generally lowers pH
        pH -= organicCarbon * 0.05;  // Example coefficient to account for organic carbon effect

        // Cation exchange capacity buffers pH changes
        pH += cec * 0.01;  // Example coefficient for cec impact on buffering capacity

        // Adjust pH based on air quality pollutants
        double no2 = airQualityData.getOrDefault("no2", 0.0);
        double so2 = airQualityData.getOrDefault("so2", 0.0);
        double nh3 = airQualityData.getOrDefault("nh3", 0.0);

        // Decrease pH for acidifying pollutants
        pH -= (no2 * 0.02) + (so2 * 0.03);  // Example coefficients to reduce pH based on acidifying pollutants

        // Increase pH for ammonia
        pH += nh3 * 0.01;  // Example coefficient for ammonia increasing pH

        // Ensure pH stays within the valid range of 0 to 14
        if (pH < 0) pH = 0;
        if (pH > 14) pH = 14;

        System.out.println("Estimated pH value: " + pH);
        return pH;
    }
   /* public double estimatePHFromSoilAndAirData(Map<String, Double> soilData, Map<String, Double> airQualityData) {
        // Starting point based on soil carbon content (organic carbon generally lowers pH)
        double organicCarbon = soilData.getOrDefault("ocd", 0.0);
        double cec = soilData.getOrDefault("cec", 0.0);  // Cation exchange capacity

        // Use organic carbon to estimate the base pH
        double pH = 7.0 - (organicCarbon * 0.05);  // Example: organic carbon can reduce pH

        // Buffering adjustment from CEC (higher CEC tends to buffer against pH changes)
        pH += (cec * 0.01);  // Example: higher CEC buffers pH

        // Adjust pH further based on air quality pollutants
        double no2 = airQualityData.getOrDefault("no2", 0.0);
        double so2 = airQualityData.getOrDefault("so2", 0.0);
        double nh3 = airQualityData.getOrDefault("nh3", 0.0);

        // Adjust pH using pollutants
        pH -= (no2 * 0.01) + (so2 * 0.02);  // Acidifying pollutants
        pH += (nh3 * 0.01);  // Alkaline pollutant

        // Ensure pH stays within the valid range of 0 to 14
        if (pH < 0) pH = 0;
        if (pH > 14) pH = 14;

        System.out.println("Estimated pH value: " + pH);
        return pH;
    }

   public double extractMeanValue(Map<String, Object> properties, String key) {
        try {
            Map<String, Object> property = (Map<String, Object>) properties.get(key);
            List<Double> values = (List<Double>) property.get("values");
            if (!values.isEmpty()) {
                return values.get(0);
            }
        } catch (Exception e) {
            System.err.println("Failed to extract mean value for " + key + ": " + e.getMessage());
        }
        return 0.0; // Default value if extraction fails
    }*/


}



