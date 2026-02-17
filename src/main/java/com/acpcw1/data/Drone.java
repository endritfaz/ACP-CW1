package com.acpcw1.data;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class Drone {
    private String name;
    private String id;
    private DroneCapability capability;
    private BigDecimal costPer100Moves;
}
