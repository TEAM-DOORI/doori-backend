package com.doori.doori_backend.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"local", "dev"})
public class OpenApiConfig {

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI().info(
			new Info()
				.title("Doori Backend API")
				.version("v1")
				.description("Doori Backend API 문서")
		);
	}
}
