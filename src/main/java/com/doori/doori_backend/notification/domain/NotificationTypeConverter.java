package com.doori.doori_backend.notification.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class NotificationTypeConverter implements AttributeConverter<NotificationType, String> {

    @Override
    public String convertToDatabaseColumn(NotificationType type) {
        return type == null ? null : type.name();
    }

    @Override
    public NotificationType convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return switch (dbData) {
            case "MATCH", "MATCH_REQUEST" -> NotificationType.MATCH_REQUEST;
            case "COMMUNITY", "COMMUNITY_COMMENT" -> NotificationType.COMMUNITY_COMMENT;
            default -> NotificationType.valueOf(dbData);
        };
    }
}
