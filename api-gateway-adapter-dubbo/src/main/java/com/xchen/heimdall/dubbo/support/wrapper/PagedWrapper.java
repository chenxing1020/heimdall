package com.xchen.heimdall.dubbo.support.wrapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 分页结果的封装
 *
 * @author xchen
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedWrapper<T extends Serializable> implements Serializable {

    /**
     * 数据负载
     */
    protected List<T> data;

    /**
     * 结果总数
     */
    @NotNull
    protected Long total;

    /**
     * 每页的数量
     */
    @NotNull
    protected Integer pageSize;

    /**
     * 页码，从1开始计数
     */
    @NotNull
    protected Integer pageNum;

}
