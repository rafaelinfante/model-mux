package net.rafaelinfante.modelmux.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import net.rafaelinfante.modelmux.config.ModelMuxProperties;
import net.rafaelinfante.modelmux.web.ClientIdResolver;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Per-client rate limit on the API surface, backed by a Resilience4j limiter per client id. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter extends OncePerRequestFilter {

    private final boolean enabled;
    private final RateLimiterRegistry registry;
    private final RateLimiterConfig config;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(
            ModelMuxProperties properties, RateLimiterRegistry registry, ObjectMapper objectMapper) {
        ModelMuxProperties.RateLimit rateLimit = properties.rateLimit();
        this.enabled = rateLimit.enabled();
        this.registry = registry;
        this.objectMapper = objectMapper;
        this.config =
                RateLimiterConfig.custom()
                        .limitForPeriod(rateLimit.requestsPerMinute())
                        .limitRefreshPeriod(Duration.ofMinutes(1))
                        .timeoutDuration(Duration.ZERO)
                        .build();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !enabled || !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String clientId = ClientIdResolver.resolve(request);
        RateLimiter limiter = registry.rateLimiter("client:" + clientId, config);
        if (limiter.acquirePermission()) {
            chain.doFilter(request, response);
        } else {
            writeTooManyRequests(response, clientId);
        }
    }

    private void writeTooManyRequests(HttpServletResponse response, String clientId) throws IOException {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded for client '" + clientId + "'");
        problem.setTitle("Too Many Requests");
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
