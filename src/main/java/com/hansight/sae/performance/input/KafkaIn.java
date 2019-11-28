package com.hansight.sae.performance.input;

import com.hansight.sae.performance.metric.MetricsCenter;
import com.hansight.sae.performance.metric.MetricsManager;
import com.hansight.sae.performance.util.JsonUtil;
import com.hansight.sae.performance.util.ThreadPoolUtil;
import org.apache.kafka.clients.consumer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

public class KafkaIn implements Runnable{
    protected static final Logger log = LoggerFactory.getLogger(KafkaIn.class);

    private static ThreadPoolExecutor executor = ThreadPoolUtil.createCachePool("kafka-input-pool");
    private static final String TOPIC = "topic";
    private static final String KAFKA = "bootstrap.servers";


    private String name;
    private Map config;
    private MessageCallback callback;
    private static Set<KafkaIn> activedInput = new HashSet<>();
    private String topic;
    private String kafka;
    private Properties props;
    private KafkaConsumer<String, String> consumer;
    private volatile boolean autoCommit;

    private volatile boolean isRunning = true;

    //private Set<TopicPartition> partitions = new HashSet<>(5);

    public KafkaIn(Map config, MessageCallback callback) {
        if(config != null){
            Iterator<Map.Entry> iterator = config.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry entry = iterator.next();
                if(entry.getValue() == null){
                    iterator.remove();
                }
            }
        }

