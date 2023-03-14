package com.xchen.heimdall.devtools.service.app.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xchen.heimdall.devtools.service.app.domain.ErrorCodeConfDO;
import com.xchen.heimdall.devtools.service.app.dto.ErrorCodeConfDTO;
import com.xchen.heimdall.devtools.service.app.vo.qo.ErrorCodeConfQO;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface ErrorCodeConfigDAO extends BaseMapper<ErrorCodeConfDO> {

    List<ErrorCodeConfDO> query(ErrorCodeConfDO condition);

    IPage<ErrorCodeConfDTO> queryPage(IPage<?> page, @Param("condition") ErrorCodeConfQO condition);

    void deleteReleaseErrorCode(@Param("codeList") List<String> codeList, @Param("version") String version);

    List<ErrorCodeConfDO> queryChangedConfig(@Param("projectId") Integer projectId,
                                             @Param("updateTime") Date updateTime);

    List<Integer> queryAllErrorCodeProject();

    String getMaxErrorCodeOfProject(@Param("projectId") Integer projectId, @Param("level") Integer level);
}
