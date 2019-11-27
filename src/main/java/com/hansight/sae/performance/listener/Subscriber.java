package com.hansight.sae.performance.listener;

import com.hansight.sae.performance.metric.MetricsCenter;
import com.hansight.sae.performance.metric.MetricsManager;

import java.util.Map;

public class Subscriber {

    public void update(Map row) {
        if(MetricsManager.isMetric()) {
            MetricsCenter.me().getMeter(MetricsCenter.ALARM_TRIGGERRED).mark();
        }
    }
}
