package com.hansight.sae.performance.core;

import com.espertech.esper.client.hook.ExceptionHandler;
import com.espertech.esper.client.hook.ExceptionHandlerContext;
import com.hansight.sae.performance.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EngineExceptionHandler implements ExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(EngineExceptionHandler.class);

    @Override
    public void handle(ExceptionHandlerContext exceptionHandlerContext) {
        logger.warn("rule:{} [{}] found an exception: {}, event info: {}", exceptionHandlerContext.getStatementName(),
                exceptionHandlerContext.getEpl(), exceptionHandlerContext.getThrowable().getMessage(),
                JsonUtil.toJsonStr(exceptionHandlerContext.getCurrentEvent().getUnderlying()));
    }
}

