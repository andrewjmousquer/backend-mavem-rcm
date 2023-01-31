package com.portal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;

//@Configuration
//@Profile("dev")
//@EnableSwagger2
@Configuration
@OpenAPIDefinition(info = @Info(title = "Portal Carbon", version = "1.0.0"))
@SecurityScheme(
		 name = "bearerAuth",
		 type = SecuritySchemeType.HTTP,
		 bearerFormat = "JWT",
		 scheme = "bearer"
		)
public class SwaggerConfig {
	
//	@Autowired
//	private PortalJwtTokenUtil jwtTokenUtil;
//
//	@Autowired
//	private UserDetailsService userDetailsService;
//
//	@Bean
//	public Docket api() {
//		return new Docket(DocumentationType.SWAGGER_2).select()
//				.apis(RequestHandlerSelectors.basePackage("com.portal.controller"))
//				.paths(PathSelectors.any()).build()
//				.apiInfo(apiInfo());
//	}
//
//	private ApiInfo apiInfo() {
//		return new ApiInfoBuilder().title("Web Portal Base")
//				.description("Documentação da API de acesso aos endpoints do Portal Base.").version("1.0")
//				.build();
//	}
//
//	@Bean
//	public SecurityConfiguration security() {
//		String token;
//		try {
//			UserDetails userDetails = this.userDetailsService.loadUserByUsername( "root" );
//			token = this.jwtTokenUtil.obterToken(userDetails);
//		} catch (Exception e) {
//			token = "";
//		}
//
//		return new SecurityConfiguration(null, null, null, null, "Bearer " + token, ApiKeyVehicle.HEADER, "Authorization", ",");
//	}
	
	@Bean
	public OpenAPI CustomOpenApiConfig() {
		final String securitySchemeName = "bearerAuth";
		return new OpenAPI().components(new Components())
				.addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
	}

}