        this.config = spread(config);
        this.callback = callback;
        this.prepare();
        synchronized (activedInput) {
            log.debug("current active inputer: {}", JsonUtil.toJsonStr(activedInput));
            if (activedInput.contains(this)) {
                throw new RuntimeException("Adding an input object with the same name is not allowed: " + name);
            }
            activedInput.add(this);
        }
    }

    protected void prepare() {
        if (!this.config.containsKey(TOPIC)) {
            log.error("topic must be included in config");
            System.exit(-1);
        }
        if (!this.config.containsKey(KAFKA)) {
            log.error("bootstrap-servers must be included in config");
            System.exit(-1);
        }
        topic = (String) this.config.remove(TOPIC);

        HashMap<String, Object> consumerSettings = new HashMap<>(this.config);

        props = new Properties();
        props.put(KAFKA, consumerSettings.remove(KAFKA));
        //props.putAll(consumerSettings);
        name = createName(props.getProperty(KAFKA), topic);
        kafka = props.getProperty(KAFKA);

        Set<String> defaultSupportedConfigs = new HashSet<>();
        Field[] fields = ConsumerConfig.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getType().toString().endsWith("java.lang.String") && Modifier.isStatic(f.getModifiers())) {
                try {
                    defaultSupportedConfigs.add(String.valueOf(f.get(ConsumerConfig.class)));
                } catch (IllegalAccessException e) {
                }
            }
        }

        consumerSettings.entrySet().stream().forEach(entry -> {
            String k = entry.getKey().toString();
            if (defaultSupportedConfigs.contains(k)) {
                props.put(k, entry.getValue());
            } else {
                log.warn("unsupported kafka consumer configs: {}", k);
            }
        });

        if(props.get("enable.auto.commit") != null && "false".equalsIgnoreCase(props.get("enable.auto.commit").toString())){
            autoCommit = false;
        }
        else {
            autoCommit = true;
        }
        this.consumer = createConsumer();
    }

    public void accept() {
        try {
            executor.submit(this);
        } catch (Exception e) {
            log.error("exception occurred when create kafka consumer, topic: {}, error: {}, exit!!!", topic, e);
            System.exit(-1);
        }
    }

    public void process(String msg) {
        callback.onMessage(msg);
    }


    @Override
    public void run() {
        log.info("Thread name: {}, topic: {}", Thread.currentThread().getName(), topic);

        final int MAX_SUPPORTED_PARTITIONS = 64;
        long[] lastReadEndOffset = new long[MAX_SUPPORTED_PARTITIONS];
        for (int i = 0; i < MAX_SUPPORTED_PARTITIONS; i++) {
            lastReadEndOffset[i] = -1L;
        }
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // if stopped running, break
                if(!isRunning) {
                    log.info("thread interrupt, stop consuming topic: {}", topic);
                    break;
                }

                long startTime = System.currentTimeMillis();
                ConsumerRecords<String, String> records = consumer.poll(1000);
                // 理论来说，这里如果处理时间过长会导致commit超时，进而导致kafka的rebalance的问题，进而导致重复消费的问题
                if(records == null || records.isEmpty())
                    continue;
                if (!autoCommit) {
                    consumer.commitSync();
                }
                for (ConsumerRecord<String, String> record : records) {
                    if (record.offset() > lastReadEndOffset[record.partition()]) {
                        if(log.isDebugEnabled()) {
                            log.debug("get message, topic: {}, partition: {}, offset: {}, message: {}", record.topic(), record.partition(), record.offset(), record.value());
                        }
                        process(record.value());
                        if(MetricsManager.isMetric()) {
                            MetricsCenter.me().getMeter(MetricsCenter.SOURCE_EVENT).mark();
                        }
                        lastReadEndOffset[record.partition()] = record.offset();
                    } else {
                        log.debug("kafka attempts to consume data repeatedly：offset={}, data={}", record.offset(), record.value());
                    }
                }

            }catch (CommitFailedException e){
                log.error("CommitFailedException occurred:{}", e);
                this.consumer = createConsumer();
            }catch (IllegalStateException e){
                log.error("IllegalStateException occurred:{}", e);
                this.consumer = createConsumer();
            }catch (Exception e){
                log.error("err in kafka read:{}", e);
            }
        }
        log.info("Thread name: {}, topic: {} destroyed", Thread.currentThread().getName(), topic);
        MetricsCenter.me().removeMeter(MetricsCenter.SOURCE_EVENT);
        removeInput(name);
        shutdown();
    }

    public void shutdown() {
        try {
            if(this.consumer != null) {
                this.consumer.wakeup();
                this.consumer.close();
            }
        }catch (Exception e){
            log.warn("", e);
        }
    }

    public static synchronized void removeInput(String name) {
        log.info("remove input object: {}", name);
        Iterator<KafkaIn> iter = activedInput.iterator();
        while (iter.hasNext()){
            KafkaIn ib = iter.next();
            if(ib.name.equalsIgnoreCase(name)){
                iter.remove();
            }
        }
    }

    public void update(Object data) {
        if(data instanceof Boolean){
            log.info("update kafka thread running status: {}", data);
            isRunning = Boolean.parseBoolean(data.toString());
        }
    }

    private KafkaConsumer createConsumer(){
        shutdown();
        try {
            Thread.sleep(3 * 1000);
        } catch (Exception e) {
        }
        if(!isRunning) {
            Thread.currentThread().interrupt();// TODO 不能保证数据已经全部读完
        }
        try {
            log.info("create kafka consumer with topic: {}", topic);
            KafkaConsumer consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Arrays.stream(topic.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet()));
            return consumer;
        } catch (Exception e) {
            log.error("create kafka consumer error: {}", e);
        }
        return null;
    }

    public static Map<String, Object> spread(Map<String, Object> cfg){
        if(cfg == null) return new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        Iterator<Map.Entry<String, Object>> iter = cfg.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Object> entry = iter.next();
            if (entry.getValue() instanceof Map) {
                Map m = spread((Map) entry.getValue());
                m.forEach((k, v) -> {
                    map.put(entry.getKey() + "." + k, v);
                });
            } else {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return map;
    }

    public static String createName(String kafkaCluster, String topic){
        return topic + "@" + kafkaCluster;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getKafka() {
        return kafka;
    }

    public void setKafka(String kafka) {
        this.kafka = kafka;
    }
}

