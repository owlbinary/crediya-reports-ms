package com.crediya.sqs.sender;

import com.crediya.sqs.sender.config.SQSSenderProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SQSSenderTest {

    @Mock
    private SQSSenderProperties properties;

    @Mock
    private SqsAsyncClient client;

    private SQSSender sqsSender;

    private static final String QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/123456789012/test-queue";
    private static final String MESSAGE_ID = "12345-67890-abcde";

    @BeforeEach
    void setUp() {
        sqsSender = new SQSSender(properties, client);
    }

    @Test
    void shouldSendMessageSuccessfully() {
        String message = "Test message";
        SendMessageResponse response = SendMessageResponse.builder()
                .messageId(MESSAGE_ID)
                .build();

        when(properties.queueUrl()).thenReturn(QUEUE_URL);
        when(client.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(response));

        Mono<String> result = sqsSender.send(message);

        StepVerifier.create(result)
                .expectNext(MESSAGE_ID)
                .verifyComplete();
    }

    @Test
    void shouldSendJsonMessage() {
        String jsonMessage = "{\"type\":\"test\",\"data\":\"value\"}";
        SendMessageResponse response = SendMessageResponse.builder()
                .messageId("json-message-id")
                .build();

        when(properties.queueUrl()).thenReturn(QUEUE_URL);
        when(client.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(response));

        Mono<String> result = sqsSender.send(jsonMessage);

        StepVerifier.create(result)
                .expectNext("json-message-id")
                .verifyComplete();
    }

    @Test
    void shouldHandleEmptyMessage() {
        String emptyMessage = "";
        SendMessageResponse response = SendMessageResponse.builder()
                .messageId("empty-message-id")
                .build();

        when(properties.queueUrl()).thenReturn(QUEUE_URL);
        when(client.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(response));

        Mono<String> result = sqsSender.send(emptyMessage);

        StepVerifier.create(result)
                .expectNext("empty-message-id")
                .verifyComplete();
    }

    @Test
    void shouldHandleClientError() {
        String message = "Test message";
        RuntimeException sqsException = new RuntimeException("SQS service unavailable");

        when(properties.queueUrl()).thenReturn(QUEUE_URL);
        when(client.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.failedFuture(sqsException));

        Mono<String> result = sqsSender.send(message);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void shouldHandleTimeout() {
        String message = "Test message";
        RuntimeException timeoutException = new RuntimeException("Request timeout");

        when(properties.queueUrl()).thenReturn(QUEUE_URL);
        when(client.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.failedFuture(timeoutException));

        Mono<String> result = sqsSender.send(message);

        StepVerifier.create(result)
                .expectErrorMatches(error -> 
                    error instanceof RuntimeException &&
                    error.getMessage().equals("Request timeout")
                )
                .verify();
    }
}