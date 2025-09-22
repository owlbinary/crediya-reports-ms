package com.crediya.sqs.listener.config;

import com.crediya.model.event.LoanApprovedEvent;
import com.crediya.sqs.listener.helper.SQSListener;
import com.crediya.usecase.incrementreportcounter.IncrementReportCounterUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.auth.credentials.ContainerCredentialsProvider;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.auth.credentials.WebIdentityTokenFileCredentialsProvider;
import software.amazon.awssdk.metrics.MetricPublisher;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.Message;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Log4j2
@Configuration
public class SQSConfig {

    private static final String PARAMS_PATH = "params";
    private static final String TIPO_ACTUALIZAR_ESTADO_SOLICITUD = "actualizar_estado_solicitud";
    private static final String APROBADO_STATUS = "APROBADO";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public Function<Message, Mono<Void>> messageProcessor(IncrementReportCounterUseCase incrementUseCase) {
        return message -> {
            try {
                String messageBody = message.body();
                log.info("Procesando mensaje SQS: " + messageBody);
                
                JsonNode rootNode = objectMapper.readTree(messageBody);
                
                LoanApprovedEvent event = parseToLoanApprovedEvent(rootNode);
                
                if (!TIPO_ACTUALIZAR_ESTADO_SOLICITUD.equals(event.getTipo())) {
                    log.info("Mensaje ignorado - no es de tipo actualizar_estado_solicitud");
                    return Mono.empty();
                }
                
                if (event.getParams() == null || !APROBADO_STATUS.equals(event.getStatus())) {
                    log.info("Mensaje ignorado - estado no es APROBADO: " + event.getStatus());
                    return Mono.empty();
                }
                
                log.info("Procesando préstamo aprobado ID: {} con monto: {}", 
                        event.getLoanId(), event.getAmount());
                
                return incrementUseCase.incrementarContador(event)
                        .doOnSuccess(result -> log.info("Contador incrementado exitosamente para préstamo: {}", 
                                event.getLoanId()))
                        .doOnError(error -> log.error("Error incrementando contador: {}", error.getMessage()));
                        
            } catch (JsonProcessingException e) {
                log.error("Error deserializando mensaje JSON: {}", e.getMessage());
                return Mono.empty();
            } catch (Exception e) {
                log.error("Error procesando mensaje SQS: {}", e.getMessage());
                return Mono.empty();
            }
        };
    }
    
    private LoanApprovedEvent parseToLoanApprovedEvent(JsonNode rootNode) {
        List<LoanApprovedEvent.PlanPagoCuota> planPago = parsePlanPago(rootNode);
        BigDecimal monto = getBigDecimalValue(rootNode, PARAMS_PATH, "monto");
        
        if (monto == null && planPago != null && !planPago.isEmpty()) {
            LoanApprovedEvent.PlanPagoCuota primeraCuota = planPago.get(0);
            if (primeraCuota.getSaldoRestante() != null && primeraCuota.getAbonoCapital() != null) {
                monto = primeraCuota.getSaldoRestante().add(primeraCuota.getAbonoCapital());
            }
        }
        
        LoanApprovedEvent.EventParams params = LoanApprovedEvent.EventParams.builder()
                .solicitudId(getTextValue(rootNode, PARAMS_PATH, "solicitudId"))
                .estado(getTextValue(rootNode, PARAMS_PATH, "nuevoEstado"))
                .justificacion(getTextValue(rootNode, PARAMS_PATH, "justificacion"))
                .email(getTextValue(rootNode, PARAMS_PATH, "email"))
                .monto(monto)
                .plazo(getIntegerValue(rootNode, PARAMS_PATH, "plazo"))
                .planPago(planPago)
                .build();
                
        return LoanApprovedEvent.builder()
                .tipo(getTextValue(rootNode, "tipo"))
                .params(params)
                .build();
    }
    
    private List<LoanApprovedEvent.PlanPagoCuota> parsePlanPago(JsonNode rootNode) {
        List<LoanApprovedEvent.PlanPagoCuota> planPago = new ArrayList<>();
        JsonNode planPagoNode = rootNode.path(PARAMS_PATH).path("planPago");
        
        if (planPagoNode.isArray()) {
            for (JsonNode cuotaNode : planPagoNode) {
                LoanApprovedEvent.PlanPagoCuota cuota = LoanApprovedEvent.PlanPagoCuota.builder()
                        .numeroCuota(cuotaNode.path("numero_cuota").asInt())
                        .cuota(getBigDecimalFromNode(cuotaNode.path("cuota")))
                        .abonoCapital(getBigDecimalFromNode(cuotaNode.path("abono_capital")))
                        .interes(getBigDecimalFromNode(cuotaNode.path("interes")))
                        .saldoRestante(getBigDecimalFromNode(cuotaNode.path("saldo_restante")))
                        .build();
                planPago.add(cuota);
            }
        }
        
        return planPago.isEmpty() ? null : planPago;
    }
    
    private String getTextValue(JsonNode rootNode, String... path) {
        JsonNode node = rootNode;
        for (String p : path) {
            node = node.path(p);
        }
        return node.isTextual() ? node.asText() : null;
    }
    
    private BigDecimal getBigDecimalValue(JsonNode rootNode, String... path) {
        JsonNode node = rootNode;
        for (String p : path) {
            node = node.path(p);
        }
        return getBigDecimalFromNode(node);
    }
    
    private BigDecimal getBigDecimalFromNode(JsonNode node) {
        if (node.isTextual()) {
            try {
                return new BigDecimal(node.asText());
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (node.isNumber()) {
            return BigDecimal.valueOf(node.asDouble());
        }
        return null;
    }
    
    private Integer getIntegerValue(JsonNode rootNode, String... path) {
        JsonNode node = rootNode;
        for (String p : path) {
            node = node.path(p);
        }
        return node.isNumber() || node.isTextual() ? node.asInt() : null;
    }
    
    @Bean
    public SQSListener sqsListener(SqsAsyncClient client, SQSProperties properties, Function<Message, Mono<Void>> fn) {
        log.info("Creando SQSListener para AWS región: {} y queueUrl: {}", properties.region(), properties.queueUrl());
        return SQSListener.builder()
                .client(client)
                .properties(properties)
                .processor(fn)
                .build()
                .start();
    }

    @Bean
    public SqsAsyncClient configSqs(SQSProperties properties, MetricPublisher publisher, Environment environment) {
        log.info("Configurando SQS client para AWS región: {}", properties.region());
        
        return SqsAsyncClient.builder()
                .region(Region.of(properties.region()))
                .overrideConfiguration(o -> o.addMetricPublisher(publisher))
                .credentialsProvider(getAwsCredentialsProvider())
                .build();
    }

    private AwsCredentialsProviderChain getAwsCredentialsProvider() {
        log.info("Configurando credenciales AWS");
        return AwsCredentialsProviderChain.builder()
                .addCredentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .addCredentialsProvider(SystemPropertyCredentialsProvider.create())
                .addCredentialsProvider(WebIdentityTokenFileCredentialsProvider.create())
                .addCredentialsProvider(ProfileCredentialsProvider.create())
                .addCredentialsProvider(ContainerCredentialsProvider.builder().build())
                .addCredentialsProvider(InstanceProfileCredentialsProvider.create())
                .build();
    }
}
