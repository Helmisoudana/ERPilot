package com.erpilot.app.api;

import com.erpilot.app.security.SqlSecurityService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/security")
public class SecurityTestController {

    private final SqlSecurityService securityService;

    public SecurityTestController(SqlSecurityService securityService) {
        this.securityService = securityService;
    }

    @PostMapping("/validate")
    public Map<String, Object> validate(@RequestBody Map<String, String> body) {
        String sql = body.get("sql");
        String role = body.getOrDefault("role", "default");

        String validatedSql = securityService.secureQuery(sql, role);

        return Map.of("originalSql", sql, "validatedSql", validatedSql);
    }
}