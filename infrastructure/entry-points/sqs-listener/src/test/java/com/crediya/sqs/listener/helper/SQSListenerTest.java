package com.crediya.sqs.listener.helper;

import com.crediya.sqs.listener.config.SQSProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SQSListenerTest {

    @Mock
    private SqsAsyncClient client;

    @Mock
    private SQSProperties properties;

    @Mock
    private Function<Message, Mono<Void>> processor;



    @Test
    void shouldCreateListenerSuccessfully() {
        SQSListener listener = SQSListener.builder()
                .client(client)
                .properties(properties)
                .processor(processor)
                .build();

        assertNotNull(listener);
    }

    @Test
    void shouldCreateListenerWithCustomProcessor() {
        Function<Message, Mono<Void>> customProcessor = message -> Mono.empty();

        SQSListener listener = SQSListener.builder()
                .client(client)
                .properties(properties)
                .processor(customProcessor)
                .build();

        assertNotNull(listener);
    }

    @Test
    void shouldBuildReceiveMessageRequestCorrectly() {
        when(properties.queueUrl()).thenReturn("test-queue-url");
        when(properties.maxNumberOfMessages()).thenReturn(5);
        when(properties.waitTimeSeconds()).thenReturn(10);
        when(properties.visibilityTimeoutSeconds()).thenReturn(30);

        SQSListener listener = SQSListener.builder()
                .client(client)
                .properties(properties)
                .processor(processor)
                .build();

        // Usar reflexión para acceder al método privado
        try {
            var method = SQSListener.class.getDeclaredMethod("getReceiveMessageRequest");
            method.setAccessible(true);
            ReceiveMessageRequest request = (ReceiveMessageRequest) method.invoke(listener);
            assertEquals("test-queue-url", request.queueUrl());
            assertEquals(5, request.maxNumberOfMessages());
            assertEquals(10, request.waitTimeSeconds());
            assertEquals(30, request.visibilityTimeout());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldBuildDeleteMessageRequestCorrectly() {
        when(properties.queueUrl()).thenReturn("delete-queue-url");
        SQSListener listener = SQSListener.builder()
                .client(client)
                .properties(properties)
                .processor(processor)
                .build();

        try {
            var method = SQSListener.class.getDeclaredMethod("getDeleteMessageRequest", String.class);
            method.setAccessible(true);
            DeleteMessageRequest request = (DeleteMessageRequest) method.invoke(listener, "receipt-123");
            assertEquals("delete-queue-url", request.queueUrl());
            assertEquals("receipt-123", request.receiptHandle());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void shouldCreateListenerWithAllComponents() {
        SQSListener listener = SQSListener.builder()
                .client(client)
                .properties(properties)
                .processor(message -> Mono.empty())
                .build();

        assertNotNull(listener);
    }

    @Test
    void shouldValidateBuilderPattern() {
        SQSListener.SQSListenerBuilder builder = SQSListener.builder()
                .client(client)
                .properties(properties);
        
        SQSListener listener = builder.processor(processor).build();

        assertNotNull(listener);
    }

    @Test
    void shouldHandleNullProcessor() {
        SQSListener listener = SQSListener.builder()
                .client(client)
                .properties(properties)
                .processor(null)
                .build();

        assertNotNull(listener);
    }

    @Test
    void shouldHandleNullClient() {
        SQSListener listener = SQSListener.builder()
                .client(null)
                .properties(properties)
                .processor(processor)
                .build();

        assertNotNull(listener);
    }

    @Test
    void shouldHandleNullProperties() {
        SQSListener listener = SQSListener.builder()
                .client(client)
                .properties(null)
                .processor(processor)
                .build();

        assertNotNull(listener);
    }

    @Test
    void shouldValidateBuilderWithMinimalSetup() {
        SQSListener listener = SQSListener.builder().build();

        assertNotNull(listener);
    }
}