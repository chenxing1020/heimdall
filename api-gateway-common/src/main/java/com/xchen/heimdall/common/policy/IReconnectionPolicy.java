package com.xchen.heimdall.common.policy;

/**
 * 连接重连策略
 *
 * @author by xchen
 * @since 2023/3/4.
 */
public interface IReconnectionPolicy {

    /**
     * 设置最小间隔
     *
     * @param minDelay
     */
    void setMinDelay(long minDelay);

    /**
     * 获取最小间隔
     *
     * @return
     */
    long getMinDelay();

    /**
     * 设置最大间隔
     *
     * @param maxDelay
     */
    void setMaxDelay(long maxDelay);

    /**
     * 获取最大间隔
     *
     * @return
     */
    long getMaxDelay();

    /**
     * 获取下次重连间隔
     *
     * @return
     */
    long getNextDelay();

    /**
     * 重置连接间隔，恢复初始值
     */
    void resetDelay();
}
