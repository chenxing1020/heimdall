package com.xchen.heimdall.devtools.service.app.service;

import com.xchen.heimdall.devtools.service.app.dto.GatewayApiDTO;
import com.xchen.heimdall.devtools.service.app.dto.GatewayApiDocDTO;
import com.xchen.heimdall.devtools.service.app.vo.BaseVO;
import com.xchen.heimdall.devtools.service.app.vo.GatewayApiIdVO;
import com.xchen.heimdall.devtools.service.app.vo.GatewayApiVO;
import com.xchen.heimdall.devtools.service.app.vo.ListGatewayApiVO;

import java.util.List;

/**
 * @author 
 * @date 2022/4/15
 */
public interface IGatewayApiService {

    /**
     * 查询全量dubbo网关接口
     * @return 查询结果
     */
    List<GatewayApiDTO> listAllDubboGatewayApi(ListGatewayApiVO vo);

    /**
     * 查询所有未同步的dubbo网关接口
     * @return 查询结果
     */
    List<GatewayApiDTO> listUnsyncedDubboGatewayApi();

    /**
     * 新增网关接口
     * @param vo 网关api vo
     * @return 生成结果
     */
    GatewayApiDTO addGatewayApi(GatewayApiVO vo);

    /**
     * 更新网关接口
     * @param vo 网关api vo
     */
    void updateGatewayApi(GatewayApiVO vo);

    /**
     * 删除网关接口
     * @param vo 网关接口id
     */
    void deleteGatewayApi(GatewayApiIdVO vo);

    /**
     * 推送网关接口到apollo
     */
    void publishGatewayApi();


    /**
     * 根据ID查询API详情
     * @param vo ID
     * @return API详情
     */
    GatewayApiDocDTO findGatewayApiDetails(BaseVO vo);
}
