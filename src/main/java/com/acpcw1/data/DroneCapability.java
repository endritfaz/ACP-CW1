package com.acpcw1.data;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DroneCapability {
    private boolean cooling;
    private boolean heating;
    private BigDecimal capacity;
    private int maxMoves;
    private BigDecimal costPerMove;
    private BigDecimal costInitial;
    private BigDecimal costFinal;
}
