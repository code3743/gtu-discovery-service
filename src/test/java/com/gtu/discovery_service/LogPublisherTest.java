package com.gtu.discovery_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.AmqpTemplate;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class LogPublisherTest {

    @Mock
    private AmqpTemplate amqpTemplate;

    @InjectMocks
    private LogPublisher logPublisher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        setPrivateField(logPublisher, "exchange", "test.exchange");
        setPrivateField(logPublisher, "routingKey", "test.routingkey");
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void sendLog_ShouldSendMessageSuccessfully() throws Exception {
        Map<String, Object> details = new HashMap<>();
        details.put("key", "value");

        logPublisher.sendLog("2023-01-01T12:00:00Z", "auth-service", "INFO", "Test log", details);

        verify(amqpTemplate, times(1))
                .convertAndSend(eq("test.exchange"), eq("test.routingkey"), anyString());

        verifyNoMoreInteractions(amqpTemplate);
    }

    @Test
    void sendLog_ShouldHandleExceptionGracefully() throws Exception {
        Map<String, Object> details = new HashMap<>();
        doThrow(new RuntimeException("AMQP error")).when(amqpTemplate).convertAndSend(anyString(), anyString(), anyString());

        logPublisher.sendLog("2023-01-01T12:00:00Z", "auth-service", "ERROR", "Test error", details);

        verify(amqpTemplate, times(1))
                .convertAndSend(anyString(), anyString(), anyString());
    }
}
