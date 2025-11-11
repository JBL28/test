package dev.toy.actuator;

import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ActuatorController { // ← 파일명과 동일하게

    private final ApplicationEventPublisher publisher;

    // 진짜 생성자: 클래스명과 동일해야 함
    public ActuatorController(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping("/internal/readiness/off")
    public void off() {
        AvailabilityChangeEvent.publish(publisher, this, ReadinessState.REFUSING_TRAFFIC);
    }

    @PostMapping("/internal/readiness/on")
    public void on() {
        AvailabilityChangeEvent.publish(publisher, this, ReadinessState.ACCEPTING_TRAFFIC);
    }
}
