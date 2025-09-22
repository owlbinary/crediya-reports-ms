package com.crediya.dynamodb.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.auth.credentials.ContainerCredentialsProvider;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.auth.credentials.WebIdentityTokenFileCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.metrics.MetricCollection;
import software.amazon.awssdk.metrics.MetricPublisher;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

@Configuration
public class DynamoDBConfig {

    @Bean
    public MetricPublisher metricPublisher() {
        return new MetricPublisher() {
            @Override
            public void publish(MetricCollection metricCollection) {
            }

            @Override
            public void close() {
            }
        };
    }

    @Bean
    @Profile({"local"})
    public DynamoDbAsyncClient amazonDynamoDBDocker(@Value("${aws.region}") String region,
                                                    @Value("${aws.credentials.accessKey}") String accessKey,
                                                    @Value("${aws.credentials.secretKey}") String secretKey,
                                                    MetricPublisher publisher) {
        return DynamoDbAsyncClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of(region))
                .overrideConfiguration(o -> o.addMetricPublisher(publisher))
                .build();
    }

    @Bean
    @Profile({"dev", "stage", "prod"})
    public DynamoDbAsyncClient amazonDynamoDBAsync(MetricPublisher publisher, @Value("${aws.region}") String region) {
        return DynamoDbAsyncClient.builder()
                .credentialsProvider(getAwsCredentialsProviderChain())
                .region(Region.of(region))
                .overrideConfiguration(o -> o.addMetricPublisher(publisher))
                .build();
    }

    private AwsCredentialsProviderChain getAwsCredentialsProviderChain() {
        System.out.println("Configurando credenciales para DynamoDB");
        return AwsCredentialsProviderChain.builder()
                .addCredentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .addCredentialsProvider(SystemPropertyCredentialsProvider.create())
                .addCredentialsProvider(WebIdentityTokenFileCredentialsProvider.create())
                .addCredentialsProvider(ProfileCredentialsProvider.create())
                .addCredentialsProvider(ContainerCredentialsProvider.builder().build())
                .addCredentialsProvider(InstanceProfileCredentialsProvider.create())
                .build();
    }

    @Bean
    public DynamoDbEnhancedAsyncClient getDynamoDbEnhancedAsyncClient(DynamoDbAsyncClient client) {
        return DynamoDbEnhancedAsyncClient.builder()
                .dynamoDbClient(client)
                .build();
    }

}
