package com.mdc.backoffice;

import java.nio.file.*;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class OpenApiIntegrationTest {
    @Autowired MockMvc mvc;

    @Test
    void exportsTypedContractForAngularGeneration() throws Exception {
        var result = mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").value("3.0.1"))
                .andExpect(jsonPath("$.paths['/api/v1/departments'].post.operationId").value("createDepartment"))
                .andExpect(jsonPath("$.paths['/api/v1/cities'].get.operationId").value("listCities"))
                .andExpect(jsonPath("$.components.schemas.CityRequestDTO.required").isArray())
                .andExpect(jsonPath("$.components.schemas.CityRequestDTO.properties.departmentId.format").value("uuid"))
                .andExpect(jsonPath("$.components.schemas.DepartmentRequestDTO.properties.name.maxLength").value(100))
                .andExpect(jsonPath("$.components.schemas.DepartmentRequestDTO.properties.name.minLength").value(1))
                .andExpect(jsonPath("$.components.schemas.CityResponseDTO.properties.createdAt.format").value("date-time"))
                .andExpect(jsonPath("$.components.schemas.ApiProblem.properties.errors.additionalProperties.type").value("string"))
                .andReturn();
        var json = result.getResponse().getContentAsString();
        var root = JsonMapper.builder().build().readTree(json);
        var operationIds = new HashSet<String>();
        for (var path : root.path("paths")) {
            for (var operation : path) {
                assertTrue(operationIds.add(operation.path("operationId").asString()));
                assertEquals("#/components/schemas/ApiProblem",
                        operation.at("/responses/400/content/application~1problem+json/schema/$ref").asString());
            }
        }
        assertEquals(10, operationIds.size());
        for (var type : new String[]{"City", "Department"}) {
            var resource = type.equals("City") ? "cities" : "departments";
            var list = root.path("paths").path("/api/v1/" + resource).path("get");
            var ref = list.at("/responses/200/content/*~1*/schema/$ref");
            if (ref.isMissingNode()) {
                ref = list.at("/responses/200/content/application~1json/schema/$ref");
            }
            var page = root.at(ref.asString().substring(1));
            assertEquals("#/components/schemas/" + type + "ResponseDTO",
                    page.at("/properties/content/items/$ref").asString());
            assertTrue(page.path("properties").has("totalElements"));
            var required = root.path("components").path("schemas").path(type + "RequestDTO").path("required");
            assertTrue(required.toString().contains("name"));
            if (type.equals("City")) {
                assertTrue(required.toString().contains("departmentId"));
            }
        }
        Files.createDirectories(Path.of("target"));
        Files.writeString(Path.of("target/openapi.json"), json);
    }

    @Test
    void swaggerUiIsAvailableLocally() throws Exception {
        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Swagger UI")));
    }

    @Test
    void angularCanCallLocalApi() throws Exception {
        mvc.perform(options("/api/v1/cities")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }
}
