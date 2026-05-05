package com.doori.doori_backend.global.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
	COMMON_BAD_REQUEST(HttpStatus.BAD_REQUEST, "C001", "잘못된 요청입니다."),
	COMMON_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다."),
	COMMON_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "요청한 리소스를 찾을 수 없습니다."),
	AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
	AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "A002", "접근 권한이 없습니다."),
	AUTH_EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "A003", "이메일 인증이 완료되지 않았습니다."),
	AUTH_EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "A004", "이미 가입된 이메일입니다."),
	AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A005", "이메일 또는 비밀번호가 올바르지 않습니다."),
	AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A006", "유효하지 않은 토큰입니다."),
	AUTH_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A007", "만료된 토큰입니다."),
	AUTH_UNSUPPORTED_EMAIL(HttpStatus.BAD_REQUEST, "A008", "지원하지 않는 학교 이메일입니다."),
	AUTH_INVALID_CODE(HttpStatus.BAD_REQUEST, "A009", "인증 코드가 올바르지 않습니다."),
	AUTH_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "A010", "인증 코드가 만료되었습니다.");

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
