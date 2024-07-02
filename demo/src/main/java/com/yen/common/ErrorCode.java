package com.yen.common;

/**
 * 自定义错误码
 *
 */
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    INVITATION_CODE_ERROR(40401,"邀请码错误"), // 一般为对应的邀请者Id不存在
    AGENT_NOT_FOUND_ERROR(40402,"客服不存在"),
    CONSUMER_NOT_FOUND_ERROR(40403,"客户不存在"),
    PROVIDER_NOT_CONFIG_ERROR(40410,"区域服务商没有正确进行相关配置"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败"),
    API_REQUEST_ERROR(50010, "接口调用失败"),
    SERVICE_API_REQUEST_ERROR(50011, "服务内部之间接口调用失败"),
    DATABASE_ERROR(50020, "数据库操作失败");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
