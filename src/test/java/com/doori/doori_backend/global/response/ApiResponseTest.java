package com.doori.doori_backend.global.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApiResponseTest {

	@Test
	void success_withData_usesDefaultMessage() {
		ApiResponse<String> response = ApiResponse.success("data");

		assertThat(response.success()).isTrue();
		assertThat(response.message()).isEqualTo("요청 성공");
		assertThat(response.data()).isEqualTo("data");
	}

	@Test
	void success_withMessageAndData_usesProvidedMessage() {
		ApiResponse<Integer> response = ApiResponse.success("생성 완료", 1);

		assertThat(response.success()).isTrue();
		assertThat(response.message()).isEqualTo("생성 완료");
		assertThat(response.data()).isEqualTo(1);
	}
}
