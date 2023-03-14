package com.xchen.heimdall.grpc.registry;

import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 016878
 * @date 2022/1/6
 */
public class ServerNameResolver extends NameResolver {

    private Listener2 listener;

    /**
     * 服务主机ip
     */
    private List<String> hostnames;
    private Integer port;

    public ServerNameResolver(List<String> hostNames, Integer port) {
        this.hostnames = hostNames;
        this.port = port;
    }

    @Override
    public String getServiceAuthority() {
        return "none";
    }

    /**
     * 配置可用服务，RPC在调用的时候，轮询选择这里配置的可用的服务地址列表
     *
     * @param listener
     */
    @Override
    public void start(Listener2 listener) {
        this.listener = listener;
        this.resolve();
    }

    @Override
    public void refresh() {
        this.resolve();
    }

    @Override
    public void shutdown() {

    }

    private void resolve() {
        ArrayList<EquivalentAddressGroup> addressGroups = buildAddressGroups();

        ResolutionResult resolutionResult = ResolutionResult.newBuilder()
                .setAddresses(addressGroups)
                .setAttributes(Attributes.EMPTY)
                .build();

        this.listener.onResult(resolutionResult);
    }

    private ArrayList<EquivalentAddressGroup> buildAddressGroups() {
        ArrayList<EquivalentAddressGroup> addressGroups = new ArrayList<>();

        // 获取rpc地址的配置列表
        for (String hostname : hostnames) {
            List<SocketAddress> socketAddresses = new ArrayList<>();
            socketAddresses.add(new InetSocketAddress(hostname, port));
            addressGroups.add(new EquivalentAddressGroup(socketAddresses));
        }

        return addressGroups;
    }

}
