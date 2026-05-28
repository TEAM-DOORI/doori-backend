package com.doori.doori_backend.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"local", "dev"})
public class OpenApiConfig {

	@Bean
	public OpenAPI openAPI() {
		final String securitySchemeName = "bearerAuth";

		return new OpenAPI()
			.info(
				new Info()
					.title("Doori Backend API")
					.version("v1")
					.description("Doori Backend API 문서")
			)
			.components(new Components().addSecuritySchemes(
				securitySchemeName,
				new SecurityScheme()
					.type(SecurityScheme.Type.HTTP)
					.scheme("bearer")
					.bearerFormat("JWT")
			))
			.addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
	}
}
