package com.hansight.sae.performance.metric;

import com.hansight.sae.performance.constant.EngineConfigProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MetricsManager {
    private static final Logger log = LoggerFactory.getLogger(MetricsManager.class);

    @Autowired
    private EngineConfigProps engineConfigProps;

    private static boolean isMetricMode;
    private static boolean isMultiThread;


    public void init() {
        isMetricMode = engineConfigProps.isMetricOn();
        isMultiThread = engineConfigProps.isMultiThread();
        if (isMetricMode) {
            log.info("start core metric reporter.");
            MetricsCenter.me().start(engineConfigProps.getMetricPeriod());
        } else {
            log.info("core metric reporter is disabled.");
        }
    }

    public static boolean isMetric() {
        return isMetricMode;
    }

    public static boolean isMultiThread() {
        return isMultiThread;
    }
}
