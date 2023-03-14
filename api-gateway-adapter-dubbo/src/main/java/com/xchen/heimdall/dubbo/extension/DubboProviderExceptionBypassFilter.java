package com.xchen.heimdall.dubbo.extension;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import com.xchen.heimdall.common.exception.errorcode.AbstractErrorCodeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.RpcResult;

/**
 * 由于{@link org.apache.dubbo.rpc.filter.ExceptionFilter}处理RuntimeException会将其String化以避免序列化失败，
 * 但本系统自定义的错误码异常是全局引用，并不会有这个问题，而且是希望能正常传递原异常信息给上游。
 * 因此为了跳过这个封装逻辑，在这里将异常额外封装一层避免其被String化，由{@link DubboProviderFilter}进行解封。
 *
 * @author xchen
 */
@Activate(group = Constants.PROVIDER, order = 1)
@Slf4j
public class DubboProviderExceptionBypassFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Result appResponse = invoker.invoke(invocation);

        if (appResponse.hasException()) {
            if (appResponse.getException() instanceof AbstractErrorCodeException) {
                ((RpcResult) appResponse).setException(new Exception(appResponse.getException()));
            }
        }

        return appResponse;
    }

}
