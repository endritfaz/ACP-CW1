package com.acpcw1.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Configuration {

    @Value( "${acp.s3-endpoint}")
    private String s3Endpoint;

    @Value( "${acp.s3-bucket}")
    private String s3Bucket;

    @Bean(name = "s3Endpoint")
    public String getS3Endpoint(){
        return s3Endpoint;
    }

    @Bean(name = "s3Bucket")
    public String getS3Bucket(){
        return s3Bucket;
    }

}
