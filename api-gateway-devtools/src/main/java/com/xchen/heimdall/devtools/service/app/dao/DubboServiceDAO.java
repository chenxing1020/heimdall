package com.xchen.heimdall.devtools.service.app.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xchen.heimdall.devtools.service.app.domain.DubboServiceDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xchen
 */
public interface DubboServiceDAO extends BaseMapper<DubboServiceDO> {

    List<Integer> selectIdsByProjectId(@Param("projectId")Integer projectId);
}
