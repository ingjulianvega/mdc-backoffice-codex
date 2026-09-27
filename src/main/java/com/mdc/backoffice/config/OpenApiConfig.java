package com.mdc.backoffice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI backofficeOpenApi() {
        return new OpenAPI()
                .info(new Info().title("Mi Dulce Compania - Backoffice API")
                        .version("1.0.0")
                        .description("Contrato del backend para clientes web. UUID como string; fechas ISO-8601 UTC."))
                .servers(List.of(new Server().url("/").description("Servidor actual")));
    }
}
