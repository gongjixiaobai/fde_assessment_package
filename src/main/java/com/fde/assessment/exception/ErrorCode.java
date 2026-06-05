package com.fde.assessment.exception;

/**
 * 统一错误码枚举 — 加分项 4
 */
public enum ErrorCode {

    // 工作项
    WORKITEM_NOT_FOUND("WORKITEM_NOT_FOUND", "工作项不存在"),
    WORKITEM_CREATE_FAILED("WORKITEM_CREATE_FAILED", "创建工作项失败"),

    // 状态流转
    ILLEGAL_TRANSITION("ILLEGAL_TRANSITION", "非法状态流转"),
    UNRESOLVED_HIGH_PRIORITY_QUESTIONS("UNRESOLVED_HIGH_PRIORITY_QUESTIONS", "存在未解决的高优先级澄清问题"),
    CONCURRENT_MODIFICATION("CONCURRENT_MODIFICATION", "数据已被其他用户修改，请刷新后重试"),

    // 澄清问题
    QUESTION_NOT_FOUND("QUESTION_NOT_FOUND", "澄清问题不存在"),

    // 校验
    VALIDATION_ERROR("VALIDATION_ERROR", "请求参数校验失败"),
    ILLEGAL_ARGUMENT("ILLEGAL_ARGUMENT", "参数非法"),

    // 认证
    UNAUTHORIZED("UNAUTHORIZED", "未认证或认证已过期"),
    FORBIDDEN("FORBIDDEN", "无权限执行此操作"),

    // AI 服务
    EXTERNAL_SERVICE_ERROR("EXTERNAL_SERVICE_ERROR", "外部服务调用失败"),

    // 系统
    INTERNAL_ERROR("INTERNAL_ERROR", "服务器内部错误");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() { return code; }

    public String getDefaultMessage() { return defaultMessage; }
}
