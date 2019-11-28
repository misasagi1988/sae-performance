package com.hansight.sae.performance.input;

import com.alibaba.fastjson.JSONArray;
import com.hansight.sae.performance.core.EngineCore;
import com.hansight.sae.performance.metric.MetricsCenter;
import com.hansight.sae.performance.metric.MetricsManager;
import com.hansight.sae.performance.util.JsonUtil;
import com.hansight.sae.performance.util.ThreadPoolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MessageCallback {
    private static final Logger logger = LoggerFactory.getLogger(MessageCallback.class);

    private LinkedBlockingQueue<Map> queue = new LinkedBlockingQueue<>(20480);
    private boolean lastEnqueueStatus = true;

    public MessageCallback(){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Map message = queue.take();
                        if(!specialCheck(message)) {
                            continue;
                        }
                        // TODO 对message进行wrap
                        EngineCore.sendEvent(message);
                        if(MetricsManager.isMetric()) {
                            MetricsCenter.me().getMeter(MetricsCenter.ENGINE_EVENT).mark();
                        }
                        if(logger.isDebugEnabled()){
                            logger.debug("send data to engine:{}", message);
                        }
                    }
                    catch (Exception e){
                        // TODO
                        logger.error("send event to engine error: {}", e);
                    }
                }
            }
        });
        t.setName(MessageCallback.class.getSimpleName());
        t.setDaemon(true);
        t.start();
        ThreadPoolUtil.createSchedulePool(1, "queue-info").scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.info("current queue size: {}", queue.size());
                } catch (Exception e) {

                }
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    public void onMessage(Map message) {

        if(message == null || message.isEmpty()){
            return;
        }
        try {
            // 如果始终失败，表示无法处理数据，直接丢弃数据避免阻塞
            if(!lastEnqueueStatus){
                lastEnqueueStatus = queue.offer(message);
            } else {
                lastEnqueueStatus = queue.offer(message, 3, TimeUnit.SECONDS);
            }
            if(!lastEnqueueStatus) {
                logger.warn("enqueue failed, queue size: {}, msg: {}", queue.size(), message);
                return;
            }
        } catch (Exception e) {
            logger.warn("some err when enqueue:{}", e);
        }
    }

    public void onMessage(String message) {
        onMessage(JsonUtil.parseObject(message, HashMap.class));
    }

    public static boolean specialCheck(Map data){

        if("/other/other".equals(data.get("event_type"))
                ||!data.containsKey("event_name")){
            return false;
        }
        if(!data.containsKey("id") && !data.containsKey("event_id")) {
            return false;
        }
        //ADD 2017-02-14 内存优化策略。
        data.remove("original_log");
        data.remove("src_address_str");
        data.remove("dst_address_str");
        data.remove("index_type");
        data.remove("original_json");

        data.remove("src_port_array");
        data.remove("dst_port_array");

        // 集成DV后，新增删除字段
        data.remove("dst_geo");
        data.remove("dev_geo");
        data.remove("src_geo");
        data.remove("src_longitude_latitude_name");
        data.remove("dev_longitude_latitude_name");
        data.remove("dst_longitude_latitude_name");

        Iterator<Map.Entry<String,Object>> iter = data.entrySet().iterator();
        while (iter.hasNext()){
            Map.Entry<String,Object> o = iter.next();
            if(o.getValue() == null){
                iter.remove();
            }
            else if( o.getValue() instanceof Integer
                    || o.getValue() instanceof Byte
                    || o.getValue() instanceof Short){
                o.setValue(Long.valueOf((Integer)o.getValue()));
            } else if(o.getValue() instanceof JSONArray) {
                int size = ((JSONArray)o.getValue()).size();
                if(size<=0) {
                    iter.remove();
                } else {
                    Object tmp = ((JSONArray)o.getValue()).get(0);
                    if(tmp instanceof Integer || tmp instanceof Byte || tmp instanceof Short || tmp instanceof Long) {
                        Long[] arr = new Long[size];
                        for(int i=0; i<size; i++ ) {
                            arr[i] = Long.parseLong(((JSONArray) o.getValue()).get(i).toString());
                        }
                        o.setValue(arr);
                    } else {
                        String[] arr = new String[size];
                        for(int i=0; i<size; i++ ) {
                            arr[i] = ((JSONArray) o.getValue()).get(i).toString();
                        }
                        o.setValue(arr);
                    }
                }
            }
        }

        //平展的写，尽量减少遍历
        Object o = null;
        o = data.get(NET_PROTOCOL);
        if(o != null){
            data.put(NET_PROTOCOL, o.toString().toUpperCase());
        }
        o = data.get(APP_PROTOCOL);
        if(o != null){
            data.put(APP_PROTOCOL, o.toString().toUpperCase());
        }
        o = data.get(PROTOCOL);
        if(o != null){
            data.put(PROTOCOL, o.toString().toUpperCase());
        }
        return true;
    }

    private static final String NET_PROTOCOL = "net_protocol";
    private static final String APP_PROTOCOL = "app_protocol";
    private static final String PROTOCOL = "protocol";

}
