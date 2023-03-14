package com.xchen.heimdall.devtools.service.app.common.constant;

public class ErrorCode {

    /**
     * 没有修改项可发布
     */
    public static final int NO_UPDATE = 210010001;

    /**
     * 数据不存在
     */
    public static final int NOT_EXIST = 210010002;

    /**
     * 编号已存在
     */
    public static final int DUPLICATE_CODE = 210010003;

    /**
     * 错误码格式错误
     */
    public static final int FORMAT_ERROR = 210010011;

    /**
     * maven编译出错
     */
    public static final int MAVEN_COMPILE_ERROR = 210010004;
    /**
     * git clone操作出错
     */
    public static final int GIT_CLONE_ERROR = 210010005;
    /**
     * git push操作出错
     */
    public static final int GIT_PUSH_ERROR = 210010006;
    /**
     * git mr操作出错
     */
    public static final int GIT_MR_ERROR = 210010007;
    /**
     * 文件操作出错
     */
    public static final int FILE_OPS_ERROR = 210010008;
    /**
     * 禁止删除操作
     */
    public static final int FORBIDDEN_DELETE_ERROR = 210010012;

    /**
     * 同步apollo失败
     */
    public static final int APOLLO_PUSH_ERROR = 110010009;

}
