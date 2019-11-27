package com.hansight.sae.performance.listener;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.client.metric.EngineMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EngineMetricsListener implements UpdateListener {
    private static final Logger logger = LoggerFactory.getLogger(EngineMetricsListener.class);
    @Override
    public void update(EventBean[] eventBeans, EventBean[] eventBeans1) {
        if(eventBeans != null && eventBeans.length > 0) {
            EventBean eventBean = eventBeans[0];
            if(eventBean.getUnderlying() instanceof EngineMetric) {
                EngineMetric engineMetric = (EngineMetric) eventBean.getUnderlying();
                logger.info(format(engineMetric));
            } else {
                // TODO
            }
        }
    }

    private static String format(EngineMetric engineMetric) {
        return String.format("engineURI=%s, timestamp=%d, inputCount=[%d], inputCountDelta=[%d], scheduleDepth=[%d]",
                engineMetric.getEngineURI(), engineMetric.getTimestamp(),
                engineMetric.getInputCount(), engineMetric.getInputCountDelta(), engineMetric.getScheduleDepth());
    }
}

