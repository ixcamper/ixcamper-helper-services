package com.suhas.common.dto;

public record ClientLogDto(
        String level,
        String message,
        String details,
        String url,
        String timestamp
) {}