package com.acpcw1.service;

import com.acpcw1.configuration.DynamoDbConfiguration;
import com.acpcw1.configuration.SystemEnvironment;
import com.acpcw1.data.Drone;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DynamoDbService {
    @Autowired
    private ObjectMapper mapper;

    private final SystemEnvironment systemEnvironment;
    private final DynamoDbConfiguration dynamoDbConfiguration;
    private final DroneService droneService;
    private final PostgresService postgresService;

    public List<JsonNode> readAllData(String table) {
        return getDynamoDbClient()
                .scanPaginator(ScanRequest.builder()
                        .tableName(table)
                        .build())
                .items()
                .stream()
                .map(e -> {String jsonString = e.get("content").s();
                    try {
                        return mapper.readTree(jsonString);
                    } catch (JsonProcessingException ex) {
                        return TextNode.valueOf(jsonString);
                    }
                })
                .toList();
    }

    public JsonNode readData(String table, String keyValue) {
        Map<String, AttributeValue> key = Map.of(
                "key", AttributeValue.fromS(keyValue)
        );

        GetItemResponse response = getDynamoDbClient().getItem(
                GetItemRequest.builder()
                        .tableName(table)
                        .key(key)
                        .build()
        );

        String jsonString = response.item().get("content").s();

        try {
            return mapper.readTree(jsonString);
        } catch (JsonProcessingException ex) {
            return TextNode.valueOf(jsonString);
        }
    }

    public void processData(String url) throws JsonProcessingException {
        Drone[] data = droneService.dumpDroneData(url);

        for (Drone datum : data) {
            String table = dynamoDbConfiguration.getDynamoDbTable();
            String key = datum.getName();
            String content = mapper.writeValueAsString(datum);

            createObject(table, key, content);
        }
    }

    public void copyData(String table) throws JsonProcessingException {
        List<Map<String, Object>> data = postgresService.getTableData(table);

        ObjectMapper objectMapper = new ObjectMapper();
        for (Map<String, Object> datum : data) {
            String bucketContent = objectMapper.writeValueAsString(datum);

            String dynamoDbTable = dynamoDbConfiguration.getDynamoDbTable();
            String key = (String) datum.get("id");
            String content = mapper.writeValueAsString(datum);

            createObject(dynamoDbTable, key, content);
        }
    }

    public void createObject(String table, String key, String objectContent) {
        getDynamoDbClient().putItem(b -> b.tableName(table).item(
                java.util.Map.of("key", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().s(key).build(),
                        "content", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().s(objectContent).build())
        ));
    }

    private DynamoDbClient getDynamoDbClient() {
        return DynamoDbClient.builder()
                .endpointOverride(URI.create(dynamoDbConfiguration.getDynamoDbEndpoint()))
                .region(systemEnvironment.getAwsRegion())
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(systemEnvironment.getAwsUser(), systemEnvironment.getAwsSecret())))
                .build();
    }
}
