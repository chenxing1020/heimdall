package com.xchen.heimdall.grpc.registry;

import com.xchen.heimdall.grpc.model.GrpcServerDefinition;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

/**
 * 通过服务名做服务发现
 *
 * @author 016878
 * @date 2022/1/6
 */

public class ServerNameResolverProvider extends NameResolverProvider {
    private Logger logger = LoggerFactory.getLogger(ServerNameResolverProvider.class);

    private Map<String, GrpcServerDefinition> servers;

    public ServerNameResolverProvider(Map<String, GrpcServerDefinition> servers) {
        this.servers = servers;
    }

    /**
     * 服务发现类
     *
     * @param targetUri
     * @param args
     * @return
     */
    @Override
    public NameResolver newNameResolver(URI targetUri, NameResolver.Args args) {
        GrpcServerDefinition server = servers.get(targetUri.toASCIIString());
        if (!Objects.isNull(server)) {
            logger.info("Load grpc server: {}", server);
            return new ServerNameResolver(server.getHostnames(), server.getPort());
        } else {
            logger.warn("No grpc config props, plz check!");
            return null;
        }
    }

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        // DNS provider优先级为5
        return 10;
    }

    @Override
    public String getDefaultScheme() {
        return null;
    }
}
