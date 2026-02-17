package com.acpcw1.controller;

import com.acpcw1.data.UrlRequest;
import com.acpcw1.service.DynamoDbService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/api/v1/acp")
@RequiredArgsConstructor
public class DynamoDbController {
    private final DynamoDbService dynamoDbService;

    @GetMapping("all/dynamo/{table}")
    public List<JsonNode> readAllData(@PathVariable String table) {
        return dynamoDbService.readAllData(table);
    }

    @GetMapping("single/dynamo/{table}/{key}")
    public JsonNode readData(@PathVariable String table,
                             @PathVariable String key) {
        return dynamoDbService.readData(table, key);
    }

    @PostMapping("process/dynamo")
    public void processData(@RequestBody UrlRequest urlRequest) throws JsonProcessingException {
        dynamoDbService.processData(urlRequest.getUrlPath());
    }

    @PostMapping("p")
}
