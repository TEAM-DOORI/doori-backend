package com.doori.doori_backend.global.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("prod")
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class SwaggerProdAccessIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void swaggerUi_isDisabledInProdProfile() throws Exception {
		mockMvc.perform(get("/swagger-ui/index.html"))
			.andExpect(status().isNotFound());
	}
}
