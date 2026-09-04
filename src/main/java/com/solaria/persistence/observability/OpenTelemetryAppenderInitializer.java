package com.solaria.persistence.observability;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;


/**
 * 
 * Prepara o envio dos logs da aplicação para o OpenTelemetry
 * 
 * <p>
 * Caso o OpenTelemetry não esteja disponível a aplicação continua funcionando
 * normalmente e os logs seguem apenas o comportamento padrão do Logback
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
