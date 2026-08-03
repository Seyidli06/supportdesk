package com.adil.supportdesk.infrastructure.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        properties = {
                "security.rate-limit.enabled=true",
                "security.rate-limit.login.capacity=2",
                "security.rate-limit.login.refill-tokens=2",
                "security.rate-limit.login.refill-period=1h"
        }
)
@AutoConfigureMockMvc
class RateLimitSecurityIntegrationTest {

    private static final String CLIENT_IP =
            "203.0.113.10";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RateLimitBucketRegistry bucketRegistry;

    @BeforeEach
    void clearBuckets() {
        bucketRegistry.invalidateAll();
    }

    @Test
    @DisplayName(
            "Security filter chain should return 429 after login limit"
    )
    void securityChainShouldReturn429AfterLoginLimit()
            throws Exception {

        performInvalidLogin()
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        header().string(
                                RateLimitFilter.LIMIT_HEADER,
                                "2"
                        )
                )
                .andExpect(
                        header().string(
                                RateLimitFilter.REMAINING_HEADER,
                                "1"
                        )
                );

        performInvalidLogin()
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        header().string(
                                RateLimitFilter.LIMIT_HEADER,
                                "2"
                        )
                )
                .andExpect(
                        header().string(
                                RateLimitFilter.REMAINING_HEADER,
                                "0"
                        )
                );

        performInvalidLogin()
                .andExpect(
                        status().isTooManyRequests()
                )
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_PROBLEM_JSON
                        )
                )
                .andExpect(
                        header().string(
                                RateLimitFilter.LIMIT_HEADER,
                                "2"
                        )
                )
                .andExpect(
                        header().string(
                                RateLimitFilter.REMAINING_HEADER,
                                "0"
                        )
                )
                .andExpect(
                        header().string(
                                "Retry-After",
                                matchesPattern("[1-9][0-9]*")
                        )
                )
                .andExpect(
                        header().string(
                                "Cache-Control",
                                "no-store"
                        )
                )
                .andExpect(
                        jsonPath("$.title")
                                .value("Too Many Requests")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(429)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "rate-limit-exceeded"
                                )
                )
                .andExpect(
                        jsonPath("$.instance")
                                .value(
                                        "/api/v1/auth/login"
                                )
                );
    }

    private ResultActions performInvalidLogin()
            throws Exception {

        return mockMvc.perform(
                post("/api/v1/auth/login")
                        .with(request -> {
                            request.setRemoteAddr(
                                    CLIENT_IP
                            );

                            return request;
                        })
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                """
                                {
                                  "email": "test@example.com",
                                  "password": ""
                                }
                                """
                        )
        );
    }
}