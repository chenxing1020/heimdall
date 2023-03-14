package com.xchen.heimdall.common.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.xchen.heimdall.common.api.*;
import com.xchen.heimdall.common.constant.UpstreamChannelType;
import com.xchen.heimdall.common.exception.errorcode.FrameworkException;

/**
 * @author xchen
 * @date 2022/3/15
 */
public class GatewayApiDeserializationUtils {

    private static final String UPSTREAM_CHANNEL_TYPE = "upstreamChannelType";

    private GatewayApiDeserializationUtils() {
        throw new FrameworkException("Utility class");
    }

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ApolloGatewayApiDTO.class, (JsonDeserializer<ApolloGatewayApiDTO>) (json, type, context) -> {
                JsonObject jsonObject = json.getAsJsonObject();

                UpstreamChannelType upstreamChannelType = UpstreamChannelType.valueOf(
                        jsonObject.get(UPSTREAM_CHANNEL_TYPE).getAsString()
                );

                switch (upstreamChannelType) {
                    case DUBBO:
                    case HEIMDALL:
                        return context.deserialize(json, ApolloDubboApiDTO.class);
                    case GRPC:
                        return context.deserialize(json, ApolloGrpcApiDTO.class);
                    case REST:
                    case REST_RAW:
                        return context.deserialize(json, ApolloRestApiDTO.class);
                    case NATS_SUB:
                        return context.deserialize(json, ApolloNatsApiDTO.class);
                    case INTERNAL:
                        return context.deserialize(json, ApolloInternalApiDTO.class);
                    default:
                        break;
                }
                return null;
            })
            .create();

    public static ApolloGatewayApiDTO decodeGatewayApi(String json) {
        return GSON.fromJson(json, ApolloGatewayApiDTO.class);
    }
}
