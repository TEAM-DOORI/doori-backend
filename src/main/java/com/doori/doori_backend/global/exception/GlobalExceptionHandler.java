package com.doori.doori_backend.global.exception;

import com.doori.doori_backend.global.error.ErrorCode;
import com.doori.doori_backend.global.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponse> handleCustomException(
		CustomException ex,
		HttpServletRequest request
	) {
		ErrorCode errorCode = ex.getErrorCode();
		ErrorResponse response = ErrorResponse.of(errorCode, ex.getMessage(), request);
		return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(
		MethodArgumentNotValidException ex,
		HttpServletRequest request
	) {
		String message = ex.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
			.collect(Collectors.joining(", "));

		ErrorResponse response = ErrorResponse.of(
			ErrorCode.COMMON_BAD_REQUEST,
			message.isBlank() ? ErrorCode.COMMON_BAD_REQUEST.getDefaultMessage() : message,
			request
		);
		return ResponseEntity.status(ErrorCode.COMMON_BAD_REQUEST.getHttpStatus()).body(response);
	}

	@ExceptionHandler({
		BindException.class,
		MethodArgumentTypeMismatchException.class,
		MissingServletRequestParameterException.class
	})
	public ResponseEntity<ErrorResponse> handleBindingException(Exception ex, HttpServletRequest request) {
		ErrorResponse response = ErrorResponse.of(
			ErrorCode.COMMON_BAD_REQUEST,
			ex.getMessage(),
			request
		);
		return ResponseEntity.status(ErrorCode.COMMON_BAD_REQUEST.getHttpStatus()).body(response);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleNoResourceFoundException(
		NoResourceFoundException ex,
		HttpServletRequest request
	) {
		ErrorResponse response = ErrorResponse.of(
			ErrorCode.COMMON_NOT_FOUND,
			ErrorCode.COMMON_NOT_FOUND.getDefaultMessage(),
			request
		);
		return ResponseEntity.status(ErrorCode.COMMON_NOT_FOUND.getHttpStatus()).body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest request) {
		ErrorResponse response = ErrorResponse.of(
			ErrorCode.COMMON_INTERNAL_SERVER_ERROR,
			request
		);
		return ResponseEntity.status(ErrorCode.COMMON_INTERNAL_SERVER_ERROR.getHttpStatus()).body(response);
	}
}
