package com.gts.auth.global.common.response;

public record ApiResult<T>(
        boolean success,
        T data,
        ApiError error
) {
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(true, data, null);
    }

    public static ApiResult<?> fail(String code, String message) {
        return new ApiResult<>(false, null, new ApiError(code, message));
    }

    public record ApiError(String code, String message) {}
}
