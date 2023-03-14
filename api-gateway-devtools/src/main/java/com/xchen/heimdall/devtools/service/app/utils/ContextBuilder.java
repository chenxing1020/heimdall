package com.xchen.heimdall.devtools.service.app.utils;

import org.thymeleaf.context.Context;

/**
 * @author xchen
 * @date 2022/5/4
 */
public class ContextBuilder {

    private Context context;

    public ContextBuilder() {
        context = new Context();
    }

    public ContextBuilder setVariable(String key, Object value) {
        context.setVariable(key, value);
        return this;
    }

    public Context getContext() {
        return context;
    }
}