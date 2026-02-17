package com.acpcw1.controller;

import com.acpcw1.data.UrlRequest;
import com.acpcw1.service.PostgresService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/api/v1/acp")
@RestController()
public class PostgresController {
    private final PostgresService postgresService;

    @GetMapping("all/postgres/{table}")
    public List<Map<String, Object>> getTableData(@PathVariable String table) {
        return postgresService.getTableData(table);
    }

    @PostMapping("process/postgres/{table}")
    public void processData(@PathVariable String table,
                            @RequestBody @Valid UrlRequest urlRequest) {

        postgresService.processDroneData(table, urlRequest.getUrlPath());
    }
}
