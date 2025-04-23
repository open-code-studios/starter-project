//package com.smart360.auth.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
//import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
//
//@Configuration
//public class RoleHierarchyConfig {
//
//    @Bean
//    public RoleHierarchy roleHierarchy() {
//        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
//        String hierarchy =
//            "ROLE_SUPER_ADMIN > ROLE_ADMIN > ROLE_USER";
//        roleHierarchy.setHierarchy(hierarchy);
//        return roleHierarchy;
//    }
//}