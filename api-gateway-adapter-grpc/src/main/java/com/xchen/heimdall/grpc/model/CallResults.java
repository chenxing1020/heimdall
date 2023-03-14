package com.xchen.heimdall.grpc.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @reference grpc-swagger https://github.com/grpc-swagger/grpc-swagger
 */
public class CallResults {
    private List<String> results;

    public CallResults() {
        this.results = new ArrayList<>();
    }

    public void add(String jsonText) {
        results.add(jsonText);
    }

    public List<String> asList() {
        return results;
    }
}