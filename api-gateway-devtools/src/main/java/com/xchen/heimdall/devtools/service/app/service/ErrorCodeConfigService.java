package com.xchen.heimdall.devtools.service.app.service;

import com.xchen.heimdall.devtools.service.app.dto.ErrorCodeConfDTO;
import com.xchen.heimdall.devtools.service.app.vo.BaseVO;
import com.xchen.heimdall.devtools.service.app.vo.ErrorCodeConfVO;
import com.xchen.heimdall.devtools.service.app.vo.qo.ErrorCodeConfQO;
import com.xchen.heimdall.dubbo.support.wrapper.PagedWrapper;
import com.xchen.heimdall.dubbo.support.wrapper.PagingWrapper;

/**
 * 统一错误码设置服务
 */
public interface ErrorCodeConfigService {

    /**
     * 新增错误码
     * @param vo 错误码设置
     */
    void addErrorCode (ErrorCodeConfVO vo);

    /**
     * 修改错误码
     * @param vo 错误码设置
     */
    void updateErrorCode (ErrorCodeConfVO vo);

    /**
     * 删除错误码
     * @param vo 错误码ID
     */
    void deleteErrorCode (BaseVO vo);

    /**
     * 分页查询错误码
     * @param condition 查询条件
     * @return r
     */
    PagedWrapper<ErrorCodeConfDTO> queryErrorCodeConfig (PagingWrapper<ErrorCodeConfQO> condition);

    /**
     * 发布错误码
     * @param errorCodeConf errorCode不为空则单个错误码发布，否则按照project来发布
     */
    void publishConfig(ErrorCodeConfQO errorCodeConf);

    /**
     * 根据工程和错误级别生成一个错误码
     * @param errorCodeConf 条件
     * @return r
     */
    ErrorCodeConfDTO generateErrorCode(ErrorCodeConfQO errorCodeConf);

}
