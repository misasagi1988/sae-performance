package com.hansight.sae.performance.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonUtil {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    static {
        JSON.DEFAULT_PARSER_FEATURE &= ~Feature.UseBigDecimal.getMask();
    }

    public static String toJsonStr(Object obj) {
        try {
            return JSONObject.toJSONString(obj);
        } catch (Exception e) {
            logger.error("Error parsing object to json string ! bean:[{}], exception:{}", obj, e);
        }
        return null;
    }

    public static <T> T parseObject(String jsonStr, Class<T> classType){
        try {
            return JSONObject.parseObject(jsonStr, classType);
        }catch (Exception e){
            logger.error("Error parsing string to object, string:[{}], object type:[{}]", jsonStr, classType.getName());
        }
        return null;
    }

    public static String parseToPrettyJson(Object o) {
        try {
            return JSON.toJSONString(o, SerializerFeature.PrettyFormat, SerializerFeature.DisableCircularReferenceDetect);
        }
        catch (Exception e) {
            logger.error("Error parsing object to json string ! bean:[{}], exception:{}", o, e);
        }
        return null;
    }
}
