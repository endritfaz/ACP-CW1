package com.acpcw1.service;

import com.acpcw1.configuration.DynamoDbConfiguration;
import com.acpcw1.configuration.SystemEnvironment;
import com.acpcw1.data.Drone;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.attribute.EnhancedAttributeValue;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DynamoDbService {
    @Autowired
    private final ObjectMapper mapper =
            new ObjectMapper().enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

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
                .map(this::convertItem)
                .toList();
    }

    public JsonNode convertItem(Map<String, AttributeValue> item) {
        ObjectNode node = mapper.createObjectNode();

        for (String attribute : item.keySet()) {
            String jsonString = getRawValue(item.get(attribute));

            JsonNode json;
            try {
                json = mapper.readTree(jsonString);
            }

            catch (JsonProcessingException ex) {
                json = TextNode.valueOf(jsonString);
            }

            node.set(attribute, json);
        }
        return node;
    }

    private String getRawValue(AttributeValue value) {
        if (value.s() != null)    return value.s();
        if (value.n() != null)    return value.n();
        if (value.bool() != null) return value.bool().toString();
        if (value.nul() != null)  return "null";
        if (value.b() != null)    return value.b().toString();
        if (value.ss() != null)   return value.ss().toString();
        if (value.ns() != null)   return value.ns().toString();
        if (value.l() != null)    return value.l().toString();
        if (value.m() != null)    return value.m().toString();
        return "";
    }

    public JsonNode readData(String table, String keyValue) {
        String keyName = getTablePrimaryKey(table);

        Map<String, AttributeValue> key = Map.of(
                keyName, AttributeValue.fromS(keyValue)
        );

        GetItemResponse response = getDynamoDbClient().getItem(
                GetItemRequest.builder()
                        .tableName(table)
                        .key(key)
                        .build()
        );

        return convertItem(response.item());
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
        String keyName = getTablePrimaryKey(table);

        getDynamoDbClient().putItem(b -> b.tableName(table).item(
                java.util.Map.of(keyName,
                        software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().s(key).build(),
                        "content", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().s(objectContent).build())
        ));
    }

    private String getTablePrimaryKey(String table) {

        DescribeTableRequest request = DescribeTableRequest.builder()
                .tableName(table)
                .build();

        DescribeTableResponse response = getDynamoDbClient().describeTable(request);

        return response.table().keySchema().stream()
                .filter(k -> k.keyType().toString().equals("HASH"))
                .map(KeySchemaElement::attributeName)
                .findFirst()
                .orElseThrow();
    }

    private DynamoDbClient getDynamoDbClient() {
        return DynamoDbClient.builder()
                .endpointOverride(URI.create(dynamoDbConfiguration.getDynamoDbEndpoint()))
                .region(systemEnvironment.getAwsRegion())
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(systemEnvironment.getAwsUser(), systemEnvironment.getAwsSecret())))
                .build();
    }
}
