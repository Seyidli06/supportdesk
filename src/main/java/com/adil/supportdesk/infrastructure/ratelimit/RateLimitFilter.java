package com.adil.supportdesk.infrastructure.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class RateLimitFilter
        extends OncePerRequestFilter {

    public static final String LIMIT_HEADER =
            "X-RateLimit-Limit";

    public static final String REMAINING_HEADER =
            "X-RateLimit-Remaining";

    private final RateLimitBucketRegistry bucketRegistry;

    private final RateLimitClientKeyResolver
            clientKeyResolver;

    private final RateLimitPolicyResolver
            policyResolver;

    private final RateLimitProperties properties;

    private final ObjectMapper objectMapper;

    public RateLimitFilter(
            RateLimitBucketRegistry bucketRegistry,
            RateLimitClientKeyResolver clientKeyResolver,
            RateLimitPolicyResolver policyResolver,
            RateLimitProperties properties,
            ObjectMapper objectMapper
    ) {
        this.bucketRegistry = Objects.requireNonNull(
                bucketRegistry,
                "RateLimitBucketRegistry cannot be null"
        );

        this.clientKeyResolver =
                Objects.requireNonNull(
                        clientKeyResolver,
                        "RateLimitClientKeyResolver cannot be null"
                );

        this.policyResolver = Objects.requireNonNull(
                policyResolver,
                "RateLimitPolicyResolver cannot be null"
        );

        this.properties = Objects.requireNonNull(
                properties,
                "RateLimitProperties cannot be null"
        );

        this.objectMapper = Objects.requireNonNull(
                objectMapper,
                "ObjectMapper cannot be null"
        );
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {
        if (!properties.enabled()) {
            return true;
        }

        if (HttpMethod.OPTIONS
                .name()
                .equalsIgnoreCase(
                        request.getMethod()
                )) {

            return true;
        }

        String path = resolveRequestPath(request);

        return !path.equals("/api")
                && !path.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        RateLimitPolicy policy =
                policyResolver.resolve(request);

        String clientKey =
                clientKeyResolver.resolve(request);

        RateLimitDecision decision =
                bucketRegistry.consume(
                        policy,
                        clientKey
                );

        response.setHeader(
                LIMIT_HEADER,
                Long.toString(
                        decision.limit()
                )
        );

        response.setHeader(
                REMAINING_HEADER,
                Long.toString(
                        decision.remainingTokens()
                )
        );

        if (decision.allowed()) {
            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        writeRejectedResponse(
                request,
                response,
                decision
        );
    }

    private void writeRejectedResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            RateLimitDecision decision
    ) throws IOException {

        response.resetBuffer();

        response.setStatus(
                HttpStatus.TOO_MANY_REQUESTS.value()
        );

        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        response.setContentType(
                MediaType.APPLICATION_PROBLEM_JSON_VALUE
        );

        response.setHeader(
                LIMIT_HEADER,
                Long.toString(
                        decision.limit()
                )
        );

        response.setHeader(
                REMAINING_HEADER,
                Long.toString(
                        decision.remainingTokens()
                )
        );

        response.setHeader(
                HttpHeaders.RETRY_AFTER,
                Long.toString(
                        decision.retryAfterSeconds()
                )
        );

        response.setHeader(
                HttpHeaders.CACHE_CONTROL,
                "no-store"
        );

        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put(
                "type",
                "about:blank"
        );

        body.put(
                "title",
                "Too Many Requests"
        );

        body.put(
                "status",
                HttpStatus.TOO_MANY_REQUESTS.value()
        );

        body.put(
                "detail",
                "Request limit exceeded. "
                        + "Please try again later."
        );

        body.put(
                "instance",
                request.getRequestURI()
        );

        body.put(
                "code",
                "rate-limit-exceeded"
        );

        objectMapper.writeValue(
                response.getOutputStream(),
                body
        );

        response.flushBuffer();
    }

    private String resolveRequestPath(
            HttpServletRequest request
    ) {
        String requestUri =
                request.getRequestURI();

        if (requestUri == null
                || requestUri.isBlank()) {

            return "/";
        }

        String contextPath =
                request.getContextPath();

        if (contextPath != null
                && !contextPath.isBlank()
                && requestUri.startsWith(contextPath)) {

            return requestUri.substring(
                    contextPath.length()
            );
        }

        return requestUri;
    }
}