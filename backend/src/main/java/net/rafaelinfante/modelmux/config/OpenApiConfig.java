package net.rafaelinfante.modelmux.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI modelMuxOpenApi() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("model-mux API")
                                .version("0.1.0")
                                .description(
                                        "LLM gateway over multiple providers: cost-aware routing, per-provider "
                                                + "failover, token/cost budgets, caching, and AI-augmented developer skills.")
                                .license(new License().name("MIT")));
    }
}
