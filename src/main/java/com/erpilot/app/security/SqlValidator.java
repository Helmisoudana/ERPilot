package com.erpilot.app.security;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class SqlValidator {

    public SqlValidationResult validate(String sql) {
        if (sql == null || sql.isBlank()) {
            return SqlValidationResult.invalid("La requête SQL ne peut pas être vide.");
        }

        Statement statement;
        try {
            statement = CCJSqlParserUtil.parse(sql);
        } catch (JSQLParserException e) {
            return SqlValidationResult.invalid("SQL syntaxiquement invalide : " + e.getMessage());
        }

        if (statement == null) {
            return SqlValidationResult.invalid("SQL syntaxiquement invalide : requête vide ou non reconnue.");
        }

        if (!(statement instanceof Select)) {
            return SqlValidationResult.invalid(
                    "Seules les requêtes SELECT sont autorisées. Type détecté : "
                            + statement.getClass().getSimpleName());
        }

        List<String> tables = new TablesNamesFinder().getTableList(statement);

        return SqlValidationResult.valid(sql, tables);
    }
}