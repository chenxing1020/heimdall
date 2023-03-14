package com.xchen.heimdall.devtools.service.app.service;

import com.xchen.heimdall.devtools.service.app.dto.DubboServiceDTO;
import com.xchen.heimdall.devtools.service.app.vo.AssigneeUserIdVO;
import com.xchen.heimdall.devtools.service.app.vo.DubboServiceIdVO;
import com.xchen.heimdall.devtools.service.app.vo.DubboServiceVO;
import com.xchen.heimdall.devtools.service.app.vo.ListDubboServiceVO;
import com.xchen.heimdall.dubbo.support.wrapper.PagedWrapper;
import com.xchen.heimdall.dubbo.support.wrapper.PagingWrapper;

/**
 * @author 
 * @date 2022/4/14
 */
public interface IDubboService {
    /**
     * 分页展示service
     * @param vo 查找条件
     * @return 查询结果
     */
    PagedWrapper<DubboServiceDTO> listService(PagingWrapper<ListDubboServiceVO> vo);

    /**
     * 增加service
     * @param vo serviceVO
     * @return 新增结果
     */
    DubboServiceDTO addService(DubboServiceVO vo);

    /**
     * 更新service
     * @param vo serviceVO
     */
    void updateService(DubboServiceVO vo);

    /**
     * 删除服务
     * @param vo 服务id
     */
    void deleteService(DubboServiceIdVO vo);

    /**
     * 预览服务
     * @param vo 服务id
     * @return 服务代码模板
     */
    String previewService(DubboServiceIdVO vo);

    /**
     * 生成服务
     * @param vo 用户id
     * @return mr的web地址
     */
    String generateAllService(AssigneeUserIdVO vo);
}
