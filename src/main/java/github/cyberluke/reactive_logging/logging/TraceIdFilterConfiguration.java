package github.cyberluke.reactive_logging.logging;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import static github.cyberluke.reactive_logging.logging.LoggingUtil.CONTEXT_KEY;
import static github.cyberluke.reactive_logging.logging.LoggingUtil.logOnEach;

@Slf4j
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@AllArgsConstructor
/**
 * context is updated and sent to the filter chain
 */
public class TraceIdFilterConfiguration implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestId = LoggingUtil.getTraceId(request.getHeaders());
        return chain
            .filter(exchange)
            .doOnEach(
                logOnEach(r -> log.info(">> {} {}", request.getMethod(), request.getURI()))
            )
            .contextWrite(Context.of(CONTEXT_KEY, requestId))
            .doFinally((r) -> {
                MDC.clear();
            });
    }

}
