package com.xchen.heimdall.api.gateway.app.config;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.apollo.ApolloDataSource;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import org.apache.dubbo.common.utils.NamedThreadFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.xchen.heimdall.common.util.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.xchen.heimdall.common.constant.ApolloNamespace.FLOW_CONFIG;

/**
 * 配置apollo动态配置
 *
 * @author xchen
 * @date 2022/9/7
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.cloud.sentinel.enabled", havingValue = "true")
public class SentinelConfig {

    @Value("${metrics.flushInterval:1}")
    private long flushInterval;

    /**
     * 拉取不到apollo配置时，默认不流控
     */
    private static final String DEFAULT_FLOW_RULES = "[]";
    private static final String FLOW_RULE_KEY = "flowRules";
    private static final String PARAM_FLOW_RULE_KEY = "paramFlowRules";

    private static final ScheduledExecutorService METRICS_SCHEDULER = Executors.newScheduledThreadPool(1,
            new NamedThreadFactory("sentinel-metrics-task", true));

    @PostConstruct
    public void postConstruct() {
        // 读取apollo配置
        loadRules();

        // 初始化metrics
        initMetrics();
    }

    private void loadRules() {
        // 加载流控规则
        ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new ApolloDataSource<>(
                FLOW_CONFIG,
                FLOW_RULE_KEY,
                DEFAULT_FLOW_RULES,
                source -> {
                    List<FlowRule> flowRules = JacksonUtil.decode(source, new TypeReference<List<FlowRule>>() {
                    });
                    log.info("Success to load flow rules: {}", flowRules);
                    return flowRules;
                }
        );
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());

        // 加载热点规则
        ReadableDataSource<String, List<ParamFlowRule>> paramFlowRuleDataSource = new ApolloDataSource<>(
                FLOW_CONFIG,
                PARAM_FLOW_RULE_KEY,
                DEFAULT_FLOW_RULES,
                source -> {
                    List<ParamFlowRule> paramFlowRules = JacksonUtil.decode(source, new TypeReference<List<ParamFlowRule>>() {
                    });
                    log.info("Success to load param flow rules: {}", paramFlowRules);
                    return paramFlowRules;
                }
        );
        ParamFlowRuleManager.register2Property(paramFlowRuleDataSource.getProperty());
    }

    /**
     * 指标上报，{@link com.alibaba.csp.sentinel.node.metric.MetricTimerListener}
     */
    private void initMetrics() {
        // 开启指标监控上报
        METRICS_SCHEDULER.scheduleAtFixedRate(() -> {
            // 整合指标
            Map<Long, List<MetricNode>> maps = new TreeMap<>();
            for (Map.Entry<ResourceWrapper, ClusterNode> e : ClusterBuilderSlot.getClusterNodeMap().entrySet()) {
                ClusterNode node = e.getValue();
                Map<Long, MetricNode> metrics = node.metrics();
                aggregateMetrics(maps, metrics, node);
            }
            aggregateMetrics(maps, Constants.ENTRY_NODE.metrics(), Constants.ENTRY_NODE);

            // 指标上报
            reportMetrics(maps);

        }, 0, flushInterval, TimeUnit.SECONDS);
    }

    /**
     * 指标聚合，{@link com.alibaba.csp.sentinel.node.metric.MetricTimerListener}
     */
    private void aggregateMetrics(Map<Long, List<MetricNode>> maps, Map<Long, MetricNode> metrics, ClusterNode node) {
        for (Map.Entry<Long, MetricNode> entry : metrics.entrySet()) {
            long time = entry.getKey();
            MetricNode metricNode = entry.getValue();
            metricNode.setResource(node.getName());
            metricNode.setClassification(node.getResourceType());
            maps.computeIfAbsent(time, k -> new ArrayList<MetricNode>());
            List<MetricNode> nodes = maps.get(time);
            nodes.add(entry.getValue());
        }
    }

    private void reportMetrics(Map<Long, List<MetricNode>> metrics) {

        if (!metrics.isEmpty()) {
            for (Map.Entry<Long, List<MetricNode>> entry : metrics.entrySet()) {
                try {
                    for (MetricNode node : entry.getValue()) {
                        // 用资源名称作为指标的tag
                        String resourceTag = node.getResource();
                        // 响应时间
                        log.debug("api.responseTime", node.getRt(), "url", resourceTag);
                        // 通过的资源请求个数
                        log.debug("api.passQps", node.getPassQps(), "url", resourceTag);
                        // 被拦截的请求个数
                        log.debug("api.blockQps", node.getBlockQps(), "url", resourceTag);
                    }
                } catch (Exception e) {
                    log.warn("Failed to report metrics, due to {}", e.getMessage(), e);
                }
            }
        }
    }
}
