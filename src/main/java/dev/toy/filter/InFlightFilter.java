package dev.toy.filter;

// 진행 중 HTTP 요청 수 계수용 필터

// Java & Jakarta
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

// Spring Core / Stereotypes
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// Spring Actuator
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

// Spring Scheduling / Async
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import jakarta.servlet.http.HttpServletRequest;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class InFlightFilter implements Filter {
    private final AtomicInteger inFlight;

    public InFlightFilter(AtomicInteger inFlight) { this.inFlight = inFlight; }

    // 간단히 여기서 카운터 빈을 등록(분리해도 무방)
    @Bean
    static AtomicInteger inFlightCounter() { return new AtomicInteger(0); }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        // 헬스/드레인/토글 같은 관측용 경로는 집계에서 제외
        String uri = (req instanceof HttpServletRequest r) ? r.getRequestURI() : "";
        if (uri.startsWith("/actuator") || uri.startsWith("/internal")) {
            chain.doFilter(req, res);
            return;
        }

        inFlight.incrementAndGet();
        try {
            chain.doFilter(req, res);
        } finally {
            inFlight.decrementAndGet();
        }
    }
}


// @Async 풀 (쓰는 경우)
@Configuration @EnableAsync
class AsyncConf {
    @Bean("appExecutor")
    ThreadPoolTaskExecutor appExecutor(){
        var ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(8); ex.setMaxPoolSize(16); ex.setQueueCapacity(200);
        ex.setThreadNamePrefix("app-"); ex.initialize(); return ex;
    }
}

// /actuator/drain
@Component
@Endpoint(id = "drain")
class DrainEndpoint {
    private final AtomicInteger inFlight;
    private final ThreadPoolTaskExecutor ex; // @Async 안 쓰면 주입 제거
    DrainEndpoint(AtomicInteger inFlight, @Qualifier("appExecutor") ThreadPoolTaskExecutor ex){
        this.inFlight = inFlight; this.ex = ex;
    }
    @ReadOperation
    public Map<String,Object> status(){
        int http = inFlight.get();
        int async = ex.getActiveCount();
        return Map.of("httpInFlight", http, "asyncActive", async, "drained", http==0 && async==0);
    }
}
