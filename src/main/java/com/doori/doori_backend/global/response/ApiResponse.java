package com.doori.doori_backend.global.response;

public record ApiResponse<T>(
	boolean success,
	String message,
	T data
) {

	private static final String DEFAULT_SUCCESS_MESSAGE = "요청 성공";

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, DEFAULT_SUCCESS_MESSAGE, data);
	}

	public static <T> ApiResponse<T> success(String message, T data) {
		return new ApiResponse<>(true, message, data);
	}
}
