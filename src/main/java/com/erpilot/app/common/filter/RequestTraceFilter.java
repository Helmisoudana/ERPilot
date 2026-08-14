package com.erpilot.app.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;


@Component
@Order(1)
public class RequestTraceFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestTraceFilter.class);

    public static final String TRACE_ID_MDC_KEY = "traceId";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String traceId = UUID.randomUUID().toString();
        try {
            MDC.put(TRACE_ID_MDC_KEY, traceId);
            response.setHeader(TRACE_ID_HEADER, traceId);
            filterChain.doFilter(request, response);

        } catch (Throwable t) {
            // Dernier filet : ne devrait normalement pas se déclencher grâce au
            // GlobalExceptionHandler, mais protège contre tout cas non prévu
            // (Error grave, exception hors du cycle Spring MVC classique...).
            log.error("Erreur non interceptée par le GlobalExceptionHandler — traceId={}", traceId, t);

            if (!response.isCommitted()) {
                response.reset();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json;charset=UTF-8");
                String body = "{"
                        + "\"timestamp\":\"" + Instant.now() + "\","
                        + "\"status\":500,"
                        + "\"error\":\"Internal Server Error\","
                        + "\"message\":\"Une erreur inattendue est survenue. L'équipe technique a été notifiée.\","
                        + "\"traceId\":\"" + traceId + "\""
                        + "}";
                response.getWriter().write(body);
                response.getWriter().flush();
            }
        } finally {
            // Toujours nettoyer le MDC, même si une exception non gérée remonte,
            // pour éviter les fuites de contexte entre requêtes (threads réutilisés).
            MDC.remove(TRACE_ID_MDC_KEY);
        }
    }
}
