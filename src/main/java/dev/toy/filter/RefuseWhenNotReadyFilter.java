package dev.toy.filter;

import org.springframework.boot.availability.ReadinessState;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

@Component
public class RefuseWhenNotReadyFilter extends OncePerRequestFilter {
    private final ApplicationAvailability availability;
    public RefuseWhenNotReadyFilter(ApplicationAvailability availability) { this.availability = availability; }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String uri = req.getRequestURI();
        // actuator는 계속 접근 가능해야 하므로 제외
        if (!uri.startsWith("/actuator")
                && availability.getReadinessState() == ReadinessState.REFUSING_TRAFFIC) {
            res.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE); // 503
            res.getWriter().write("Service is draining. Try again later.");
            return;
        }
        chain.doFilter(req, res);
    }
}
