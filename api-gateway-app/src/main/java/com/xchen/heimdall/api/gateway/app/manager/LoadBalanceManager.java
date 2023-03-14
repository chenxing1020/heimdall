package com.xchen.heimdall.api.gateway.app.manager;

import com.xchen.heimdall.common.exception.errorcode.InternalServerException;
import com.xchen.heimdall.common.policy.ExponentialReconnectionPolicy;
import com.xchen.heimdall.common.policy.IReconnectionPolicy;
import io.jsonwebtoken.lang.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.util.InetAddressUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

/**
 * @author xchen
 * @date 2022/2/28
 */

@Service
@Slf4j
public class LoadBalanceManager {

    /**
     * 存活的rpc client
     */
    private List<RpcClientManager> upClientManagers = new CopyOnWriteArrayList<>();
    /**
     * 所有的rpc client
     */
    private final List<RpcClientManager> allClientManagers = new CopyOnWriteArrayList<>();
    private final AtomicBoolean pingInProgress = new AtomicBoolean(false);
    private final AtomicInteger nextServerCyclicCounter = new AtomicInteger(0);
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    /**
     * ip1:port1,ip2:port2
     */
    @Value("${facade.server.list:}")
    private String[] serverList;

    @PostConstruct
    public void postConstruct() throws InterruptedException {

        if (Objects.isEmpty(serverList)) {
            throw new InternalServerException("Failed to init, due to server list is empty.");
        }

        for (String server : serverList) {
            try {
                server = server.trim();
                String[] serverIpPort = server.split(":");
                String host = checkHost(serverIpPort[0]);
                int serverPort = checkPort(Integer.parseInt(serverIpPort[1]));

                RpcClientManager clientManager = new RpcClientManager(host, serverPort);
                allClientManagers.add(clientManager);
                clientManager.startConnection();
            } catch (Exception e) {
                log.error("Failed to start connection for {}", server);
            }
        }

        // 初始化时阻塞等待第一个存活的server
        firstRunPinger();

        // 定时探活
        executorService.scheduleAtFixedRate(this::runPinger, 0, 10, TimeUnit.SECONDS);
    }

    /**
     * lb核心，目前对请求采用round-robin轮询策略，参考ribbon的baseLoadBalance
     *
     * @return 均衡后的可用rpc client
     */
    public RpcClientManager choose() {
        int count = 0;
        while (count++ < 10) {
            int upCount = upClientManagers.size();
            int serverCount = allClientManagers.size();

            if (upCount == 0) {
                throw new InternalServerException("No up facade servers available.");
            }

            int nextServerIndex = incrementAndGetModulo(serverCount);
            RpcClientManager clientManager = allClientManagers.get(nextServerIndex);

            // 连接状态和存活状态均正常
            if (clientManager.isAlive() && clientManager.isConnected()) {
                return clientManager;
            }
        }

        if (count >= 10) {
            log.warn("No available alive servers after 10 tries");
        }
        throw new InternalServerException("No available alive facade service");
    }

    private int incrementAndGetModulo(int modulo) {
        for (; ; ) {
            int current = nextServerCyclicCounter.get();
            int next = (current + 1) % modulo;
            if (nextServerCyclicCounter.compareAndSet(current, next)) {
                return next;
            }
        }
    }

    private void firstRunPinger() throws InterruptedException {
        if (!pingInProgress.compareAndSet(false, true)) {
            // Ping in progress - nothing to do
            return;
        }

        try {
            IReconnectionPolicy reconnectionPolicy = new ExponentialReconnectionPolicy();
            // 阻塞等待有存活的server
            while (upClientManagers.isEmpty()) {
                // 延迟等待
                long nextDelay = reconnectionPolicy.getNextDelay() * 1000;
                log.info("First run pinger after {} ms", nextDelay);
                sleep(nextDelay);
                upClientManagers = getAliveServers();
            }
        } finally {
            pingInProgress.set(false);
        }
    }

    /**
     * 主动探活，更新
     */
    private void runPinger() {
        if (!pingInProgress.compareAndSet(false, true)) {
            // Ping in progress - nothing to do
            return;
        }

        try {
            upClientManagers = getAliveServers();
        } finally {
            pingInProgress.set(false);
        }
    }

    /**
     * 获取存活的servers
     *
     * @return alive servers
     */
    private List<RpcClientManager> getAliveServers() {
        final List<RpcClientManager> newUpList = new ArrayList<>();

        for (RpcClientManager clientManager : allClientManagers) {

            boolean alive = pingServer(clientManager);

            // 存活状态变更
            boolean oldAlive = clientManager.isAlive();
            if (oldAlive != alive) {
                log.debug("Server [{}] status changed to {}", clientManager, (alive ? "ALIVE" : "DEAD"));
            }

            clientManager.setAlive(alive);
            if (alive) {
                newUpList.add(clientManager);
            }
        }
        return newUpList;
    }

    /**
     * 存活状态判断
     *
     * @param clientManager rpc client
     * @return 当前channel是否活跃
     */
    private boolean pingServer(RpcClientManager clientManager) {
        boolean alive = false;
        try {
            // 首先判断通道是否联通
            if (clientManager.isConnected()) {
                clientManager.sendPingRequest();
                alive = true;
            }
        } catch (Exception e) {
            log.warn("Exception while pinging server: {}", clientManager, e);
        }
        return alive;
    }

    private int checkPort(int port) {
        if (port < 0 || port > 0xFFFF) {
            throw new InternalServerException("Port out of range");
        }
        return port;
    }

    private String checkHost(String host) {
        if (!InetAddressUtils.isIPv4Address(host)) {
            throw new InternalServerException("Invalid host");
        }
        return host;
    }
}
