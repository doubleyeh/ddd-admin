package com.mok.ddd.web.rest;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestResponse<T> {

    private int code;
    private String message;
    private T data;

    public static <T> RestResponse<T> success() {
        return success(null);
    }

    public static <T> RestResponse<T> success(T data) {
        return new RestResponse<>(200, "操作成功", data);
    }

    public static <T> RestResponse<T> failure(int code, String message) {
        return new RestResponse<>(code, message, null);
    }
}