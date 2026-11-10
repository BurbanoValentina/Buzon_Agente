package com.ucc.complaints.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservabilityConfig {
    @Bean
    public Timer requestLatencyTimer(MeterRegistry registry) {
        return Timer.builder("complaints.requests.latency")
            .description("Application request latency")
            .publishPercentileHistogram()
            .register(registry);
    }

    @Bean
    public Counter errorCounter(MeterRegistry registry) {
        return Counter.builder("complaints.errors")
            .description("Application errors without sensitive request data")
            .register(registry);
    }

    @Bean
    public Counter validationCounter(MeterRegistry registry) {
        return Counter.builder("complaints.validation.requests")
            .description("Public complaint validation requests")
            .register(registry);
    }

    @Bean
    public Counter lockoutCounter(MeterRegistry registry) {
        return Counter.builder("complaints.auth.lockouts")
            .description("Administrator lockout events")
            .register(registry);
    }

    @Bean
    public Counter externalFailureCounter(MeterRegistry registry) {
        return Counter.builder("complaints.external.failures")
            .description("Drive, AI and Telegram external failures")
            .register(registry);
    }

    @Bean
    public Counter exportCounter(MeterRegistry registry) {
        return Counter.builder("complaints.exports.requests")
            .description("Excel and Word export requests")
            .register(registry);
    }

    @Bean
    public Timer queryTimer(MeterRegistry registry) {
        return Timer.builder("complaints.queries.duration")
            .description("Normal administrator query duration")
            .publishPercentileHistogram()
            .register(registry);
    }

    @Bean
    public HealthIndicator telegramReadiness() {
        return () -> Health.up().withDetail("telegram", "configured-or-disabled").build();
    }
}
