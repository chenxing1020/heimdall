package com.xchen.heimdall.common.util;

import org.springframework.beans.BeanUtils;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author xchen
 */
public class BeanUtil {

    private BeanUtil() {
    }

    /**
     * 将一个对象实例转换成另外一个对象实例，相同的properties会拷贝
     *
     * @param source         源对象实例
     * @param targetSupplier 目标对象的类构造器
     * @param <S>            源对象
     * @param <T>            目标对象类型
     * @return 目标对象的实例
     */
    public static <S, T> T convert(@NonNull S source, @NonNull Supplier<T> targetSupplier) {
        T target = targetSupplier.get();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    /**
     * 转换对象
     *
     * @param sources        源对象list
     * @param targetSupplier 目标对象供应方
     * @param <S>            源对象
     * @param <T>            目标对象类型
     * @return 目标对象list
     */
    public static <S, T> List<T> convertList(@NonNull List<S> sources, @NonNull Supplier<T> targetSupplier) {
        List<T> list = new ArrayList<>(sources.size());
        for (S source : sources) {
            T target = targetSupplier.get();
            BeanUtils.copyProperties(source, target);
            list.add(target);
        }
        return list;
    }

}
