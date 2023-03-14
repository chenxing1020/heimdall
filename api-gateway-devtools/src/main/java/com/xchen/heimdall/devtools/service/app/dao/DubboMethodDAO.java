package com.xchen.heimdall.devtools.service.app.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xchen.heimdall.devtools.service.app.domain.DubboMethodDO;
import com.xchen.heimdall.devtools.service.app.domain.DubboMethodDetailDO;
import com.xchen.heimdall.devtools.service.app.vo.ListDubboMethodVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xchen
 */
public interface DubboMethodDAO extends BaseMapper<DubboMethodDO> {

    IPage<DubboMethodDetailDO> listAllMethod(IPage<?> page);

    IPage<DubboMethodDetailDO> listMethod(IPage<?> page, @Param("condition") ListDubboMethodVO condition);

    /**
     * 根据方法名模糊搜索方法列表
     *
     * @param page       分页参数
     * @param methodName 方法名
     * @return
     */
    IPage<DubboMethodDetailDO> listMethodByMethodName(IPage<?> page, @Param("methodName") String methodName);

    IPage<DubboMethodDetailDO> listMethodByServiceName(IPage<?> page, @Param("serviceName") String serviceName);

    IPage<DubboMethodDetailDO> listMethodByProjectName(IPage<?> page, @Param("projectName") String projectName);

    List<DubboMethodDetailDO> listMethodByServiceId(@Param("id") Integer id);
}
