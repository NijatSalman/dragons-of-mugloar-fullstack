package com.company.dragonsofmugloar.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Puts the current traceId into an {@code X-Trace-Id} response header, so a user-visible error can be matched
 * to its log lines even when the body is not a ProblemDetail. Micrometer Tracing fills the MDC before this runs.
 */
@Component
public class TraceIdResponseFilter extends OncePerRequestFilter {

    static final String HEADER = "X-Trace-Id";
    private static final String MDC_KEY = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String traceId = MDC.get(MDC_KEY);
        if (traceId != null) {
            response.setHeader(HEADER, traceId);
        }
        chain.doFilter(request, response);
    }
}
