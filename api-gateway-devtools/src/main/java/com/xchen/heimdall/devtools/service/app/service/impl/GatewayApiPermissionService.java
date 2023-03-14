package com.xchen.heimdall.devtools.service.app.service.impl;

import org.apache.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xchen.heimdall.devtools.service.app.dao.GatewayApiPermissionDAO;
import com.xchen.heimdall.devtools.service.app.domain.GatewayApiPermissionDO;
import com.xchen.heimdall.devtools.service.app.service.IGatewayApiPermissionService;
import com.xchen.heimdall.devtools.service.app.vo.GatewayApiIdsVO;
import com.xchen.heimdall.devtools.service.app.vo.PermissionIdsVO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xchen
 * @date 2022/7/18
 */
@Service
public class GatewayApiPermissionService implements IGatewayApiPermissionService {

    @Resource
    private GatewayApiPermissionDAO gatewayApiPermissionDAO;

    public List<Integer> selectPermissionIds(Integer gatewayApiId) {
        return gatewayApiPermissionDAO.selectPermissionIds(gatewayApiId);
    }

    public List<Integer> selectGatewayApiIds(Integer permissionId) {
        return gatewayApiPermissionDAO.selectGatewayApiIds(permissionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePermissionsByGatewayApiId(PermissionIdsVO vo) {
        // 先删除，再更新
        gatewayApiPermissionDAO.delete(new LambdaQueryWrapper<GatewayApiPermissionDO>()
                .eq(GatewayApiPermissionDO::getGatewayApiId, vo.getGatewayApiId())
        );
        if (!CollectionUtils.isEmpty(vo.getPermissionIds())) {
            gatewayApiPermissionDAO.insertBatchPermission(vo.getGatewayApiId(), vo.getPermissionIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGatewayApisByPermissionId(GatewayApiIdsVO vo) {
        // 先删除，再更新
        gatewayApiPermissionDAO.delete(new LambdaQueryWrapper<GatewayApiPermissionDO>()
                .eq(GatewayApiPermissionDO::getPermissionId, vo.getPermissionId())
        );
        if (!CollectionUtils.isEmpty(vo.getGatewayApiIds())) {
            gatewayApiPermissionDAO.insertBatchGatewayApi(vo.getPermissionId(), vo.getGatewayApiIds());
        }
    }
}
