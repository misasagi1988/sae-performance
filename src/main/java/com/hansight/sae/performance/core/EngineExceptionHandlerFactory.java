package com.hansight.sae.performance.core;

import com.espertech.esper.client.hook.ExceptionHandler;
import com.espertech.esper.client.hook.ExceptionHandlerFactory;
import com.espertech.esper.client.hook.ExceptionHandlerFactoryContext;

public class EngineExceptionHandlerFactory implements ExceptionHandlerFactory {

    private static EngineExceptionHandler exceptionHandler = new EngineExceptionHandler();

    @Override
    public ExceptionHandler getHandler(ExceptionHandlerFactoryContext exceptionHandlerFactoryContext) {
        return exceptionHandler;
    }
}