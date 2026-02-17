package com.acpcw1.service;

import com.acpcw1.data.Drone;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Service
public class DroneService {
    public Drone[] dumpDroneData(String url) {
        RestTemplate restTemplate = new RestTemplate();

        Drone[] drones = restTemplate.getForObject(url, Drone[].class);

        // Calculate cost per 100 moves for all drones
        for (Drone drone : drones) {
            BigDecimal costPer100Moves =
                    drone.getCapability().getCostInitial()
                            .add(drone.getCapability().getCostFinal())
                            .add(drone.getCapability().getCostPerMove().scaleByPowerOfTen(2));

            drone.setCostPer100Moves(costPer100Moves);
        }
        return drones;
    }
}
