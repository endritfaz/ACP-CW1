package com.acpcw1.service;

import com.acpcw1.configuration.PostgresConfiguration;
import com.acpcw1.data.Drone;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostgresService {
    private final JdbcTemplate jdbcTemplate;
    private final DroneService droneService;

    public List<Map<String, Object>> getTableData(String table) {
        String sql = String.format("SELECT * FROM %s", table);
        return jdbcTemplate.queryForList(sql);
    }

    public void processDroneData(String table, String url) {
        Drone[] drones = droneService.dumpDroneData(url);

        for (Drone drone : drones) {
            createDroneUsingJdbc(drone, table);
        }
    }

    @Transactional
    public void createDroneUsingJdbc(Drone drone, String table) {
        String sql = String.format("INSERT INTO %s (id, name, cooling, heating, capacity, max_moves, cost_per_move, cost_initial, cost_final) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", table);

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, drone.getId());
            ps.setString(2, drone.getName());
            ps.setBoolean(3, drone.getCapability().isCooling());
            ps.setBoolean(4, drone.getCapability().isHeating());
            ps.setBigDecimal(5, drone.getCapability().getCapacity());
            ps.setInt(6, drone.getCapability().getMaxMoves());
            ps.setBigDecimal(7, drone.getCapability().getCostPerMove());
            ps.setBigDecimal(8, drone.getCapability().getCostInitial());
            ps.setBigDecimal(9, drone.getCapability().getCostFinal());
            return ps;
        });
    }
}

