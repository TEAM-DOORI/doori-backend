package com.doori.doori_backend.lifestyle.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum HousingType {
    DORMITORY("dormitory"),
    SHARE_HOUSE("share_house"),
    ONE_ROOM("one_room"),
    ETC("etc");

    private final String value;

    HousingType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static HousingType fromValue(String value) {
        for (HousingType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown housing type: " + value);
    }
}
