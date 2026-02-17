package com.acpcw1.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DynamoDbConfiguration {

    @Value("${acp.dynamodb-endpoint}")
    private String dynamoDbEndpoint;


    @Value("${acp.dynamodb-table}")
    private String dynamoDbTable;

    @Bean(name = "dynamoDbEndpoint")
    public String getDynamoDbEndpoint(){
        return dynamoDbEndpoint;
    }

    @Bean(name = "dynamoDbTable")
    public String getDynamoDbTable() { return dynamoDbTable; }

}
