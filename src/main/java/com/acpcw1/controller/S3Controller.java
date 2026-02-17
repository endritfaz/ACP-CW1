package com.acpcw1.controller;

import com.acpcw1.data.UrlRequest;
import com.acpcw1.service.S3Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController()
@RequestMapping("/api/v1/acp")
@RequiredArgsConstructor
public class S3Controller {
    private final S3Service s3Service;

    @GetMapping("all/s3/{bucket}")
    public List<JsonNode>  getAllBucketData(@PathVariable String bucket) {
        return s3Service.getAllBucketData(bucket);
    }

    @GetMapping("single/s3/{bucket}/{key}")
    public JsonNode getBucketData(@PathVariable String bucket,
                                  @PathVariable String key) {
        return s3Service.getBucketData(bucket, key);
    }

    @PostMapping("process/s3")
    public void processData(@RequestBody @Valid UrlRequest urlRequest) throws JsonProcessingException {
        s3Service.processData(urlRequest.getUrlPath());
    }

    @PostMapping("copy-content/S3/{table}")
    public void copyData(@PathVariable String table) throws JsonProcessingException {
        s3Service.copyData(table);
    }
}
