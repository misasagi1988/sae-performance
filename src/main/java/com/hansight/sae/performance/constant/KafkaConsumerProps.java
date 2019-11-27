package com.hansight.sae.performance.constant;

import java.util.Map;

public class KafkaConsumerProps {
    private Map<String, Object> consumer;

    public Map<String, Object> getConsumer() {
        return consumer;
    }

    public void setConsumer(Map<String, Object> consumer) {
        this.consumer = consumer;
    }

    @Override
    public String toString() {
        return "KafkaConsumerProps{" +
                "consumer=" + consumer +
                '}';
    }
}
