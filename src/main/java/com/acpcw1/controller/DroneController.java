package com.acpcw1.controller;

import com.acpcw1.data.Drone;
import com.acpcw1.data.UrlRequest;
import com.acpcw1.service.DroneService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/acp")
@RestController()
public class DroneController {
    private final DroneService droneService;

    @PostMapping("process/dump")
    public Drone[] dumpDroneData(@RequestBody UrlRequest urlRequest) {
        return droneService.dumpDroneData(urlRequest.getUrlPath());
    }
}
