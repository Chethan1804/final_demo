package com.AI_service.dto;

public class ApiResponse<T> {
    private String message;
    private T data;
    private String error;

    public ApiResponse() {}

    public ApiResponse(String message, T data, String error) {
        this.message = message;
        this.data = data;
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(message, data, null);
    }

    public static <T> ApiResponse<T> error(String message, String error) {
        return new ApiResponse<>(message, null, error);
    }
}
