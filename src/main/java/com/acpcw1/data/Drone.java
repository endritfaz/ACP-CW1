package com.acpcw1.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Drone {
    private String name;
    private String id;
    private DroneCapability capability;
    private BigDecimal costPer100Moves;
}
