package com.example.vrfsbackend.service;

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

    public JSONObject fetchAPIData(String apiUrl) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            System.out.println("Error: Received HTTP response code " + responseCode);
            return null;
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }

        if (response.toString().isEmpty()) {
            System.out.println("Error: Empty data returned from API.");
            return null;
        }

        return new JSONObject(response.toString());
    }

    // Analyze weather data for favorable conditions
    public double analyzeWeatherData(JSONObject weatherData) {
        if (!weatherData.has("data")) {
            System.out.println("Error: 'data' key not found in weather data.");
            return 0.0;
        }

        int validDays = 0, favorableWeatherDays = 0;
        var dailyData = weatherData.getJSONArray("data");
        for (int i = 0; i < dailyData.length(); i++) {
            JSONObject day = dailyData.getJSONObject(i);
            double temp = day.optDouble("temp", Double.NaN);
            double precip = day.optDouble("precip", Double.NaN);
            double wind = day.optDouble("wind_spd", Double.NaN);
            double hum = day.optDouble("rh", Double.NaN);

            if (!Double.isNaN(temp) && temp <= 40 && precip > 0 && hum >= 50 && wind <= 15) {
                favorableWeatherDays++;
            }
            validDays++;
        }

        return (validDays > 0) ? ((double) favorableWeatherDays / validDays * 100) : 0.0;
    }

    // Analyze soil moisture data for favorable conditions
    public double analyzeSoilMoistureData(JSONObject soilMoistureData) {
        if (!soilMoistureData.has("properties")) {
            System.out.println("Error: 'properties' key not found in soil moisture data.");
            return 0.0;
        }

        var properties = soilMoistureData.getJSONObject("properties");
        if (!properties.has("parameter")) {
            System.out.println("Error: 'parameter' key not found in soil moisture data.");
            return 0.0;
        }

        var parameter = properties.getJSONObject("parameter");
        if (!parameter.has("GWETROOT")) {
            System.out.println("Error: 'GWETROOT' key not found in soil moisture data.");
            return 0.0;
        }

        var gwetrootData = parameter.getJSONObject("GWETROOT");
        int validDays = 0, favorableDays = 0;
        for (String dateKey : gwetrootData.keySet()) {
            double soilMoisture = gwetrootData.optDouble(dateKey, -1.0);
            if (soilMoisture != -999.0 && soilMoisture >= 0) {
                validDays++;
                if (soilMoisture > 0.3) favorableDays++;
            }
        }

        return (validDays > 0) ? ((double) favorableDays / validDays * 100) : 0.0;
    }

    // Calculate ET score from weather data
    public Map<String, Double> calculateETScore(JSONObject weatherData) {
        Map<String, Double> etResults = new HashMap<>();
        if (!weatherData.has("data")) {
            System.out.println("Error: 'data' key not found in weather data for ET calculation.");
            etResults.put("etScore", 0.0);
            etResults.put("etAverage", 0.0);
            return etResults;
        }

        var dailyData = weatherData.getJSONArray("data");
        double totalET = 0;
        int validDays = 0;
        for (int i = 0; i < dailyData.length(); i++) {
            var day = dailyData.getJSONObject(i);
            double temp = day.optDouble("temp", Double.NaN);
            double hum = day.optDouble("rh", Double.NaN);
            double wind = day.optDouble("wind_spd", Double.NaN);
            if (!Double.isNaN(temp) && !Double.isNaN(hum) && !Double.isNaN(wind)) {
                double etValue = 0.0023 * ((temp + 17.8) * Math.sqrt(wind)) * ((hum / 100) * 0.408);
                totalET += etValue;
                validDays++;
            }
        }

        double etAverage = (validDays > 0) ? (totalET / validDays) : 0.0;
        etResults.put("etScore", (1.0 - etAverage / 10.0) * 100); // Example scoring
        etResults.put("etAverage", etAverage);
        return etResults;
    }
}
