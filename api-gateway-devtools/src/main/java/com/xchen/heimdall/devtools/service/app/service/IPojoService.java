package com.xchen.heimdall.devtools.service.app.service;

import com.xchen.heimdall.devtools.service.app.dto.FieldTypeDTO;
import com.xchen.heimdall.devtools.service.app.dto.ParentPojoDTO;
import com.xchen.heimdall.devtools.service.app.dto.PojoDTO;
import com.xchen.heimdall.devtools.service.app.vo.*;
import com.xchen.heimdall.dubbo.support.wrapper.PagedWrapper;
import com.xchen.heimdall.dubbo.support.wrapper.PagingWrapper;

import java.util.List;
import java.util.Map;

/**
 * @author 
 * @date 2022/4/13
 */
public interface IPojoService {

    /**
     * 新增pojo
     *
     * @param vo pojoVO
     * @return 新增结果
     */
    PojoDTO addPojo(PojoVO vo);

    /**
     * 更新pojo
     *
     * @param vo pojoVO
     */
    void updatePojo(PojoVO vo);

    /**
     * 删除pojo
     *
     * @param vo
     */
    void deletePojo(PojoIdVO vo);

    /**
     * 分页查询pojo
     *
     * @param vo 查询条件
     * @return 查询结果
     */
    PagedWrapper<PojoDTO> listPojo(PagingWrapper<ListPojoVO> vo);

    /**
     * 查询所有父类
     * @param vo 查询条件
     * @return 查询结果
     */
    List<ParentPojoDTO> listParentPojo(ListParentPojoVO vo);

    /**
     * 根据projectId查询所有的字段类型
     *
     * @param vo 项目id vo
     * @return 所有可用的字段类型
     */
    List<FieldTypeDTO> listFieldType(ProjectIdVO vo);

    /**
     * 查询vo包装类
     *
     * @return vo包装类型
     */
    List<Map<String, String>> listVoWrapperType();

    /**
     * 查询dto包装类
     *
     * @return dto包装类
     */
    List<Map<String, String>> listDtoWrapperType();

    /**
     * 预览pojo
     *
     * @param vo pojoId
     * @return pojo代码模板
     */
    String previewPojo(PojoIdVO vo);
}
