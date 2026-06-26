package com.doori.doori_backend.notification.domain;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToNotificationTypeConverter implements Converter<String, NotificationType> {

    @Override
    public NotificationType convert(String source) {
        return NotificationType.valueOf(source.toUpperCase());
    }
}
