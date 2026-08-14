package com.erpilot.app.security;

import com.erpilot.app.common.exception.SqlValidationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SqlSecurityService {

    private final SqlValidator validator;
    private final List<SecurityRule> rules;

    public SqlSecurityService(SqlValidator validator, List<SecurityRule> rules) {
        this.validator = validator;
        this.rules = rules;
    }

    public String secureQuery(String rawSql, String role) {
        SqlValidationResult validation = validator.validate(rawSql);

        if (!validation.isValid()) {
            throw new SqlValidationException(validation.getErrorMessage());
        }

        for (SecurityRule rule : rules) {
            rule.apply(validation, role);
        }

        return validation.getSql();
    }
}