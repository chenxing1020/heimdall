package com.xchen.heimdall.dubbo.support.wrapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 分页请求的封装
 *
 * @author xchen
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagingWrapper<T extends Serializable> implements Serializable {

    /**
     * 数据负载
     */
    @Valid
    protected T data;

    /**
     * 页码，从1开始计数
     */
    @NotNull
    @Min(1)
    protected Integer pageNum;

    /**
     * 每页的大小，上限1000条防止攻击
     */
    @NotNull
    @Min(1)
    @Max(1000)
    protected Integer pageSize;

}
