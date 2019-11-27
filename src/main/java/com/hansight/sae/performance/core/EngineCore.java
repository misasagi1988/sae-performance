package com.hansight.sae.performance.core;

import com.espertech.esper.client.*;
import com.hansight.sae.performance.constant.EngineMetricConstant;
import com.hansight.sae.performance.listener.EngineMetricsListener;
import com.hansight.sae.performance.listener.StatementMetricsListener;
import com.hansight.sae.performance.listener.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class EngineCore {

    private static final Logger logger = LoggerFactory.getLogger(EngineCore.class);

    public static final String ONLINE_ENGINE = "online";

    public static final String GLOBAL_EVENT = "GlobalEvent";

    private static EPServiceProvider onlineEngine;

    private static Configuration configuration;

    public static void init(boolean multi) {
        configuration = new Configuration();

        configuration.setPatternMaxSubexpressions(100000L);
        configuration.getEngineDefaults().getViewResources().setShareViews(false);
        configuration.getEngineDefaults().getThreading().setListenerDispatchPreserveOrder(true);
        configuration.getEngineDefaults().getExceptionHandling().addClass(EngineExceptionHandlerFactory.class);

        configuration.getEngineDefaults().getExecution().setFairlock(false);
        configuration.getEngineDefaults().getExecution().setThreadingProfile(ConfigurationEngineDefaults.ThreadingProfile.LARGE);
        configuration.getEngineDefaults().getExecution().setFilterServiceProfile(ConfigurationEngineDefaults.FilterServiceProfile.READWRITE);

        if (multi) {
            configuration.getEngineDefaults().getThreading().setThreadPoolInbound(true);
            configuration.getEngineDefaults().getThreading().setThreadPoolInboundCapacity(1000);
            configuration.getEngineDefaults().getThreading().setThreadPoolInboundNumThreads(Runtime.getRuntime().availableProcessors());
        }

        onlineEngine = EPServiceProviderManager.getProvider(ONLINE_ENGINE, configuration);
        onlineEngine.getEPAdministrator().getConfiguration().addEventType(GLOBAL_EVENT, getSchema());
    }

    public static boolean addRule(String epl, String name, Subscriber subscriber) {
        try {
            onlineEngine.getEPAdministrator().createEPL(epl, name).setSubscriber(subscriber);
        } catch (Exception e) {
            logger.error("add rule to esper engine failed: {}", e);
            return false;
        }
        return true;
    }

    public static void addMetrics() {
        try {
            UpdateListener engineMetricsListener = new EngineMetricsListener();
            onlineEngine.getEPAdministrator().createEPL(EngineMetricConstant.ENGINE_METRIC, EngineMetricConstant.ENGINE_METRIC_NAME)
                    .addListener(engineMetricsListener);
        } catch (Exception e){
            logger.error("add engine metrics to realtime engine failed: {}", e);
        }
        try {
            UpdateListener statementMetricsListener = new StatementMetricsListener();
            onlineEngine.getEPAdministrator().createEPL(EngineMetricConstant.STATEMENT_METRIC, EngineMetricConstant.STATEMENT_METRIC_NAME)
                    .addListener(statementMetricsListener);
        } catch (Exception e){
            logger.error("add statement metrics to realtime engine failed: {}", e);
        }
    }

    public static void destroyAllRules() {
        try {
            onlineEngine.getEPAdministrator().destroyAllStatements();
        } catch (Exception e) {
            logger.error("destroy all statement failed: {}", e);
        }
    }

    public static void sendEvent(Map<String, Object> map) {
        try {
            onlineEngine.getEPRuntime().sendEvent(map, GLOBAL_EVENT);
        } catch (Exception e) {
            logger.warn("send event to online Engine failed: {} -- {}", e, map);
        }
    }

    public static Map<String, Object> getSchema() {
        Map<String, Object> schema = new HashMap();
        schema.put("id", String.class);
        schema.put("event_name", String.class);
        schema.put("src_address", String.class);
        schema.put("dst_address", String.class);
        schema.put("src_port", Integer.class);
        schema.put("dst_port", Integer.class);
        schema.put("occur_time", Long.class);
        return schema;
    }
}
