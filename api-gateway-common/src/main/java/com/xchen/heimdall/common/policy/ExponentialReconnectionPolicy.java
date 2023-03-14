package com.xchen.heimdall.common.policy;

/**
 * 指数退让重连
 *
 * @author by xchen
 * @since 2023/3/4.
 */
public class ExponentialReconnectionPolicy implements IReconnectionPolicy {
    private long minDelay = 1;
    private long maxDelay = 64;
    private long nextDelay = minDelay;

    @Override
    public void setMinDelay(long minDelay) {
        this.maxDelay = minDelay;
    }

    @Override
    public long getMinDelay() {
        return minDelay;
    }

    @Override
    public void setMaxDelay(long maxDelay) {
        this.maxDelay = maxDelay;
    }

    @Override
    public long getMaxDelay() {
        return maxDelay;
    }

    @Override
    public long getNextDelay() {
        if (nextDelay > maxDelay) {
            return maxDelay;
        } else {
            nextDelay = Math.min(nextDelay * 2, maxDelay);
            return nextDelay;
        }
    }

    @Override
    public void resetDelay() {
        nextDelay = maxDelay;
    }
}
