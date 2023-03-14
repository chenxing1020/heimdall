package com.xchen.heimdall.devtools.service.app.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xchen.heimdall.devtools.service.app.domain.ConfigVersionDO;
import org.apache.ibatis.annotations.Param;

public interface ConfigVersionDAO extends BaseMapper<ConfigVersionDO> {

    String findLastVersion(@Param("projectId")Integer projectId, @Param("type") int type);
}
