package com.zkril.aerial_back.util;

import lombok.Getter;

/**
 * 统一返回结果状态信息类
 *
 */
@Getter
public enum ResultCodeEnum {

    SUCCESS(200,"成功"),
    FAIL(201, "失败"),
    SERVICE_ERROR(2012, "服务异常"),
    ILLEGAL_REQUEST( 204, "非法请求"),
    ARGUMENT_VALID_ERROR(206, "参数校验错误"),
    LOGIN_ERROR(207, "用户名或密码错误"),
    LOGIN_AUTH(208, "未登陆"),
    PERMISSION(209, "没有权限"),
    SECKILL_ILLEGAL(217, "请求不合法"),
    ;

    private Integer code;

    private String message;

    private ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}