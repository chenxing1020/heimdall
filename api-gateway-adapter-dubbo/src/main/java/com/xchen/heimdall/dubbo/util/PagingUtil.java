package com.xchen.heimdall.dubbo.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xchen.heimdall.dubbo.support.wrapper.PagedWrapper;
import com.xchen.heimdall.common.util.BeanUtil;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * @author 019227
 */
public class PagingUtil {

    private PagingUtil() {
    }

    public static <U, V extends Serializable> PagedWrapper<V> convert(
            @NonNull IPage<U> page, @NonNull Supplier<V> targetSupplier) {
        return new PagedWrapper<>(
                BeanUtil.convertList(page.getRecords(), targetSupplier),
                page.getTotal(),
                (int) page.getSize(),
                (int) page.getCurrent()
        );
    }

}
