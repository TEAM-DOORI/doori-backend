package com.doori.doori_backend.global.error;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

public record ErrorResponse(
	int status,
	String code,
	String message,
	String path,
	LocalDateTime timestamp
) {

	public static ErrorResponse of(ErrorCode errorCode, HttpServletRequest request) {
		return new ErrorResponse(
			errorCode.getHttpStatus().value(),
			errorCode.getCode(),
			errorCode.getDefaultMessage(),
			request.getRequestURI(),
			LocalDateTime.now()
		);
	}

	public static ErrorResponse of(ErrorCode errorCode, String message, HttpServletRequest request) {
		return new ErrorResponse(
			errorCode.getHttpStatus().value(),
			errorCode.getCode(),
			message,
			request.getRequestURI(),
			LocalDateTime.now()
		);
	}
}
