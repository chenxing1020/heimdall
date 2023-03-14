package com.xchen.heimdall.api.gateway.app.manager;

import com.xchen.heimdall.api.gateway.app.model.GwSyncRequestModel;
import com.xchen.heimdall.api.gateway.app.model.GwSyncResponseModel;
import com.xchen.heimdall.api.gateway.app.model.SubscribeRequestModel;
import com.xchen.heimdall.common.constant.UpstreamChannelType;
import com.xchen.heimdall.common.exception.errorcode.BadRequestException;
import com.xchen.heimdall.common.exception.errorcode.InternalServerException;
import com.xchen.heimdall.common.util.JacksonUtil;
import io.nats.client.Subscription;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author xchen
 * @date 2023/1/10
 */
@Slf4j
@Component
public class SyncRequestManager {

    private static final int SUCCESS_CODE = 0;
    private static final int ERROR_CODE = 1500;
    private static final long TIMEOUT_MILLS = 5000L;
    private static final String GW_SYNC_REQUEST_TOPIC = "gw_sync_request";
    private static final String NATS_SEPARATOR = ".";
    @Resource
    private SubscriptionHolderManager subscriptionHolderManager;
    @Resource
    private SubscribeHandlerManager subscribeHandlerManager;
    @Resource
    private NatsManager natsManager;

    /**
     * 监听连入网关的客户端订阅/退订远程请求
     *
     * @param clientId 客户端唯一标识
     */
    public Subscription acceptRequest(String clientId) {
        String gwSyncRequestTopic = GW_SYNC_REQUEST_TOPIC + NATS_SEPARATOR + clientId;
        try {
            // 订阅client的请求，用于websocket session同步
            Subscription subscription = natsManager.subscribe(gwSyncRequestTopic, message -> {
                // 订阅回调
                byte[] data = message.getData();
                GwSyncRequestModel syncRequest = JacksonUtil.decode(new String(data), GwSyncRequestModel.class);
                GwSyncResponseModel syncResponse = new GwSyncResponseModel();
                syncResponse.setResponseCode(SUCCESS_CODE);
                try {
                    // 流式请求类型：sub请求、一问多答请求
                    handleRequestLocal(syncRequest.getServiceName(),
                            syncRequest.getMethodName(),
                            syncRequest.getUpstreamChannelType(),
                            JacksonUtil.decode(syncRequest.getPayload(), SubscribeRequestModel.class)
                    );
                } catch (Exception e) {
                    syncResponse.setResponseCode(ERROR_CODE);
                    syncResponse.setMsg(e.getMessage());
                }
                try {
                    natsManager.publish(GW_SYNC_REQUEST_TOPIC, message.getReplyTo(), syncResponse);
                } catch (Exception e) {
                    log.error("Failed to publish gw sync response, due to {}", e.getMessage(), e);
                }
            });
            return subscription;
        } catch (Exception e) {
            throw new InternalServerException("Failed to sub gw_sync_request", e);
        }
    }

    public boolean handleRequest(String service, String method, UpstreamChannelType upstreamChannelType, SubscribeRequestModel request) {
        String clientId = request.getClientId();
        if (subscriptionHolderManager.containsClient(clientId)) {
            // clientId在本地，在本地处理
            return handleRequestLocal(service, method, upstreamChannelType, request);
        } else {
            // 发送同步请求到nats
            return handleRequestRemote(service, method, upstreamChannelType, request);
        }
    }

    private boolean handleRequestLocal(String service, String method, UpstreamChannelType upstreamChannelType, SubscribeRequestModel request) {
        // 流式请求类型：sub请求、一问多答请求
        switch (upstreamChannelType) {
            case NATS_SUB:
                // 处理sub请求
                if (!subscribeHandlerManager.handleSubscribeRequest(service, method, request)) {
                    throw new BadRequestException("Failed to subscribe");
                }
                return true;
            default:
                throw new BadRequestException("Failed to handle sync request " + request + ", due to wrong upstream channel type");
        }
    }

    /**
     * 同步流式请求到远程
     *
     * @param service             服务名，退订时为null
     * @param method              方法名，退订时为null
     * @param upstreamChannelType 请求类型
     * @param request             订阅/退订请求参数
     * @return 订阅/退订是否成功
     */
    private boolean handleRequestRemote(String service, String method, UpstreamChannelType upstreamChannelType, SubscribeRequestModel request) {
        GwSyncRequestModel gwSyncRequest = GwSyncRequestModel.builder()
                .clientId(request.getClientId())
                .payload(JacksonUtil.encode(request))
                .serviceName(service)
                .methodName(method)
                .upstreamChannelType(upstreamChannelType)
                .build();
        GwSyncResponseModel gwSyncResponse;
        try {
            gwSyncResponse = (GwSyncResponseModel) natsManager.request(GW_SYNC_REQUEST_TOPIC, gwSyncRequest, TIMEOUT_MILLS);
            if (Objects.nonNull(gwSyncResponse) &&
                    gwSyncResponse.getResponseCode() == SUCCESS_CODE) {
                return true;
            }
        } catch (Exception e) {
            log.info("Gw sync request time out", e);
            return false;
        }
        log.info("Failed to handle request remote, {}", gwSyncResponse);
        return false;
    }
}
