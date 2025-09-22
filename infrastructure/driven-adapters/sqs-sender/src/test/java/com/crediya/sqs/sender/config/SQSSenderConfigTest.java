package com.crediya.sqs.sender.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.metrics.MetricPublisher;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SQSSenderConfigTest {

    @Mock
    private SQSSenderProperties properties;

    @Mock
    private MetricPublisher metricPublisher;

    private SQSSenderConfig sqsSenderConfig;

    @BeforeEach
    void setUp() {
        sqsSenderConfig = new SQSSenderConfig();
    }

    @Test
    void shouldConfigureSqsClientWithEndpoint() {
        when(properties.region()).thenReturn("us-east-1");
        when(properties.endpoint()).thenReturn("http://localhost:4566");
        SqsAsyncClient client = sqsSenderConfig.configSqs(properties, metricPublisher);

        assertNotNull(client);
    }

    @Test
    void shouldConfigureSqsClientWithoutEndpoint() {
        when(properties.region()).thenReturn("us-west-2");
        when(properties.endpoint()).thenReturn(null);
        SqsAsyncClient client = sqsSenderConfig.configSqs(properties, metricPublisher);

        assertNotNull(client);
    }

    @Test
    void shouldConfigureSqsClientWithEmptyEndpoint() {
        when(properties.region()).thenReturn("eu-west-1");
        when(properties.endpoint()).thenReturn(""); 
        SqsAsyncClient client = sqsSenderConfig.configSqs(properties, metricPublisher);
        assertNotNull(client);
    }

    @Test
    void shouldHandleValidRegions() {
        String[] validRegions = {
            "us-east-1", "us-west-1", "us-west-2", 
            "eu-west-1", "eu-central-1", "ap-southeast-1",
            "sa-east-1", "ca-central-1"
        };
        when(properties.endpoint()).thenReturn("https://sqs.amazonaws.com");

        for (String region : validRegions) {
            when(properties.region()).thenReturn(region);
            SqsAsyncClient client = sqsSenderConfig.configSqs(properties, metricPublisher);


            assertNotNull(client, "Client should be created for region: " + region);
        }
    }

    @Test
    void shouldConfigureWithLocalStackEndpoint() {
        when(properties.region()).thenReturn("us-east-1");
        when(properties.endpoint()).thenReturn("http://localhost:4566");
        SqsAsyncClient client = sqsSenderConfig.configSqs(properties, metricPublisher);

        assertNotNull(client);
    }

    @Test
    void shouldConfigureWithProductionEndpoint() {
        when(properties.region()).thenReturn("us-east-1");
        when(properties.endpoint()).thenReturn("https://sqs.us-east-1.amazonaws.com");
        SqsAsyncClient client = sqsSenderConfig.configSqs(properties, metricPublisher);

        assertNotNull(client);
    }

    @Test
    void shouldHandleNullMetricPublisher() {
        when(properties.region()).thenReturn("us-east-1");
        when(properties.endpoint()).thenReturn(null);
        assertDoesNotThrow(() -> {
            SqsAsyncClient client = sqsSenderConfig.configSqs(properties, null);
            assertNotNull(client);
        });
    }

    @Test
    void shouldHandleWhitespaceOnlyEndpoint() {
        when(properties.region()).thenReturn("us-west-2");
        when(properties.endpoint()).thenReturn("   "); 
        SqsAsyncClient client = sqsSenderConfig.configSqs(properties, metricPublisher);

        assertNotNull(client);
    }

    @Test
    void shouldConfigureCredentialsProviderChain() {
        when(properties.region()).thenReturn("ap-southeast-2");
        when(properties.endpoint()).thenReturn("http://localhost:9324");
        SqsAsyncClient client = sqsSenderConfig.configSqs(properties, metricPublisher);

        assertNotNull(client);
    }

    @Test
    void shouldCreateClientForDifferentEnvironments() {
        when(properties.region()).thenReturn("us-east-1");
        when(properties.endpoint()).thenReturn("http://localstack:4566");
        SqsAsyncClient devClient = sqsSenderConfig.configSqs(properties, metricPublisher);
        assertNotNull(devClient);

        when(properties.region()).thenReturn("us-west-1");
        when(properties.endpoint()).thenReturn("https://sqs.us-west-1.amazonaws.com");
        SqsAsyncClient stagingClient = sqsSenderConfig.configSqs(properties, metricPublisher);
        assertNotNull(stagingClient);

        when(properties.region()).thenReturn("eu-central-1");
        when(properties.endpoint()).thenReturn(null);  // Use default AWS endpoint
        SqsAsyncClient prodClient = sqsSenderConfig.configSqs(properties, metricPublisher);
        assertNotNull(prodClient);
    }

    @Test 
    void shouldCreateUniqueClientsForDifferentConfigurations() {
        when(properties.region()).thenReturn("us-east-1");
        when(properties.endpoint()).thenReturn("http://localhost:4566");
        SqsAsyncClient client1 = sqsSenderConfig.configSqs(properties, metricPublisher);
        when(properties.region()).thenReturn("us-west-2");
        when(properties.endpoint()).thenReturn("http://localhost:4567");
        SqsAsyncClient client2 = sqsSenderConfig.configSqs(properties, metricPublisher);

        assertNotNull(client1);
        assertNotNull(client2);
        assertNotSame(client1, client2);
    }

    @Test
    void shouldHandleSpecialCharactersInEndpoint() {
        when(properties.region()).thenReturn("us-east-1");
        when(properties.endpoint()).thenReturn("http://localhost:4566/path-with-special_chars");
        SqsAsyncClient client = sqsSenderConfig.configSqs(properties, metricPublisher);

        assertNotNull(client);
    }

    @Test
    void shouldConfigureWithDifferentPorts() {
        String[] endpoints = {
            "http://localhost:4566",
            "http://localhost:9324",
            "http://localhost:8080",
            "https://sqs.amazonaws.com"
        };

        when(properties.region()).thenReturn("us-east-1");

        for (String endpoint : endpoints) {
            when(properties.endpoint()).thenReturn(endpoint);
            SqsAsyncClient client = sqsSenderConfig.configSqs(properties, metricPublisher);


            assertNotNull(client, "Client should be created for endpoint: " + endpoint);
        }
    }
}