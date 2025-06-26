package com.gtu.discovery_service;

import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiscoveryServiceApplication.class, args);
	}

	@Autowired
    private LogPublisher logPublisher;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logPublisher.sendLog(
            Instant.now().toString(),
            "discovery-service",
            "INFO",
            "Discovery Service is up and running",
            Map.of()
        );
    }

}
