package com.health.mediconnectx.dto;

public class AuthResponse {
    private String token;
    private String message;
    private String name;
    private String role;
    private Long roleId;

    public AuthResponse(String token, String message, String name, String role, Long roleId) {
        this.token   = token;
        this.message = message;
        this.name    = name;
        this.role    = role;
        this.roleId  = roleId;
    }

    public String getToken()   { return token; }
    public void setToken(String token) { this.token = token; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getName()    { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole()    { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getRoleId()    { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
}
