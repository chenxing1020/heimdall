package com.xchen.heimdall.devtools.service.app.service.impl;

import org.apache.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xchen.heimdall.devtools.service.app.common.constant.MethodException;
import com.xchen.heimdall.devtools.service.app.dao.DubboMethodDAO;
import com.xchen.heimdall.devtools.service.app.dao.DubboServiceDAO;
import com.xchen.heimdall.devtools.service.app.domain.DubboMethodDO;
import com.xchen.heimdall.devtools.service.app.domain.DubboMethodDetailDO;
import com.xchen.heimdall.devtools.service.app.dto.DubboMethodDTO;
import com.xchen.heimdall.devtools.service.app.service.IDubboMethodService;
import com.xchen.heimdall.devtools.service.app.vo.*;
import com.xchen.heimdall.devtools.service.app.vo.qo.SingleFindQO;
import com.xchen.heimdall.dubbo.support.wrapper.PagedWrapper;
import com.xchen.heimdall.dubbo.support.wrapper.PagingWrapper;
import com.xchen.heimdall.dubbo.util.PagingUtil;
import com.xchen.heimdall.common.util.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.xchen.heimdall.devtools.service.app.common.constant.DmtConstant.CODE_STATUS_SYNCED;
import static com.xchen.heimdall.devtools.service.app.common.constant.DmtConstant.CODE_STATUS_UNSYNCED;

/**
 * @author 
 * @date 2022/4/12
 */
@Slf4j
@Service
public class DubboMethodService implements IDubboMethodService {

    @Resource
    private DubboMethodDAO dubboMethodDAO;

    @Resource
    private DubboServiceDAO dubboServiceDAO;

    @Override
    public PagedWrapper<DubboMethodDTO> listMethod(PagingWrapper<ListDubboMethodVO> vo) {

        Page<?> page = new Page<>(vo.getPageNum(), vo.getPageSize());

        if (Objects.nonNull(vo.getData())) {
            return convertPaging(dubboMethodDAO.listMethod(page, vo.getData()));
        }

        return convertPaging(dubboMethodDAO.listAllMethod(page));
    }

    @Override
    public List<DubboMethodDTO> listMethodByServiceId(SingleFindQO serviceId) {
        List<DubboMethodDO> list = dubboMethodDAO.selectList(new LambdaQueryWrapper<DubboMethodDO>()
                .eq(DubboMethodDO::getServiceId, serviceId.getId()));
        return BeanUtil.convertList(list, DubboMethodDTO::new);
    }

    public List<DubboMethodDetailDO> listMethodDetailByServiceId(Integer serviceId) {
        return dubboMethodDAO.listMethodByServiceId(serviceId);
    }

    @Override
    public DubboMethodDTO addMethod(DubboMethodVO dubboMethodVO) {
        DubboMethodDO methodDO = BeanUtil.convert(dubboMethodVO, DubboMethodDO::new);
        methodDO.setCreateUserId(dubboMethodVO.getUserId());
        methodDO.setUpdateUserId(dubboMethodVO.getUserId());
        dubboMethodDAO.insert(methodDO);
        return BeanUtil.convert(methodDO, DubboMethodDTO::new);
    }

    @Override
    public void updateMethod(DubboMethodVO vo) {
        DubboMethodDO dubboMethodDO = BeanUtil.convert(vo, DubboMethodDO::new);
        dubboMethodDO.setCodeStatus(CODE_STATUS_UNSYNCED);
        dubboMethodDAO.updateById(dubboMethodDO);
    }

    @Override
    public void deleteMethod(DubboMethodIdVO vo) {
        dubboMethodDAO.delete(new LambdaQueryWrapper<DubboMethodDO>()
                .eq(DubboMethodDO::getId, vo.getMethodId())
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteMethodByServiceId(DubboServiceIdVO vo) {
        dubboMethodDAO.delete(new LambdaQueryWrapper<DubboMethodDO>()
                .eq(DubboMethodDO::getServiceId, vo.getServiceId())
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteMethodByPojoId(PojoIdVO vo) {
        dubboMethodDAO.delete(new LambdaQueryWrapper<DubboMethodDO>()
                .eq(DubboMethodDO::getVoId, vo.getPojoId())
                .or()
                .eq(DubboMethodDO::getDtoId, vo.getPojoId())
        );
    }

    public Set<Integer> getUnsyncedServiceIds() {
        List<DubboMethodDO> dubboMethods = dubboMethodDAO.selectList(new LambdaQueryWrapper<DubboMethodDO>()
                .eq(DubboMethodDO::getCodeStatus, CODE_STATUS_UNSYNCED)
        );
        return dubboMethods.stream().map(DubboMethodDO::getServiceId).collect(Collectors.toSet());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateCodeStatusUnsyncedByProjectId(Integer projectId) {

        List<Integer> serviceIdList = dubboServiceDAO.selectIdsByProjectId(projectId);

        if (!CollectionUtils.isEmpty(serviceIdList)) {
            dubboMethodDAO.update(null, new LambdaUpdateWrapper<DubboMethodDO>()
                    .in(DubboMethodDO::getServiceId, serviceIdList)
                    .set(DubboMethodDO::getCodeStatus, CODE_STATUS_UNSYNCED)
            );
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateCodeStatusUnsyncedByServiceId(Integer serviceId) {
        dubboMethodDAO.update(null, new LambdaUpdateWrapper<DubboMethodDO>()
                .eq(DubboMethodDO::getServiceId, serviceId)
                .set(DubboMethodDO::getCodeStatus, CODE_STATUS_UNSYNCED)
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateCodeStatusUnsyncedByServiceIdSet(Set<Integer> serviceIds) {
        if (!CollectionUtils.isEmpty(serviceIds)) {
            dubboMethodDAO.update(null, new LambdaUpdateWrapper<DubboMethodDO>()
                    .in(DubboMethodDO::getServiceId, serviceIds)
                    .set(DubboMethodDO::getCodeStatus, CODE_STATUS_UNSYNCED)
            );
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateCodeStatusSyncedAll() {
        dubboMethodDAO.update(null, new LambdaUpdateWrapper<DubboMethodDO>()
                .set(DubboMethodDO::getCodeStatus, CODE_STATUS_SYNCED)
        );
    }

    @Override
    public List<Map<String, String>> listMethodExceptions() {
        return Arrays.stream(MethodException.values()).map(item -> {
            Map<String, String> itemMap = new HashMap<>();
            itemMap.put("key", item.name());
            itemMap.put("value", item.getKey());
            return itemMap;
        }).collect(Collectors.toList());
    }

    private PagedWrapper<DubboMethodDTO> convertPaging(IPage<DubboMethodDetailDO> page) {
        return PagingUtil.convert(page, DubboMethodDTO::new);
    }
}
