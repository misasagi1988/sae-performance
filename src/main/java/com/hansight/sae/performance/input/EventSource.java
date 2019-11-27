package com.hansight.sae.performance.input;

import com.hansight.sae.performance.metric.MetricsCenter;
import com.hansight.sae.performance.metric.MetricsManager;
import com.hansight.sae.performance.util.JsonUtil;
import com.hansight.sae.performance.util.ThreadPoolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public class EventSource implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(EventSource.class);

    private static ThreadPoolExecutor executor = ThreadPoolUtil.createCachePool("data-source-pool");
    private MessageCallback messageCallback;
    private Map event;


    public EventSource(MessageCallback messageCallback) {
        this.messageCallback = messageCallback;
        String strData = "{\"receive_time\":1544604757386,\"event_level\":0,\"windows_event_id\":472,\"occur_time\":1565139398255,\"protocol\":\"UDP\",\"event_name\":\"网络连接\",\"dst_port\":515,\"original_log\":\"src:8.7.7.7,dst:8.8.8.8,domain:morning$circle.net,hash_file:54e1c3722102182bb133912ad4442e19,url:http://landini.az/greeting-ecards\",\"image\":\"C:\\\\ProgramFiles\",\"id\":1150745862875136,\"event_type\":\"/36JHPF2P0007/VNJN6T3B4eb9\",\"dst_address\":\"8.7.7.7\",\"src_address\":\"8.7.7.7\",\"sa_da\":\"8.7.7.7_8.8.8.8\",\"rule_name\":\"test hash and url\",\"domain_name\":\"DelegateExecute\",\"data_source\":\"Imperva1\"}";
        event = JsonUtil.parseObject(strData, HashMap.class);
    }

    public void accept() {
        try {
            executor.submit(this);
        } catch (Exception e) {
            logger.error("error: {}", e);
        }
    }

    @Override
    public void run() {
        int cnt = 0;
        while (true) {
            try {
                event.put("event_name", "网络连接"+cnt);
                event.put("occur_time", System.currentTimeMillis());
                messageCallback.onMessage(event);
                if(MetricsManager.isMetric()) {
                    MetricsCenter.me().getMeter(MetricsCenter.KAFKA_EVENT).mark();
                }
                cnt++;
                if(cnt>10) {
                    cnt = 0;
                }

            } catch (Exception e) {
                logger.error("error: {}", e);
            }
        }
    }
}
