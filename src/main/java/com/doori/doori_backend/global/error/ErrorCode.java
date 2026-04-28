package com.doori.doori_backend.global.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
	COMMON_BAD_REQUEST(HttpStatus.BAD_REQUEST, "C001", "잘못된 요청입니다."),
	COMMON_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다."),
	COMMON_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "요청한 리소스를 찾을 수 없습니다."),
	AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
	AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "A002", "접근 권한이 없습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String defaultMessage;

	ErrorCode(HttpStatus httpStatus, String code, String defaultMessage) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.defaultMessage = defaultMessage;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	public String getCode() {
		return code;
	}

	public String getDefaultMessage() {
		return defaultMessage;
	}
}
