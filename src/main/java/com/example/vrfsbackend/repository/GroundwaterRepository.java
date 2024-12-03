package com.example.vrfsbackend.repository;

import com.example.vrfsbackend.model.GroundwaterModel;
import com.example.vrfsbackend.model.WaterQuality;
import org.json.JSONObject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public interface GroundwaterRepository extends JpaRepository<GroundwaterModel,Integer> {

}
