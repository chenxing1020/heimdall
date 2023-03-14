package com.xchen.heimdall.dubbo.api.gateway.model;

import lombok.Data;

import java.util.List;

@Data
public class ParamTreeNode {

    private String name;

    private int type;

    private List<ParamTreeNode> chlidren;
}
