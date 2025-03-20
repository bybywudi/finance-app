package org.example.model;

import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<String> details
) {
    public ErrorResponse(String code, List<String> details) {
        this(code, "validation failed", details);
    }

    public ErrorResponse(String code, String message) {
        this(code, message, null);
    }
}