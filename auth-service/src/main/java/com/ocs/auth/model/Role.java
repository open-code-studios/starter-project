package com.ocs.auth.model;

public enum Role {
    SUPER_ADMIN("ROLE_SUPER_ADMIN"),
    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER");

    private final String roleName;

    Role(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    public static Role fromString(String roleName) {
        for (Role role : Role.values()) {
            if (role.roleName.equals(roleName)) {
                return role;
            }
        }
        throw new IllegalArgumentException("No role found with name: " + roleName);
    }
} 