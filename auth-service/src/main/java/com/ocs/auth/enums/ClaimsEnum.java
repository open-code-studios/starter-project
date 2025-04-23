package com.ocs.auth.enums;

public enum ClaimsEnum {
    ROLE("role");

    private final String key;

    ClaimsEnum(String key) {
        this.key = key;
    }

    public String getValue() {
        return key;
    }

    public static ClaimsEnum fromString(String key) {
        for (ClaimsEnum role : ClaimsEnum.values()) {
            if (role.key.equals(key)) {
                return role;
            }
        }
        throw new IllegalArgumentException("No claim found with name: " + key);
    }
}
