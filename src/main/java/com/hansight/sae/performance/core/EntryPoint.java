package com.hansight.sae.performance.core;

import com.hansight.sae.performance.constant.EngineConfigProps;
import com.hansight.sae.performance.constant.KafkaConsumerProps;
import com.hansight.sae.performance.constant.PerformanceTestProps;
import com.hansight.sae.performance.input.EventSource;
import com.hansight.sae.performance.input.KafkaIn;
import com.hansight.sae.performance.input.MessageCallback;
import com.hansight.sae.performance.listener.Subscriber;
import com.hansight.sae.performance.metric.MetricsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class EntryPoint {
    private static final Logger logger = LoggerFactory.getLogger(EntryPoint.class);
    @Autowired
    private MetricsManager metricsManager;

    @Autowired
    private EngineConfigProps engineConfigProps;
    @Autowired
    private KafkaConsumerProps kafkaConsumerProps;
    @Autowired
    private PerformanceTestProps performanceTestProps;

    private MessageCallback messageCallback = new MessageCallback();
    private Subscriber subscriber = new Subscriber();
    private static String eplName = "performance-test-rule-";

    @PostConstruct
    public void init() {
        logger.info("engine configs: {}", engineConfigProps);
        metricsManager.init();
        EngineCore.init(metricsManager.isMultiThread());
        logger.info("performance test info: {}", performanceTestProps);
        for(int i=0; i<performanceTestProps.getCount(); i++) {
            EngineCore.addRule(performanceTestProps.getEpl(), eplName+i, subscriber);
        }
        EventSource eventSource = new EventSource(messageCallback);
        eventSource.accept();
        /*KafkaIn kafkaIn = new KafkaIn(kafkaConsumerProps.getConsumer(), messageCallback);
        kafkaIn.accept();*/
        logger.info("init success");
    }

}
