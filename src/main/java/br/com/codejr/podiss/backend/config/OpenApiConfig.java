package br.com.codejr.podiss.backend.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.*;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Centralizes HTTP documentation shared by controllers without changing their execution.
 * Security requirements describe the existing method guards, not a new authorization policy.
 * @author oEnzoRibas
 */
@Configuration
public class OpenApiConfig {
    @Bean
    org.springdoc.core.customizers.OpenApiCustomizer episodePathTemplate() {
        return api -> {
            // OpenAPI forbids duplicate templates differing only in the parameter name.
            // MVC still uses youtubeId for GET and UUID id for DELETE.
            PathItem lookup = api.getPaths().remove("/episodes/{youtubeId}");
            if (lookup != null) {
                lookup.getGet().getParameters().stream().filter(p -> "path".equals(p.getIn()))
                    .forEach(p -> p.setName("id"));
                api.getPaths().get("/episodes/{id}").setGet(lookup.getGet());
            }
        };
    }

    @Bean
    OpenAPI podissOpenApi() {
        return new OpenAPI().info(new Info().title("PodIss API").version("1.1.0")
            .description("Legacy HTTP contract; English persistence schema. Credits: oEnzoRibas.")
            .contact(new Contact().name("oEnzoRibas")))
            .components(new Components().addSecuritySchemes("bearerAuth",
                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))
                .addSchemas("ApiProblem", new ObjectSchema()
                    .addProperty("type", new StringSchema().format("uri"))
                    .addProperty("title", new StringSchema())
                    .addProperty("status", new IntegerSchema())
                    .addProperty("detail", new StringSchema())
                    .addProperty("instance", new StringSchema().format("uri"))));
    }

    @Bean
    OperationCustomizer contractDetails() {
        return (operation, handler) -> {
            boolean admin = handler.hasMethodAnnotation(PreAuthorize.class);
            operation.setSecurity(admin ? List.of(new SecurityRequirement().addList("bearerAuth")) : List.of());
            operation.setDescription(operation.getDescription() + (admin
                ? " Requires an authenticated ADMIN account." : " Public operation; no token required."));
            // Invalid explicitly supplied Bearer credentials fail even on public operations.
            operation.getResponses().addApiResponse("401", problem("Missing or invalid credentials."));
            if (admin) operation.getResponses().addApiResponse("403", problem("ADMIN role required."));
            operation.getResponses().addApiResponse("400", problem("Invalid fields, pagination or identifier."));
            String method = handler.getMethod().getName();
            if (handler.hasMethodAnnotation(PostMapping.class) && !method.equals("login")) {
                ApiResponse success = operation.getResponses().remove("200");
                if (success != null) operation.getResponses().addApiResponse("201", success.description("Created."));
                operation.getResponses().addApiResponse("409", problem("Uniqueness or persistence conflict."));
            }
            if (handler.hasMethodAnnotation(DeleteMapping.class)) {
                operation.getResponses().remove("200");
                operation.getResponses().addApiResponse("204", new ApiResponse().description("Removed; no response body."));
            }
            if (List.of("get", "update", "delete", "image", "replaceImage", "removeImage").contains(method))
                operation.getResponses().addApiResponse("404", problem("Resource or image not found."));
            if (handler.getBeanType().getSimpleName().equals("PostController")
                    && List.of("create", "replaceImage").contains(method))
                operation.getResponses().addApiResponse("413", problem("File exceeds 5 MiB or multipart request exceeds 6 MiB."));
            if (handler.getBeanType().getSimpleName().equals("EpisodeController") && method.equals("create")) {
                operation.getResponses().addApiResponse("502", problem("YouTube metadata unavailable or invalid."));
                operation.getResponses().addApiResponse("504", problem("YouTube timeout."));
            }
            if (List.of("ContactMessageController", "TopicSuggestionController").contains(handler.getBeanType().getSimpleName())
                    && method.equals("create"))
                operation.getResponses().addApiResponse("429", problem("Submission quota exceeded.")
                    .addHeaderObject("Retry-After", new Header().description("Seconds until retry.").schema(new IntegerSchema())));
            if (method.equals("list")) {
                ApiResponse response = operation.getResponses().get("200");
                for (String header : List.of("X-Total-Count", "X-Total-Pages", "X-Page", "X-Page-Size"))
                    response.addHeaderObject(header, new Header().schema(new IntegerSchema().format("int64")));
                if (operation.getParameters() != null) operation.getParameters().forEach(parameter -> {
                    switch (parameter.getName()) {
                        case "page" -> parameter.setDescription("Zero-based page; minimum 0.");
                        case "size" -> parameter.setDescription("Page size from 1 to 100; default 100.");
                        case "order" -> parameter.setDescription("Exactly asc or desc; sorted by createdAt then id.");
                        default -> { }
                    }
                });
            }
            if (method.equals("image"))
                operation.getResponses().get("200").setContent(new Content()
                    .addMediaType("image/jpeg", binary()).addMediaType("image/png", binary())
                    .addMediaType("image/gif", binary()).addMediaType("image/webp", binary()));
            return operation;
        };
    }

    private static MediaType binary() {
        return new MediaType().schema(new StringSchema().format("binary"));
    }

    private static ApiResponse problem(String description) {
        return new ApiResponse().description(description).content(new Content()
            .addMediaType("application/problem+json", new MediaType().schema(new Schema<>().$ref("#/components/schemas/ApiProblem"))));
    }
}
