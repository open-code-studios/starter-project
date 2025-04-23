package com.ocs.auth.enums;

public enum ResponseKeyEnum {
    USERNAME("username"),
    EMAIL("email"),
    ERROR("error");

    private final String key;

    ResponseKeyEnum(String key) {
        this.key = key;
    }

    public String getValue() {
        return key;
    }

    public static ResponseKeyEnum fromString(String key) {
        for (ResponseKeyEnum role : ResponseKeyEnum.values()) {
            if (role.key.equals(key)) {
                return role;
            }
        }
        throw new IllegalArgumentException("No keys found with name: " + key);
    }
}
