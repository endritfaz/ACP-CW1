package com.acpcw1.service;

import com.acpcw1.configuration.S3Configuration;
import com.acpcw1.configuration.SystemEnvironment;
import com.acpcw1.data.Drone;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final ObjectMapper mapper =
            new ObjectMapper().enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

    private final S3Configuration s3Configuration;
    private final SystemEnvironment systemEnvironment;
    private final DroneService droneService;
    private final PostgresService postgresService;

    public List<JsonNode> getAllBucketData(String bucket) {
        return getS3Client()
                .listObjectsV2(b -> b.bucket(bucket))
                .contents()
                .stream()
                .map(S3Object::key)
                .map(key -> getBucketData(bucket, key))
                .toList();
    }

    public JsonNode getBucketData(String bucket, String key) {
        try {
            ResponseInputStream<GetObjectResponse> object =
                    getS3Client().getObject(b -> b.bucket(bucket).key(key));

            String jsonString = new String(object.readAllBytes());

            try {
                return mapper.readTree(jsonString);
            }

            catch (Exception e) {
                return TextNode.valueOf(jsonString);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void processData(String url) throws JsonProcessingException {
        Drone[] drones = droneService.dumpDroneData(url);

        ObjectMapper objectMapper = new ObjectMapper();
        for (Drone drone : drones) {
            String bucketContent = objectMapper.writeValueAsString(drone);
            createBucketObject(s3Configuration.getS3Bucket(), drone.getName()
                    , bucketContent);
        }
    }

    public void copyData(String table) throws JsonProcessingException {
        List<Map<String, Object>> data = postgresService.getTableData(table);

        ObjectMapper objectMapper = new ObjectMapper();
        for (Map<String, Object> datum : data) {
            String bucketContent = objectMapper.writeValueAsString(datum);
            createBucketObject(s3Configuration.getS3Bucket(), (String) datum.get("id"),
                    bucketContent);
        }
    }

    public void createBucketObject(String bucket, String s3Object, String objectContent) {
        getS3Client().putObject(b -> b.bucket(bucket).key(s3Object), software.amazon.awssdk.core.sync.RequestBody.fromString(objectContent));
    }

    private S3Client getS3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(s3Configuration.getS3Endpoint()))
                .forcePathStyle(true)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(systemEnvironment.getAwsUser(), systemEnvironment.getAwsSecret())))
                .region(systemEnvironment.getAwsRegion())
                .build();
    }
}
