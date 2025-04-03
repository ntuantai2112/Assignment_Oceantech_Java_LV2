package com.octl2.api.commons.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorMessages implements ErrorMessage {
    SUCCESS(200, "Success"),

    BAD_REQUEST(400, "Bad request"),
    UNSUPPORTED_TYPE(405, "Unsupported response type"),
    INVALID_VALUE(400_001, "Invalid value"),
    INVALID_VALUE_LEVEL_MAPPING(406, "Invalid levelMapping. Allowed values: 1, 2, 3"),
    SAVE_DATABASE_ERROR(400_002, "Save database error"),
    MISSING_REQUIRED_FIELD(400_003, "Missing required field"),
    NOT_FOUND(404, "Resource not found"),
    ;

    private final int code;
    private final String message;
}
