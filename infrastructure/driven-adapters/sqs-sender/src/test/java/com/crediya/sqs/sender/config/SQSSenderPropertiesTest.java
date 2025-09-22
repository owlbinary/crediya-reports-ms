package com.crediya.sqs.sender.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SQSSenderPropertiesTest.TestConfiguration.class)
@TestPropertySource(properties = {
        "adapter.sqs.region=us-east-1",
        "adapter.sqs.queue-url=https://sqs.us-east-1.amazonaws.com/123456789012/test-queue",
        "adapter.sqs.endpoint=http://localhost:4566"
})
class SQSSenderPropertiesTest {

    @Test
    void shouldCreatePropertiesWithAllFields() {
        String region = "us-west-2";
        String queueUrl = "https://sqs.us-west-2.amazonaws.com/987654321098/production-queue";
        String endpoint = "https://sqs.amazonaws.com";

        SQSSenderProperties properties = new SQSSenderProperties(region, queueUrl, endpoint);

        assertNotNull(properties);
        assertEquals(region, properties.region());
        assertEquals(queueUrl, properties.queueUrl());
        assertEquals(endpoint, properties.endpoint());
    }

    @Test
    void shouldCreatePropertiesWithNullEndpoint() {
        String region = "us-east-1";
        String queueUrl = "https://sqs.us-east-1.amazonaws.com/123456789012/test-queue";
        String endpoint = null;

        SQSSenderProperties properties = new SQSSenderProperties(region, queueUrl, endpoint);

        assertNotNull(properties);
        assertEquals(region, properties.region());
        assertEquals(queueUrl, properties.queueUrl());
        assertNull(properties.endpoint());
    }

    @Test
    void shouldHandleEmptyStrings() {
        String region = "";
        String queueUrl = "";
        String endpoint = "";

        SQSSenderProperties properties = new SQSSenderProperties(region, queueUrl, endpoint);

        assertNotNull(properties);
        assertEquals("", properties.region());
        assertEquals("", properties.queueUrl());
        assertEquals("", properties.endpoint());
    }

    @Test
    void shouldTestEquality() {
        SQSSenderProperties properties1 = new SQSSenderProperties(
                "us-east-1", 
                "https://sqs.us-east-1.amazonaws.com/123456789012/queue1", 
                "http://localhost:4566"
        );
        SQSSenderProperties properties2 = new SQSSenderProperties(
                "us-east-1", 
                "https://sqs.us-east-1.amazonaws.com/123456789012/queue1", 
                "http://localhost:4566"
        );
        SQSSenderProperties properties3 = new SQSSenderProperties(
                "us-west-2", 
                "https://sqs.us-west-2.amazonaws.com/123456789012/queue2", 
                "http://localhost:4566"
        );

        assertEquals(properties1, properties2);
        assertNotEquals(properties1, properties3);
        assertEquals(properties1.hashCode(), properties2.hashCode());
        assertNotEquals(properties1.hashCode(), properties3.hashCode());
    }

    @Test
    void shouldTestToString() {
        SQSSenderProperties properties = new SQSSenderProperties(
                "eu-west-1", 
                "https://sqs.eu-west-1.amazonaws.com/555666777888/my-queue", 
                "https://sqs.amazonaws.com"
        );

        String toString = properties.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("eu-west-1"));
        assertTrue(toString.contains("https://sqs.eu-west-1.amazonaws.com/555666777888/my-queue"));
        assertTrue(toString.contains("https://sqs.amazonaws.com"));
    }

    @Test
    void shouldValidateRegionProperty() {
        String validRegion = "ap-southeast-2";
        SQSSenderProperties properties = new SQSSenderProperties(
                validRegion, 
                "https://sqs.ap-southeast-2.amazonaws.com/111222333444/test", 
                null
        );
        assertEquals(validRegion, properties.region());
        assertNotNull(properties.region());
    }

    @Test
    void shouldValidateQueueUrlProperty() {
        String validQueueUrl = "https://sqs.sa-east-1.amazonaws.com/999888777666/integration-queue";
        SQSSenderProperties properties = new SQSSenderProperties(
                "sa-east-1", 
                validQueueUrl, 
                "http://localhost:9324"
        );
        assertEquals(validQueueUrl, properties.queueUrl());
        assertTrue(properties.queueUrl().startsWith("https://sqs"));
        assertTrue(properties.queueUrl().contains("integration-queue"));
    }

    @Test
    void shouldHandleLocalStackEndpoint() {
        String localStackEndpoint = "http://localhost:4566";
        SQSSenderProperties properties = new SQSSenderProperties(
                "us-east-1", 
                "http://localhost:4566/000000000000/local-queue", 
                localStackEndpoint
        );
        assertEquals(localStackEndpoint, properties.endpoint());
        assertTrue(properties.endpoint().contains("localhost"));
        assertTrue(properties.endpoint().contains("4566"));
    }

    @EnableConfigurationProperties(SQSSenderProperties.class)
    static class TestConfiguration {
    }
}