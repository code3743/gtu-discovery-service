package com.gtu.discovery_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceCanceledEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import com.netflix.appinfo.InstanceInfo;

import java.util.Map;

import static org.mockito.Mockito.*;

class EurekaEventsListenerTest {

    private LogPublisher logPublisher;
    private EurekaEventsListener listener;

    @BeforeEach
    void setUp() {
        logPublisher = mock(LogPublisher.class);
        listener = new EurekaEventsListener();
        // Inyectar el mock usando reflexión (ya que el campo es privado y @Autowired)
        java.lang.reflect.Field field;
        try {
            field = EurekaEventsListener.class.getDeclaredField("logPublisher");
            field.setAccessible(true);
            field.set(listener, logPublisher);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void onInstanceRegistered_shouldLogWhenUpAndNotReplication() {
        InstanceInfo instanceInfo = mock(InstanceInfo.class);
        when(instanceInfo.getStatus()).thenReturn(InstanceInfo.InstanceStatus.UP);
        when(instanceInfo.getInstanceId()).thenReturn("test-instance");
        when(instanceInfo.getIPAddr()).thenReturn("127.0.0.1");

        EurekaInstanceRegisteredEvent event = mock(EurekaInstanceRegisteredEvent.class);
        when(event.isReplication()).thenReturn(false);
        when(event.getInstanceInfo()).thenReturn(instanceInfo);

        listener.onInstanceRegistered(event);

        verify(logPublisher, times(1)).sendLog(
                anyString(),
                eq("discovery-service"),
                eq("INFO"),
                contains("test-instance"),
                argThat((Map<String, Object> map) ->
                        "test-instance".equals(map.get("instanceId")) &&
                        "127.0.0.1".equals(map.get("ipAddr")) &&
                        "UP".equals(map.get("status"))
                )
        );
    }

    @Test
    void onInstanceRegistered_shouldNotLogWhenReplication() {
        EurekaInstanceRegisteredEvent event = mock(EurekaInstanceRegisteredEvent.class);
        when(event.isReplication()).thenReturn(true);

        listener.onInstanceRegistered(event);

        verify(logPublisher, never()).sendLog(any(), any(), any(), any(), any());
    }

    @Test
    void onInstanceCanceled_shouldLogOncePerServerId() {
        EurekaInstanceCanceledEvent event = mock(EurekaInstanceCanceledEvent.class);
        when(event.isReplication()).thenReturn(false);
        when(event.getServerId()).thenReturn("server-1");

        listener.onInstanceCanceled(event);
        listener.onInstanceCanceled(event); // Debe loguear solo la primera vez

        verify(logPublisher, times(1)).sendLog(
                anyString(),
                eq("discovery-service"),
                eq("WARN"),
                contains("server-1"),
                argThat((Map<String, Object> map) ->
                        "server-1".equals(map.get("serverId"))
                )
        );
    }

    @Test
    void onInstanceCanceled_shouldNotLogWhenReplication() {
        EurekaInstanceCanceledEvent event = mock(EurekaInstanceCanceledEvent.class);
        when(event.isReplication()).thenReturn(true);

        listener.onInstanceCanceled(event);

        verify(logPublisher, never()).sendLog(any(), any(), any(), any(), any());
    }
}