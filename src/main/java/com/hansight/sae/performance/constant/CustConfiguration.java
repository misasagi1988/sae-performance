package com.hansight.sae.performance.constant;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties
public class CustConfiguration {
    @Bean
    @ConfigurationProperties(prefix = "kafka")
    public KafkaConsumerProps kafkaConsumerProps() {
        return new KafkaConsumerProps();
    }

    @Bean
    @ConfigurationProperties(prefix = "engine")
    public EngineConfigProps engineConfigProps() {
        return new EngineConfigProps();
    }

    @Bean
    @ConfigurationProperties(prefix = "performance")
    public PerformanceTestProps performanceTestProps() {
        return new PerformanceTestProps();
    }
}
