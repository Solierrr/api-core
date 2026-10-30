package com.solaria.persistence.observability;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.http.server.observation.ServerRequestObservationContext;

import io.micrometer.observation.ObservationPredicate;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.DispatcherType;

/**
 * Configuração central de observabilidade da aplicação
 *
 * <p>
 * Concentra na camada de infra os beans que ajustam o comportamento de Micrometer, Micrometer Tracing
 * e OpenTelemetry -> observabilidade não se mistura com a camada de negócio
 * </p>
 *
 * <p>Beans disponibilizados:</p>
 * <ul>
 *     <li>{@link #traceIdResponseFilterRegistration(ObjectProvider)} -> registra o
 *     {@link TraceIdResponseFilter} logo após o filtro de observação do Spring MVC</li>
 *     <li>{@link #noActuatorObservations()} -> descarta observações (spans/métricas)
 *     do tráfego de probe em {@code /actuator/**}, reduzindo ruído e custo</li>
 *     <li>{@link #contextPropagatingTaskDecorator()} -> preserva o contexto de
 *     observabilidade em trocas de thread (async), aplicado pelo Spring Boot ao executor padrão</li>
 * </ul>
 */
@Configuration
public class ObservabilityConfig {

    /**
     * Registra o filtro responsável por adicionar o traceId
     * na resposta HTTP -> {@link TraceIdResponseFilter}
     *
     * <p> 
     * permite que o cliente receba o traceId e possa utilizá-lo para localizar a requests
     * </p>
     *
     * <p>funciona para requests normais, assíncronas e relacionadas a erros</p>
     * 
     * @param tracerProvider fornece o tracer quando o tracing está habilitado
     * @return configuração do filtro de traceId
     */
    @Bean
    public FilterRegistrationBean<TraceIdResponseFilter> traceIdResponseFilterRegistration(
            ObjectProvider<Tracer> tracerProvider) {
        FilterRegistrationBean<TraceIdResponseFilter> registration =
                new FilterRegistrationBean<>(new TraceIdResponseFilter(tracerProvider));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
        registration.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR);
        registration.addUrlPatterns("/*");
        return registration;
    }

    /**
     * Evita gerar métricas e traces para requisições do Actuator
     *
     * <p>Endpoints como {@code /actuator/health} são chamados
     * frequentemente pelo K8s para verificar se a aplicação
     * está funcionando, essas chamadas não são importantes
     * para acompanhar o comportamento da aplicação, elas são ignoradas</p>
     *
     * @return regra que decide quais requisições devem gerar observabilidade
     */
    @Bean
    public ObservationPredicate noActuatorObservations() {
        return (name, context) -> {
            if (context instanceof ServerRequestObservationContext serverContext) {
                String uri = serverContext.getCarrier().getRequestURI();
                return uri == null || !uri.startsWith("/actuator");
            }
            return true;
        };
    }

    /**
     * Mantém o contexto de observabilidade quando uma tarefa
     * é executada em outra thread.
     *
     * <p>importante para async pois permite que o traceId e outras informações de contexto
     * continuem disponíveis mesmo após a troca de thread.</p>
     *
     * @return decorator responsável por propagar o contexto
     */
    @Bean
    @ConditionalOnClass(name = "io.micrometer.context.ContextSnapshot")
    public TaskDecorator contextPropagatingTaskDecorator() {
        return new ContextPropagatingTaskDecorator();
    }
}
