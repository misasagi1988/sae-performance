package com.hansight.sae.performance.listener;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.client.metric.StatementMetric;
import com.hansight.sae.performance.constant.EngineMetricConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StatementMetricsListener  implements UpdateListener {
    private static final Logger logger = LoggerFactory.getLogger(StatementMetricsListener.class);
    private static final Set<String> systemMetricsEvents = new HashSet<>(Arrays.asList(EngineMetricConstant.ENGINE_METRIC_NAME, EngineMetricConstant.STATEMENT_METRIC_NAME));

    @Override
    public void update(EventBean[] eventBeans, EventBean[] eventBeans1) {
        if(eventBeans != null && eventBeans.length > 0) {
            for(EventBean eventBean: eventBeans) {
                if(eventBean.getUnderlying() instanceof StatementMetric) {
                    StatementMetric statementMetric = (StatementMetric) eventBean.getUnderlying();
                    if(systemMetricsEvents.contains(statementMetric.getStatementName()))
                        continue;
                    logger.info(format(statementMetric));
                } else {
                    // TODO
                }
            }
        }
    }

    private static String format(StatementMetric statementMetric) {
        return String.format("engineURI=%s, statementName=[%s], timestamp=%d, cpuTime=[%d], wallTime=[%d], numInput=[%d], numOutputIStream=[%d], numOutputRStream=[%d]",
                statementMetric.getEngineURI(), statementMetric.getStatementName(), statementMetric.getTimestamp(),
                statementMetric.getCpuTime(), statementMetric.getWallTime(), statementMetric.getNumInput(),
                statementMetric.getNumOutputIStream(), statementMetric.getNumOutputRStream());
    }
}
