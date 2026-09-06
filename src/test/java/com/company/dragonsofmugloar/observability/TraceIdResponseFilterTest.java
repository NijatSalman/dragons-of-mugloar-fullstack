package com.company.dragonsofmugloar.observability;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class TraceIdResponseFilterTest {

    private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";

    private final TraceIdResponseFilter filter = new TraceIdResponseFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void doFilterAddsTheTraceIdHeaderWhenTracingIsActive() throws ServletException, IOException {
        MDC.put("traceId", TRACE_ID);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(new MockHttpServletRequest("GET", "/api/v1/games/ggLmesXI"), response, new MockFilterChain());

        assertThat(response.getHeader(TraceIdResponseFilter.HEADER)).isEqualTo(TRACE_ID);
    }

    @Test
    void doFilterAddsNoHeaderWhenThereIsNoTrace() throws ServletException, IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(new MockHttpServletRequest("GET", "/actuator/health"), response, new MockFilterChain());

        assertThat(response.getHeader(TraceIdResponseFilter.HEADER)).isNull();
    }
}
