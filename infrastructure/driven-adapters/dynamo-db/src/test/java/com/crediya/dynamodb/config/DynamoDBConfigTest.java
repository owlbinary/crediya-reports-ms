package com.crediya.dynamodb.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.metrics.MetricCollection;
import software.amazon.awssdk.metrics.MetricPublisher;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DynamoDBConfigTest {

    @Mock
    private DynamoDbAsyncClient dynamoDbAsyncClient;

    @Mock
    private MetricCollection metricCollection;

    private DynamoDBConfig dynamoDBConfig;

    @BeforeEach
    void setUp() {
        dynamoDBConfig = new DynamoDBConfig();
    }

    @Test
    void shouldCreateMetricPublisher() {
        MetricPublisher result = dynamoDBConfig.metricPublisher();

        assertNotNull(result, "MetricPublisher should not be null");
    }

    @Test
    void shouldHandleMetricPublisherPublish() {
        MetricPublisher publisher = dynamoDBConfig.metricPublisher();
        assertDoesNotThrow(() -> publisher.publish(metricCollection));
    }

    @Test
    void shouldHandleMetricPublisherClose() {
        MetricPublisher publisher = dynamoDBConfig.metricPublisher();
        assertDoesNotThrow(publisher::close);
    }

    @Test
    void shouldCreateLocalDynamoDbAsyncClient() {
        String region = "us-east-1";
        String accessKey = "test-access-key";
        String secretKey = "test-secret-key";
        MetricPublisher publisher = dynamoDBConfig.metricPublisher();

        DynamoDbAsyncClient result = dynamoDBConfig.amazonDynamoDBDocker(
                region, accessKey, secretKey, publisher);

        assertNotNull(result, "DynamoDbAsyncClient should not be null");
    }

    @Test
    void shouldCreateLocalDynamoDbAsyncClientWithDifferentRegion() {
        String region = "us-west-2";
        String accessKey = "test-access-key-2";
        String secretKey = "test-secret-key-2";
        MetricPublisher publisher = dynamoDBConfig.metricPublisher();

        DynamoDbAsyncClient result = dynamoDBConfig.amazonDynamoDBDocker(
                region, accessKey, secretKey, publisher);

        assertNotNull(result, "DynamoDbAsyncClient should not be null");
    }

    @Test
    void shouldCreateAwsDynamoDbAsyncClient() {
        String region = "us-east-1";
        MetricPublisher publisher = dynamoDBConfig.metricPublisher();

        DynamoDbAsyncClient result = dynamoDBConfig.amazonDynamoDBAsync(publisher, region);

        assertNotNull(result, "DynamoDbAsyncClient should not be null");
    }

    @Test
    void shouldCreateAwsDynamoDbAsyncClientWithDifferentRegion() {
        String region = "eu-west-1";
        MetricPublisher publisher = dynamoDBConfig.metricPublisher();

        DynamoDbAsyncClient result = dynamoDBConfig.amazonDynamoDBAsync(publisher, region);

        assertNotNull(result, "DynamoDbAsyncClient should not be null");
    }

    @Test
    void shouldCreateDynamoDbEnhancedAsyncClient() {
        DynamoDbEnhancedAsyncClient result = dynamoDBConfig.getDynamoDbEnhancedAsyncClient(dynamoDbAsyncClient);

        assertNotNull(result, "DynamoDbEnhancedAsyncClient should not be null");
    }

    @Test
    void shouldHandleNullDynamoDbClientInEnhancedClient() {
        DynamoDbEnhancedAsyncClient result = dynamoDBConfig.getDynamoDbEnhancedAsyncClient(null);

        assertNotNull(result, "DynamoDbEnhancedAsyncClient should not be null even with null input");
    }

    @Test
    void shouldCreateMultipleMetricPublishers() {
        MetricPublisher publisher1 = dynamoDBConfig.metricPublisher();
        MetricPublisher publisher2 = dynamoDBConfig.metricPublisher();

        assertNotNull(publisher1);
        assertNotNull(publisher2);
        assertNotSame(publisher1, publisher2, "Each call should return a new instance");
    }

    @Test
    void shouldCreateMultipleLocalClients() {
        String region = "us-east-1";
        String accessKey = "test-key";
        String secretKey = "test-secret";
        MetricPublisher publisher = dynamoDBConfig.metricPublisher();

        DynamoDbAsyncClient client1 = dynamoDBConfig.amazonDynamoDBDocker(region, accessKey, secretKey, publisher);
        DynamoDbAsyncClient client2 = dynamoDBConfig.amazonDynamoDBDocker(region, accessKey, secretKey, publisher);

        assertNotNull(client1);
        assertNotNull(client2);
        assertNotSame(client1, client2, "Each call should return a new instance");
    }

    @Test
    void shouldCreateMultipleAwsClients() {
        String region = "us-east-1";
        MetricPublisher publisher = dynamoDBConfig.metricPublisher();

        DynamoDbAsyncClient client1 = dynamoDBConfig.amazonDynamoDBAsync(publisher, region);
        DynamoDbAsyncClient client2 = dynamoDBConfig.amazonDynamoDBAsync(publisher, region);

        assertNotNull(client1);
        assertNotNull(client2);
        assertNotSame(client1, client2, "Each call should return a new instance");
    }

    @Test
    void shouldCreateMultipleEnhancedClients() {
        DynamoDbEnhancedAsyncClient enhanced1 = dynamoDBConfig.getDynamoDbEnhancedAsyncClient(dynamoDbAsyncClient);
        DynamoDbEnhancedAsyncClient enhanced2 = dynamoDBConfig.getDynamoDbEnhancedAsyncClient(dynamoDbAsyncClient);

        assertNotNull(enhanced1);
        assertNotNull(enhanced2);
        assertNotSame(enhanced1, enhanced2, "Each call should return a new instance");
    }

    @Test
    void shouldHaveCorrectAnnotations() {
        assertTrue(DynamoDBConfig.class.isAnnotationPresent(org.springframework.context.annotation.Configuration.class),
                "DynamoDBConfig should be annotated with @Configuration");
    }

    @Test
    void shouldHaveCorrectMethodAnnotations() throws NoSuchMethodException {
        assertTrue(DynamoDBConfig.class.getMethod("metricPublisher").isAnnotationPresent(org.springframework.context.annotation.Bean.class),
                "metricPublisher method should be annotated with @Bean");

        assertTrue(DynamoDBConfig.class.getMethod("amazonDynamoDBDocker", String.class, String.class, String.class, MetricPublisher.class)
                        .isAnnotationPresent(org.springframework.context.annotation.Bean.class),
                "amazonDynamoDBDocker method should be annotated with @Bean");

        assertTrue(DynamoDBConfig.class.getMethod("amazonDynamoDBAsync", MetricPublisher.class, String.class)
                        .isAnnotationPresent(org.springframework.context.annotation.Bean.class),
                "amazonDynamoDBAsync method should be annotated with @Bean");

        assertTrue(DynamoDBConfig.class.getMethod("getDynamoDbEnhancedAsyncClient", DynamoDbAsyncClient.class)
                        .isAnnotationPresent(org.springframework.context.annotation.Bean.class),
                "getDynamoDbEnhancedAsyncClient method should be annotated with @Bean");
    }
}
