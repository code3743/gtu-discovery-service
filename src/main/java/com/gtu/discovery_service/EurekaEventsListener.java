package com.gtu.discovery_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceCanceledEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EurekaEventsListener {



    @Autowired
    private LogPublisher logPublisher;

    private final Set<String> canceledInstances = ConcurrentHashMap.newKeySet();

    @EventListener
    public void onInstanceRegistered(EurekaInstanceRegisteredEvent event) {
        if (!event.isReplication() && event.getInstanceInfo().getStatus().toString().equals("UP")) {
        
            logPublisher.sendLog(
                Instant.now().toString(),
                "discovery-service",
                "INFO",
                "✅ New service registered: " + event.getInstanceInfo().getInstanceId(),
                Map.of(
                    "instanceId", event.getInstanceInfo().getInstanceId(),
                    "ipAddr", event.getInstanceInfo().getIPAddr(),
                    "status", event.getInstanceInfo().getStatus().toString()
                )
            );
        }
    }

    @EventListener
    public void onInstanceCanceled(EurekaInstanceCanceledEvent event) {
        if (!event.isReplication()) {
            if (canceledInstances.add(event.getServerId())) {   
                logPublisher.sendLog(
                    Instant.now().toString(),
                    "discovery-service",
                    "WARN",
                    "❌ Service disconnected: " + event.getServerId(),
                    Map.of(
                        "serverId", event.getServerId()
                    )
                );
            }
        }
    }

}