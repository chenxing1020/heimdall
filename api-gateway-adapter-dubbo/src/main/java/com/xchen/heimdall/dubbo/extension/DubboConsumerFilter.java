package com.xchen.heimdall.dubbo.extension;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import com.xchen.heimdall.common.constant.TraceConfig;
import com.xchen.heimdall.common.util.UuidUtil;
import java.util.Arrays;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

/**
 * @author xchen
 */
@Activate(group = Constants.CONSUMER, order = -1)
@Slf4j
public class DubboConsumerFilter implements Filter {


    @SneakyThrows
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 将traceId & span传递给下游
        String traceId = MDC.get(TraceConfig.TRACE_ID);
        String spanString = MDC.get(TraceConfig.SPAN);
        int span = 0;
        if (StringUtils.isEmpty(traceId)) {
            traceId = UuidUtil.generate();
        } else if (StringUtils.isNumeric(spanString)) {
            span = Integer.parseInt(spanString);
        }
        invocation.getAttachments().put(TraceConfig.TRACE_ID, traceId);
        invocation.getAttachments().put(TraceConfig.SPAN, String.valueOf(span));

        long startTime = System.currentTimeMillis();

        Result result;
        try {
            result = invoker.invoke(invocation);
        } finally {
            log.info("invoke {}: {} {}ms",
                invoker.getInterface().getSimpleName() + "." + invocation.getMethodName(),
                Arrays.toString(invocation.getArguments()),
                System.currentTimeMillis() - startTime);
        }

        log.debug("invoke result: {}", result);
        return result;
    }

}
