package com.mok.ddd.web.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestResponse<T> {

    private int code;
    private boolean state;
    private String message;
    private T data;

    public static <T> RestResponse<T> success() {
        return success(null);
    }

    public static <T> RestResponse<T> success(T data) {
        return new RestResponse<>(200, true, "操作成功", data);
    }

    public static <T> RestResponse<T> failure(int code, String message) {
        return new RestResponse<>(code, false, message, null);
    }

    public static <T> RestResponse<T> failure(String message) {
        return failure(400, message);
    }
}