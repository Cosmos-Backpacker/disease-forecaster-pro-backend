package com.cosmos.diseaseforecasterpro.common;

import lombok.Getter;


@Getter
public enum ErrorCode {

    SUCCESS(200, "ok", ""),
    PARAMS_ERROR(40000, "请求参数错误", ""),
    NULL_ERROR(40001, "请求数据为空", ""),
    NO_LOGIN(40100, "未登录", ""),
    NO_AUTH(40101, "无权限", ""),
    ALREADY_ACCOUNT(500, "账号已存在", ""),
    NOT_FOUND_ERROR(40002, "请求数据不存在", ""),
    SYSTEM_ERROR(50000, "系统内部异常", "");


    private final int code;

    private final String msg;

    private final String description;

    ErrorCode(int code, String msg, String description) {
        this.code = code;
        this.msg = msg;
        this.description = description;

    }

}
