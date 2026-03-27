package com.Petcare.Petcare.Configurations.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RateLimitFilter - Pruebas de Rate Limiting")
class RateLimitFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Authentication authentication;

    @Mock
    private PrintWriter printWriter;

    private RateLimitFilter rateLimitFilter;
    private RateLimitConfig rateLimitConfig;

    @BeforeEach
    void setUp() {
        rateLimitConfig = new RateLimitConfig();
        rateLimitConfig.setRequestsPerMinute(5);
        rateLimitConfig.setBlockDurationMinutes(15);
        rateLimitConfig.setEnabled(true);

        rateLimitFilter = new RateLimitFilter(rateLimitConfig);

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("IP-based: Should allow request when under limit")
    void shouldAllowRequest_WhenUnderLimit() throws Exception {
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getRequestURI()).thenReturn("/api/v1/pets");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);

        for (int i = 0; i < 5; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, times(5)).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    @DisplayName("IP-based: Should block request when limit exceeded")
    void shouldBlockRequest_WhenLimitExceeded() throws Exception {
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");
        when(request.getRequestURI()).thenReturn("/api/v1/pets");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);

        for (int i = 0; i < 5; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(429);
        verify(response).setHeader("Retry-After", String.valueOf(15 * 60));
        verify(filterChain, times(5)).doFilter(request, response);
    }

    @Test
    @DisplayName("IP-based: Should reset counter after one minute")
    void shouldResetCounter_AfterOneMinute() throws Exception {
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        when(request.getRequestURI()).thenReturn("/api/v1/pets");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);

        for (int i = 0; i < 5; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        RateLimitEntry entry = rateLimitFilter.getIpRateLimitMap().get("10.0.0.1");
        assertThat(entry).isNotNull();
        assertThat(entry.getRequestCount()).isEqualTo(5);

        entry.resetWindow();

        assertThat(entry.getRequestCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("User-based: Should track by user when authenticated")
    void shouldTrackByUser_WhenAuthenticated() throws Exception {
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getRequestURI()).thenReturn("/api/v1/pets");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("test@example.com");

        for (int i = 0; i < 5; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, times(5)).doFilter(request, response);
    }

    @Test
    @DisplayName("User-based: Should block user when limit exceeded")
    void shouldBlockUser_WhenLimitExceeded() throws Exception {
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getRequestURI()).thenReturn("/api/v1/bookings");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("user@test.com");

        for (int i = 0; i < 5; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(429);
        verify(filterChain, times(5)).doFilter(request, response);
    }

    @Test
    @DisplayName("Endpoint exclusion: Should not rate limit when endpoint excluded - /api/services")
    void shouldNotRateLimit_WhenEndpointExcluded() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/services/v1");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);

        for (int i = 0; i < 10; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, times(10)).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    @DisplayName("Endpoint exclusion: Should not rate limit when auth endpoint - /api/users/login")
    void shouldNotRateLimit_WhenAuthEndpoint() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/users/login");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);

        for (int i = 0; i < 10; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, times(10)).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    @DisplayName("Endpoint exclusion: Should not rate limit when register endpoint")
    void shouldNotRateLimit_WhenRegisterEndpoint() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/users/register");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);

        for (int i = 0; i < 10; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, times(10)).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    @DisplayName("Blocking logic: Should block for 15 minutes when exceeded")
    void shouldBlockFor15Minutes_WhenExceeded() throws Exception {
        when(request.getRemoteAddr()).thenReturn("172.16.0.1");
        when(request.getRequestURI()).thenReturn("/api/v1/pets");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);

        for (int i = 0; i < 5; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader("Retry-After", String.valueOf(15 * 60));
    }

    @Test
    @DisplayName("Blocking logic: Should return Retry-After header in 429 response")
    void shouldReturnRetryAfterHeader() throws Exception {
        when(request.getRemoteAddr()).thenReturn("192.168.50.1");
        when(request.getRequestURI()).thenReturn("/api/v1/pets");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);

        for (int i = 0; i < 5; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(429);
        verify(response).setHeader("Retry-After", "900");
    }

    @Test
    @DisplayName("IP extraction: Should extract IP from X-Forwarded-For header")
    void shouldExtractIP_FromXForwardedFor() throws Exception {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRequestURI()).thenReturn("/api/v1/pets");
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.195, 70.41.3.18");
        when(response.getWriter()).thenReturn(printWriter);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        assertThat(rateLimitFilter.getIpRateLimitMap().containsKey("203.0.113.195")).isTrue();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("IP extraction: Should fallback to remote addr when no X-Forwarded-For")
    void shouldFallbackToRemoteAddr_WhenNoXForwardedFor() throws Exception {
        when(request.getRemoteAddr()).thenReturn("192.168.1.50");
        when(request.getRequestURI()).thenReturn("/api/v1/pets");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        assertThat(rateLimitFilter.getIpRateLimitMap().containsKey("192.168.1.50")).isTrue();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should bypass rate limiting when disabled in config")
    void shouldBypassRateLimit_WhenDisabledInConfig() throws Exception {
        RateLimitFilter disabledFilter = new RateLimitFilter(rateLimitConfig);
        rateLimitConfig.setEnabled(false);

        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getRequestURI()).thenReturn("/api/v1/pets");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);

        for (int i = 0; i < 10; i++) {
            disabledFilter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, times(10)).doFilter(request, response);
    }

    @Test
    @DisplayName("Should return JSON error message when rate limited")
    void shouldReturnJSONErrorMessage_WhenRateLimited() throws Exception {
        when(request.getRemoteAddr()).thenReturn("10.10.10.1");
        when(request.getRequestURI()).thenReturn("/api/v1/pets");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);

        for (int i = 0; i < 5; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(response).setContentType("application/json");
        verify(printWriter).write("{\"status\":429,\"message\":\"Too many requests. Please try again later.\"}");
    }

    @Test
    @DisplayName("Should exclude swagger endpoints")
    void shouldExcludeSwaggerEndpoints() throws Exception {
        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    @DisplayName("Should exclude actuator endpoints")
    void shouldExcludeActuatorEndpoints() throws Exception {
        when(request.getRequestURI()).thenReturn("/actuator/health");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
