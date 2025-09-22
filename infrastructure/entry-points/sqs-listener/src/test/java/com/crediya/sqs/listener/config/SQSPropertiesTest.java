package com.crediya.sqs.listener.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SQSPropertiesTest {

    @Test
    void shouldCreateSQSPropertiesWithAllFields() {
        String region = "us-east-1";
        String queueUrl = "https://sqs.us-east-1.amazonaws.com/123456789012/test-queue";
        int waitTimeSeconds = 20;
        int visibilityTimeoutSeconds = 300;
        int maxNumberOfMessages = 10;
        int numberOfThreads = 5;

        SQSProperties properties = new SQSProperties(
                region,
                queueUrl,
                waitTimeSeconds,
                visibilityTimeoutSeconds,
                maxNumberOfMessages,
                numberOfThreads
        );

        assertEquals(region, properties.region());
        assertEquals(queueUrl, properties.queueUrl());
        assertEquals(waitTimeSeconds, properties.waitTimeSeconds());
        assertEquals(visibilityTimeoutSeconds, properties.visibilityTimeoutSeconds());
        assertEquals(maxNumberOfMessages, properties.maxNumberOfMessages());
        assertEquals(numberOfThreads, properties.numberOfThreads());
    }

    @Test
    void shouldHandleMinimumValues() {
        SQSProperties properties = new SQSProperties(
                "us-west-2",
                "https://sqs.us-west-2.amazonaws.com/987654321098/min-queue",
                1,
                1,
                1,
                1
        );

        assertEquals("us-west-2", properties.region());
        assertEquals("https://sqs.us-west-2.amazonaws.com/987654321098/min-queue", properties.queueUrl());
        assertEquals(1, properties.waitTimeSeconds());
        assertEquals(1, properties.visibilityTimeoutSeconds());
        assertEquals(1, properties.maxNumberOfMessages());
        assertEquals(1, properties.numberOfThreads());
    }

    @Test
    void shouldHandleMaximumValues() {
        SQSProperties properties = new SQSProperties(
                "eu-west-1",
                "https://sqs.eu-west-1.amazonaws.com/555555555555/max-queue",
                20,
                43200, // 12 hours max
                10,
                100
        );

        assertEquals("eu-west-1", properties.region());
        assertEquals("https://sqs.eu-west-1.amazonaws.com/555555555555/max-queue", properties.queueUrl());
        assertEquals(20, properties.waitTimeSeconds());
        assertEquals(43200, properties.visibilityTimeoutSeconds());
        assertEquals(10, properties.maxNumberOfMessages());
        assertEquals(100, properties.numberOfThreads());
    }

    @Test
    void shouldSupportEquality() {
        SQSProperties props1 = new SQSProperties("us-east-1", "queue1", 10, 30, 5, 2);
        SQSProperties props2 = new SQSProperties("us-east-1", "queue1", 10, 30, 5, 2);
        SQSProperties props3 = new SQSProperties("us-west-2", "queue2", 15, 60, 10, 4);

        assertEquals(props1, props2);
        assertNotEquals(props1, props3);
        assertEquals(props1.hashCode(), props2.hashCode());
    }

    @Test
    void shouldProvideToStringRepresentation() {
        SQSProperties properties = new SQSProperties(
                "ap-south-1",
                "https://sqs.ap-south-1.amazonaws.com/111222333444/test-queue",
                5,
                120,
                3,
                8
        );

        String toString = properties.toString();
        assertTrue(toString.contains("region=ap-south-1"));
        assertTrue(toString.contains("queueUrl=https://sqs.ap-south-1.amazonaws.com/111222333444/test-queue"));
        assertTrue(toString.contains("waitTimeSeconds=5"));
        assertTrue(toString.contains("visibilityTimeoutSeconds=120"));
        assertTrue(toString.contains("maxNumberOfMessages=3"));
        assertTrue(toString.contains("numberOfThreads=8"));
    }

    @Test
    void shouldHandleNullRegion() {
        SQSProperties properties = new SQSProperties(
                null,
                "https://sqs.us-east-1.amazonaws.com/123456789012/test-queue",
                10,
                30,
                5,
                2
        );

        assertNull(properties.region());
        assertNotNull(properties.queueUrl());
    }

    @Test
    void shouldHandleNullQueueUrl() {
        SQSProperties properties = new SQSProperties(
                "us-east-1",
                null,
                10,
                30,
                5,
                2
        );

        assertNotNull(properties.region());
        assertNull(properties.queueUrl());
    }
}