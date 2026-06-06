package com.okanetransfer.config;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.util.Set;

public class OpenApiServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            WebApplicationContext ctx = WebApplicationContextUtils
                    .getRequiredWebApplicationContext(getServletContext());

            RequestMappingHandlerMapping mapping = ctx.getBean(RequestMappingHandlerMapping.class);

            Paths paths = new Paths();
            Components components = new Components()
                    .addSecuritySchemes("bearerAuth",
                            new SecurityScheme()
                                    .name("bearerAuth")
                                    .type(SecurityScheme.Type.HTTP)
                                    .scheme("bearer")
                                    .bearerFormat("JWT"));

            mapping.getHandlerMethods().forEach((info, handlerMethod) -> {
                java.lang.reflect.Method method = handlerMethod.getMethod();
                String pathStr = info.getPatternValues().stream().findFirst().orElse("/");
                Set<RequestMethod> httpMethods = info.getMethodsCondition().getMethods();

                Operation operation = new Operation();

                // read @Operation
                io.swagger.v3.oas.annotations.Operation opAnnotation =
                        method.getAnnotation(io.swagger.v3.oas.annotations.Operation.class);
                if (opAnnotation != null) {
                    operation.setSummary(opAnnotation.summary());
                }

                // read @Tag on class
                io.swagger.v3.oas.annotations.tags.Tag tag =
                        handlerMethod.getBeanType()
                                .getAnnotation(io.swagger.v3.oas.annotations.tags.Tag.class);
                if (tag != null) {
                    operation.addTagsItem(tag.name());
                }

                // scan method parameters for Spring @RequestBody
                for (java.lang.reflect.Parameter param : method.getParameters()) {
                    if (param.isAnnotationPresent(
                            org.springframework.web.bind.annotation.RequestBody.class)) {

                        Class<?> bodyType = param.getType();

                        ResolvedSchema resolvedSchema = ModelConverters.getInstance()
                                .resolveAsResolvedSchema(new AnnotatedType(bodyType));

                        if (resolvedSchema != null && resolvedSchema.schema != null) {
                            components.addSchemas(bodyType.getSimpleName(), resolvedSchema.schema);
                            if (resolvedSchema.referencedSchemas != null) {
                                resolvedSchema.referencedSchemas.forEach(components::addSchemas);
                            }

                            Schema<?> schemaRef = new Schema<>()
                                    .$ref("#/components/schemas/" + bodyType.getSimpleName());

                            io.swagger.v3.oas.models.media.MediaType mediaType =
                                    new io.swagger.v3.oas.models.media.MediaType()
                                            .schema(schemaRef);

                            io.swagger.v3.oas.models.parameters.RequestBody reqBody =
                                    new io.swagger.v3.oas.models.parameters.RequestBody()
                                            .required(true)
                                            .content(new io.swagger.v3.oas.models.media.Content()
                                                    .addMediaType("application/json", mediaType));

                            operation.setRequestBody(reqBody);
                        }
                    }

                    // scan for @PathVariable and @RequestParam
                    if (param.isAnnotationPresent(
                            org.springframework.web.bind.annotation.PathVariable.class)) {
                        org.springframework.web.bind.annotation.PathVariable pv =
                                param.getAnnotation(
                                        org.springframework.web.bind.annotation.PathVariable.class);
                        io.swagger.v3.oas.models.parameters.PathParameter pathParam =
                                (io.swagger.v3.oas.models.parameters.PathParameter) new io.swagger.v3.oas.models.parameters.PathParameter()
                                        .name(pv.value().isEmpty() ? param.getName() : pv.value())
                                        .required(true)
                                        .schema(new Schema<>().type("string"));
                        operation.addParametersItem(pathParam);
                    }

                    if (param.isAnnotationPresent(
                            org.springframework.web.bind.annotation.RequestParam.class)) {
                        org.springframework.web.bind.annotation.RequestParam rp =
                                param.getAnnotation(
                                        org.springframework.web.bind.annotation.RequestParam.class);
                        io.swagger.v3.oas.models.parameters.QueryParameter queryParam =
                                (io.swagger.v3.oas.models.parameters.QueryParameter) new io.swagger.v3.oas.models.parameters.QueryParameter()
                                        .name(rp.value().isEmpty() ? param.getName() : rp.value())
                                        .required(rp.required())
                                        .schema(new Schema<>().type("string"));
                        operation.addParametersItem(queryParam);
                    }
                }

                operation.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));

                PathItem pathItem = paths.getOrDefault(pathStr, new PathItem());
                if (httpMethods.contains(RequestMethod.GET)) pathItem.setGet(operation);
                else if (httpMethods.contains(RequestMethod.POST)) pathItem.setPost(operation);
                else if (httpMethods.contains(RequestMethod.PUT)) pathItem.setPut(operation);
                else if (httpMethods.contains(RequestMethod.DELETE)) pathItem.setDelete(operation);
                else if (httpMethods.contains(RequestMethod.PATCH)) pathItem.setPatch(operation);

                paths.addPathItem(pathStr, pathItem);
            });

            OpenAPI openAPI = new OpenAPI()
                    .info(new Info()
                            .title("OkaneTransfer API")
                            .description("Money Transfer Platform — REST API Documentation")
                            .version("1.0.0"))
                    .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                    .components(components)
                    .paths(paths);

            resp.getWriter().write(Json.mapper().writeValueAsString(openAPI));

        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

}