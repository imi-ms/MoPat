package de.imi.mopat.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.imi.mopat.service.ApiRateLimitService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that enforces rate limiting on the API endpoint {@code POST /encounter/schedule/api}.
 * Applies both a global limit and a per-IP limit using token buckets.
 * Requests exceeding either limit are rejected with HTTP 429.
 */
public class ApiRateLimitFilter extends OncePerRequestFilter {

    private static final String API_PATH = "/encounter/schedule/api";
    private static final String HTTP_METHOD = "POST";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApiRateLimitService apiRateLimitService;

    public ApiRateLimitFilter(ApiRateLimitService apiRateLimitService) {
        this.apiRateLimitService = apiRateLimitService;
    }
    /**
     * Skips filtering for all requests that do not target the rate-limited endpoint.
     *
     * @param request the incoming HTTP request
     * @return {@code true} if the request should not be filtered
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !API_PATH.equals(request.getServletPath())
                || !HTTP_METHOD.equalsIgnoreCase(request.getMethod());
    }
    /**
     * Checks the global and per-IP rate limits. Forwards the request if both limits
     * are satisfied, otherwise responds with HTTP 429.
     *
     * @param request     the incoming HTTP request
     * @param response    the HTTP response
     * @param filterChain the filter chain
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        ConsumptionProbe globalProbe =
                apiRateLimitService.getGlobalBucket().tryConsumeAndReturnRemaining(1);

        if (!globalProbe.isConsumed()) {
            writeTooManyRequestsResponse(response, globalProbe, "Global rate limit exceeded");
            return;
        }

        String clientIp = request.getRemoteAddr();
        Bucket ipBucket = apiRateLimitService.resolveIpBucket(clientIp);
        ConsumptionProbe ipProbe = ipBucket.tryConsumeAndReturnRemaining(1);

        if (!ipProbe.isConsumed()) {
            writeTooManyRequestsResponse(response, ipProbe, "IP rate limit exceeded");
            return;
        }

        response.setHeader("X-Rate-Limit-Global-Remaining",
                String.valueOf(globalProbe.getRemainingTokens()));
        response.setHeader("X-Rate-Limit-IP-Remaining",
                String.valueOf(ipProbe.getRemainingTokens()));

        filterChain.doFilter(request, response);
    }
    /**
     * Writes a {@code 429 Too Many Requests} response including a retry-after header
     * and a JSON error message.
     *
     * @param response the HTTP response
     * @param probe    the consumption probe containing refill timing information
     * @param message  the error message to include in the response body
     */
    private void writeTooManyRequestsResponse(HttpServletResponse response,
                                              ConsumptionProbe probe,
                                              String message) throws IOException {
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(
                "X-Rate-Limit-Retry-After-Seconds",
                String.valueOf(TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()))
        );

        objectMapper.writeValue(response.getWriter(), Map.of(
                "error", message
        ));
    }
}