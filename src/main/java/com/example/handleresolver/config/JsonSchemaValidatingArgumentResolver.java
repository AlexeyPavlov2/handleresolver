package com.example.handleresolver.config;

import com.example.handleresolver.annotation.ValidJson;
import com.example.handleresolver.enumeration.JsonSchemaEnum;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;

public class JsonSchemaValidatingArgumentResolver implements HandlerMethodArgumentResolver {

    private final ObjectMapper objectMapper;
    private final ResourcePatternResolver resourcePatternResolver;

    public JsonSchemaValidatingArgumentResolver(ObjectMapper objectMapper,
                                                ResourcePatternResolver resourcePatternResolver) {
        this.objectMapper = objectMapper;
        this.resourcePatternResolver = resourcePatternResolver;
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameterAnnotation(ValidJson.class) != null;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest nativeWebRequest, WebDataBinderFactory binderFactory) throws Exception {
        JsonSchema schema = getJsonSchema(parameter.getParameterType().getSimpleName());
        JsonNode json = objectMapper.readTree(getJsonPayload(nativeWebRequest));

        Set<ValidationMessage> validationResult = schema.validate(json);

        if (validationResult.isEmpty()) {
            // No validation errors, convert JsonNode to method parameter type and return it
            return objectMapper.treeToValue(json, parameter.getParameterType());
        }

        String messages = String.join(",", validationResult.stream().map(ValidationMessage::getMessage)
                .collect(Collectors.toList()));

        // throw exception if validation failed
        throw new RuntimeException(messages);

    }

    private InputStream getJsonPayload(NativeWebRequest nativeWebRequest) throws IOException {
        HttpServletRequest httpServletRequest = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        return httpServletRequest.getInputStream();
    }

    private JsonSchema getJsonSchema(String className) {
        JsonSchemaEnum jsonSchemaEnumName;

        try {
            jsonSchemaEnumName = JsonSchemaEnum.valueOf(className);
        } catch (Exception e) {
            throw new RuntimeException("JsonSchemaEnum item does not exist, class: " + className, e);
        }
        String resourcePath = jsonSchemaEnumName.getJsonSchemaFileName();
        Resource resource = resourcePatternResolver.getResource(resourcePath);
        if (!resource.exists()) {
            throw new RuntimeException("Schema file does not exist, path: " + resourcePath);
        }
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        try {
            InputStream schemaStream = resource.getInputStream();
            return schemaFactory.getSchema(schemaStream);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while loading JSON Schema, path: " + resourcePath, e);
        }
    }
}
