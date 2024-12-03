package com.example.vrfsbackend.service;

import com.example.vrfsbackend.repository.GroundwaterRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Service
public class GroundwaterService {


    // Fetch weather data from the API
    public JSONObject fetchWeatherData(String apiUrl) throws Exception {
        return fetchAPIData(apiUrl);
    }

    public JSONObject fetchAPIData(String apiUrl) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            System.out.println("Error: Received HTTP response code " + responseCode);
            return null;
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        String responseString = response.toString();
        if (responseString.isEmpty()) {
            System.out.println("Error: Empty data returned from API.");
            return null;
        }

        return new JSONObject(responseString);
    }

    // Fetch soil moisture data from the API
    public JSONObject fetchSoilMoistureData(String apiUrl) throws Exception {
        return fetchAPIData(apiUrl);
    }

    // Analyze weather data for favorable conditions
    public double analyzeWeatherData(JSONObject weatherData) {
        if (!weatherData.has("data")) {
            System.out.println("Error: 'data' key not found in weather data.");
            return 0.0;
        }

        JSONArray dailyData = weatherData.getJSONArray("data");
        int validDays = 0;
        int favorableWeatherDays = 0;

        for (int i = 0; i < dailyData.length(); i++) {
            JSONObject day = dailyData.getJSONObject(i);

            double temp = day.optDouble("temp", Double.NaN);
            double precip = day.optDouble("precip", Double.NaN);
            double wind = day.optDouble("wind_spd", Double.NaN);
            double hum = day.optDouble("rh", Double.NaN);

            if (Double.isNaN(temp) || Double.isNaN(precip) || Double.isNaN(wind) || Double.isNaN(hum)) {
                continue;
            }

            if (temp <= 40 && precip > 0 && hum >= 50 && wind <= 15) {
                favorableWeatherDays++;
            }

            validDays++;
        }

        return (validDays > 0) ? (favorableWeatherDays * 100.0 / validDays) : 0.0;
    }

    // Analyze soil moisture data for favorable conditions
    public double analyzeSoilMoistureData(JSONObject soilMoistureData) {
        if (!soilMoistureData.has("properties")) {
            System.out.println("Error: 'properties' key not found in soil moisture data.");
            return 0.0;
        }

        JSONObject properties = soilMoistureData.getJSONObject("properties");
        if (!properties.has("parameter")) {
            System.out.println("Error: 'parameter' key not found in soil moisture data.");
            return 0.0;
        }

        JSONObject parameter = properties.getJSONObject("parameter");
        if (!parameter.has("GWETROOT")) {
            System.out.println("Error: 'GWETROOT' key not found in soil moisture data.");
            return 0.0;
        }

        JSONObject gwetrootData = parameter.getJSONObject("GWETROOT");
        int validDays = 0;
        int favorableDays = 0;

        for (String dateKey : gwetrootData.keySet()) {
            double soilMoisture = gwetrootData.optDouble(dateKey, -1.0);

            // Skip invalid data represented by -999.0
            if (soilMoisture == -999.0) {
                continue; // Skip the day with missing data
            }

            // Debug: Log raw soil moisture values for each date
            // System.out.println("Date: " + dateKey + ", Soil Moisture: " + soilMoisture);

            if (soilMoisture >= 0) {
                validDays++;
                if (soilMoisture > 0.3) {  // Change to > 30 for percentage scale, or leave for fraction scale
                    favorableDays++;
                }
            }
        }

        return (validDays > 0) ? (favorableDays * 100.0 / validDays) : 0.0;
    }


    // Calculate ET score from weather data
    public Map<String, Double> calculateETScore(JSONObject weatherData) {
        Map<String, Double> etResults = new HashMap<>();
        double totalET = 0.0;
        int validDays = 0;

        if (!weatherData.has("data")) {
            System.out.println("Error: 'data' key not found in weather data for ET calculation.");
            etResults.put("etScore", 0.0);
            etResults.put("etAverage", 0.0);
            return etResults;
        }

        JSONArray dailyData = weatherData.getJSONArray("data");

        for (int i = 0; i < dailyData.length(); i++) {
            JSONObject day = dailyData.getJSONObject(i);

            double temp = day.optDouble("temp", Double.NaN);
            double hum = day.optDouble("rh", Double.NaN);
            double wind = day.optDouble("wind_spd", Double.NaN);

            if (Double.isNaN(temp) || Double.isNaN(hum) || Double.isNaN(wind) || temp < -10 || temp > 50) {
                continue;
            }

            // Improved ET formula
            double etValue = 0.0023 * ((temp + 17.8) * Math.sqrt(wind)) * ((hum / 100) * 0.408);
            totalET += etValue;
            validDays++;
        }

        double etAverage = validDays > 0 ? (totalET / validDays) : 0.0;

        // Dynamic scoring
        double minET = 0.0;
        double maxET = 10.0;
        double etScore = 100.0 * ((maxET - etAverage) / (maxET - minET));
        etScore = Math.max(0, Math.min(etScore, 100.0)); // Clamp to 0-100%

        etResults.put("etScore", etScore);
        etResults.put("etAverage", etAverage);

        return etResults;
    }

    public double getSoilMoistureFromAPI(JSONObject soilMoistureData) {
        if (soilMoistureData.has("properties")) {
            JSONObject properties = soilMoistureData.getJSONObject("properties");
            if (properties.has("parameter")) {
                JSONObject parameter = properties.getJSONObject("parameter");
                if (parameter.has("GWETROOT")) {
                    JSONObject gwetrootData = parameter.getJSONObject("GWETROOT");
                    // Assuming we are calculating the soil moisture value based on the data
                    // For simplicity, we take the first available day
                    String firstDay = gwetrootData.keys().next();
                    return gwetrootData.getDouble(firstDay); // Return the first day's soil moisture value
                }
            }
        }
        return 0.0; // Default if data is not found
    }

    // Helper method to extract water table depth from the weather API data (or calculate it)
    public double getWaterTableDepthFromWeatherData(JSONObject weatherData) {
        // Extract water table depth from weather data or assume a default value
        // For example, we might assume a shallow water table based on precipitation levels
        if (weatherData.has("data")) {
            JSONArray weatherDataArray = weatherData.getJSONArray("data");
            // Let's assume we extract a parameter like precipitation that affects water table depth
            JSONObject firstDayWeather = weatherDataArray.getJSONObject(0);
            double precipitation = firstDayWeather.getDouble("precipitation"); // Precipitation in mm

            // Hypothetically, calculate water table depth based on precipitation (very simplistic assumption)
            return precipitation * 0.0001; // This is just an example of how you might derive it
        }
        return 0.0005; // Default if no data available
    }

    public boolean depthandtable(JSONObject soilMoistureData, JSONObject weatherData) {
        try {
            // Get soil moisture (GWETROOT) from the soil moisture data
            double soilMoisture = soilMoistureData.getJSONObject("properties")
                    .getJSONObject("parameter")
                    .getJSONObject("GWETROOT")
                    .optDouble("value", 0.0); // Default to 0.0 if not found

            // Get precipitation (P) from the weather data (in mm)
            double precipitation = weatherData.getJSONArray("data")
                    .getJSONObject(0)
                    .optDouble("precip", 0.0); // Default to 0.0 if not found

            // Get evapotranspiration (ET) from the weather data (in mm or as a derived value)
            double evapotranspiration = weatherData.getJSONArray("data")
                    .getJSONObject(0)
                    .optDouble("ET", 0.0); // Default to 0.0 if not found

            // Estimate surface runoff (Q) as 20% of precipitation
            double surfaceRunoff = precipitation * 0.20;  // Assuming 20% of precipitation is runoff

            // Calculate groundwater recharge (R)
            double groundwaterRecharge = precipitation - evapotranspiration - surfaceRunoff;

            // Print the intermediate values for debugging
            System.out.println("Precipitation (P): " + precipitation);
            System.out.println("Evapotranspiration (ET): " + evapotranspiration);
            System.out.println("Surface Runoff (Q): " + surfaceRunoff);
            System.out.println("Groundwater Recharge (R): " + groundwaterRecharge);
            System.out.println("Soil Moisture: " + soilMoisture);

            // Evaluate if there's potential for groundwater
            if (groundwaterRecharge >= 0.76 && soilMoisture >= 0.76) {
                return true;  // Groundwater potential exists
            } else {
                System.out.println("No potential for groundwater. Recharge is insufficient.");
                return false;  // No potential for groundwater
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;   // In case of any error, return false
        }
    }



}
