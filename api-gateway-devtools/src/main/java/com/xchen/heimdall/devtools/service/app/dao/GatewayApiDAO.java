package com.xchen.heimdall.devtools.service.app.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xchen.heimdall.devtools.service.app.domain.GatewayApiDO;
import com.xchen.heimdall.devtools.service.app.dto.GatewayApiDTO;
import com.xchen.heimdall.devtools.service.app.vo.ListGatewayApiVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 
 * @date 2022/4/15
 */
public interface GatewayApiDAO extends BaseMapper<GatewayApiDO> {

    List<GatewayApiDTO> listAllDubboGatewayApi();
    List<GatewayApiDTO> listUnsyncedDubboGatewayApi();
    List<GatewayApiDTO> listDubboGatewayApiByBatchIds(@Param("ids") List<Integer> ids);
    List<GatewayApiDTO> listDubboGatewayApi(@Param("condition") ListGatewayApiVO condition);

    GatewayApiDTO findDetails(int id);

    void updateBatchGatewayApiSynced(@Param("synced") Boolean synced,
                                     @Param("gatewayApiIds") List<Integer> gatewayApiIds);
}
