package com.doori.doori_backend.lifestyle.domain;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToHousingTypeConverter implements Converter<String, HousingType> {

    @Override
    public HousingType convert(String source) {
        return HousingType.fromValue(source);
    }
}
