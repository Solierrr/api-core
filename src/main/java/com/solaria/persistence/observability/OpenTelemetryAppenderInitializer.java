package com.solaria.persistence.observability;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;


/**
 * Liga o appender {@code OTEL} do {@code logback-spring.xml} ao OpenTelemetry da aplicação
 *
 * <p>
 * O Spring Boot não instala o appender sozinho -> sem esta classe os logs ficam só no console
 * </p>
 *
 * <p>
 * Sem bean {@link OpenTelemetry}, não faz nada e os logs seguem o comportamento padrão do Logback
 * </p>
 */

@Component
public class OpenTelemetryAppenderInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final ObjectProvider<OpenTelemetry> openTelemetryProvider;

    public OpenTelemetryAppenderInitializer(ObjectProvider<OpenTelemetry> openTelemetryProvider) {
        this.openTelemetryProvider = openTelemetryProvider;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        OpenTelemetry openTelemetry = openTelemetryProvider.getIfAvailable();
        if (openTelemetry != null) {
            OpenTelemetryAppender.install(openTelemetry);
        }
    }
}
