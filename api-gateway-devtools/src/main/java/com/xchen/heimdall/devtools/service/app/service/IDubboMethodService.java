package com.xchen.heimdall.devtools.service.app.service;

import com.xchen.heimdall.devtools.service.app.dto.DubboMethodDTO;
import com.xchen.heimdall.devtools.service.app.vo.DubboMethodIdVO;
import com.xchen.heimdall.devtools.service.app.vo.DubboMethodVO;
import com.xchen.heimdall.devtools.service.app.vo.ListDubboMethodVO;
import com.xchen.heimdall.devtools.service.app.vo.qo.SingleFindQO;
import com.xchen.heimdall.dubbo.support.wrapper.PagedWrapper;
import com.xchen.heimdall.dubbo.support.wrapper.PagingWrapper;

import java.util.List;
import java.util.Map;

/**
 * @author 
 * @date 2022/4/12
 */
public interface IDubboMethodService {

    /**
     * 查找method
     * @param vo 查找条件
     * @return 方法信息list
     */
    PagedWrapper<DubboMethodDTO> listMethod(PagingWrapper<ListDubboMethodVO> vo);

    /**
     * 新增方法
     *
     * @param vo 方法信息
     * @return 方法dto
     */
    DubboMethodDTO addMethod(DubboMethodVO vo);

    /**
     * 方法更新
     *
     * @param vo method信息
     */
    void updateMethod(DubboMethodVO vo);

    /**
     * 删除method
     *
     * @param vo methodId
     */
    void deleteMethod(DubboMethodIdVO vo);

    /**
     * 查询服务下的方法
     * @param serviceId 服务ID
     * @return 方法列表
     */
    List<DubboMethodDTO> listMethodByServiceId(SingleFindQO serviceId);

    /**
     * 展示可选的方法异常类
     * @return 方法异常
     */
    List<Map<String, String>> listMethodExceptions();
}
