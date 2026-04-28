package com.doori.doori_backend.global.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.doori.doori_backend.global.error.ErrorCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandlerIntegrationTest.TestController.class)
class GlobalExceptionHandlerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void customException_isMappedToErrorResponse() throws Exception {
		mockMvc.perform(get("/test/custom"))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.status").value(403))
			.andExpect(jsonPath("$.code").value("A002"))
			.andExpect(jsonPath("$.message").value("접근 권한이 없습니다."))
			.andExpect(jsonPath("$.path").value("/test/custom"))
			.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	void validationException_isMappedToBadRequest() throws Exception {
		mockMvc.perform(post("/test/validation")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.code").value("C001"))
			.andExpect(jsonPath("$.message").value("name: must not be blank"))
			.andExpect(jsonPath("$.path").value("/test/validation"))
			.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	void unknownException_isMappedToInternalServerError() throws Exception {
		mockMvc.perform(get("/test/runtime"))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.status").value(500))
			.andExpect(jsonPath("$.code").value("C002"))
			.andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."))
			.andExpect(jsonPath("$.path").value("/test/runtime"))
			.andExpect(jsonPath("$.timestamp").exists());
	}

	@RestController
	@RequestMapping("/test")
	public static class TestController {

		@GetMapping("/custom")
		public void customException() {
			throw new CustomException(ErrorCode.AUTH_FORBIDDEN);
		}

		@PostMapping("/validation")
		public void validationException(@Valid @RequestBody TestRequest request) {
		}

		@GetMapping("/runtime")
		public void runtimeException() {
			throw new IllegalStateException("unexpected");
		}
	}

	record TestRequest(@NotBlank String name) {
	}
}
