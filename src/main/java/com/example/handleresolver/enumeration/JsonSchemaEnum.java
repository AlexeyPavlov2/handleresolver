package com.example.handleresolver.enumeration;

public enum JsonSchemaEnum {
    UpdateTaskRequestDto("classpath:json-schema/task/UpdateTaskRequest.json");

    private final String jsonSchemaFileName;

    JsonSchemaEnum(String jsonSchemaFileName) {
        this.jsonSchemaFileName = jsonSchemaFileName;
    }

    public String getJsonSchemaFileName() {
        return this.jsonSchemaFileName;
    }
}
