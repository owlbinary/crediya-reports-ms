package com.crediya.api.controller;

import com.crediya.api.dto.ReportResponseDto;
import com.crediya.api.security.JwtUserPrincipal;
import com.crediya.api.service.AutomaticReportService;
import com.crediya.model.report.Report;
import com.crediya.usecase.getreportcounter.GetReportCounterUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportController - Controlador de Reportes con Seguridad ADMIN")
class ReportControllerTest {

    @Mock
    private GetReportCounterUseCase getReportCounterUseCase;

    @Mock
    private AutomaticReportService automaticReportService;

    private ReportController reportController;

    private JwtUserPrincipal adminPrincipal;
    private Authentication adminAuthentication;

    @BeforeEach
    void setUp() {
        reportController = new ReportController(getReportCounterUseCase, automaticReportService);
        
        adminPrincipal = JwtUserPrincipal.builder()
                .email("admin@crediya.com")
                .idUsuario("1")
                .nombre("Admin")
                .apellido("Sistema")
                .idRol("1")
                .build();

        adminAuthentication = new UsernamePasswordAuthenticationToken(
                adminPrincipal, 
                null, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    @Test
    @DisplayName("Debe obtener reporte de préstamos aprobados exitosamente")
    void debeObtenerReportePrestamosAprobadosExitosamente() {
        Report reportMock = Report.builder()
                .reportType("APPROVED_LOANS")
                .approvedLoansCount(150L)
                .totalApprovedAmount(BigDecimal.valueOf(5000000))
                .lastUpdated(LocalDateTime.now())
                .build();

        when(getReportCounterUseCase.obtenerContadorPrestamosAprobados())
                .thenReturn(Mono.just(reportMock));

        StepVerifier.create(reportController.obtenerReportePrestamosAprobados(adminAuthentication))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getReportType()).isEqualTo("APPROVED_LOANS");
                    assertThat(response.getApprovedLoansCount()).isEqualTo(150L);
                    assertThat(response.getTotalApprovedAmount()).isEqualTo(BigDecimal.valueOf(5000000));
                    assertThat(response.getMessage()).isEqualTo("Cantidad total de préstamos aprobados obtenida exitosamente");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe manejar error en use case correctamente")
    void debeManejarErrorEnUseCaseCorrectamente() {
        when(getReportCounterUseCase.obtenerContadorPrestamosAprobados())
                .thenReturn(Mono.error(new RuntimeException("Error en base de datos")));

        StepVerifier.create(reportController.obtenerReportePrestamosAprobados(adminAuthentication))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe tener anotación @PreAuthorize con ROLE_ADMIN")
    void debeTenerAnotacionPreAuthorizeConRoleAdmin() throws Exception {
        var method = ReportController.class.getMethod("obtenerReportePrestamosAprobados", Authentication.class);
        
        assertThat(method.isAnnotationPresent(org.springframework.security.access.prepost.PreAuthorize.class))
                .isTrue();
        
        var preAuthorize = method.getAnnotation(org.springframework.security.access.prepost.PreAuthorize.class);
        assertThat(preAuthorize.value()).isEqualTo("hasRole('ADMIN')");
    }

    @Test
    @DisplayName("Debe estar anotado como RestController")
    void debeEstarAnotadoComoRestController() {
        assertThat(ReportController.class.isAnnotationPresent(org.springframework.web.bind.annotation.RestController.class))
                .isTrue();
    }

    @Test
    @DisplayName("Debe tener RequestMapping correcto")
    void debeTenerRequestMappingCorrecto() {
        var requestMapping = ReportController.class.getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class);
        
        assertThat(requestMapping).isNotNull();
        assertThat(requestMapping.value()).containsExactly("/api/v1/reportes");
    }

    @Test
    @DisplayName("Debe tener SecurityRequirement configurado")
    void debeTenerSecurityRequirementConfigurado() {
        assertThat(ReportController.class.isAnnotationPresent(io.swagger.v3.oas.annotations.security.SecurityRequirement.class))
                .isTrue();
        
        var securityRequirement = ReportController.class.getAnnotation(io.swagger.v3.oas.annotations.security.SecurityRequirement.class);
        assertThat(securityRequirement.name()).isEqualTo("bearerAuth");
    }

    @Test
    @DisplayName("Debe extraer información del usuario ADMIN correctamente")
    void debeExtraerInformacionDelUsuarioAdminCorrectamente() {
        Report reportMock = Report.builder()
                .reportType("APPROVED_LOANS")
                .approvedLoansCount(100L)
                .totalApprovedAmount(BigDecimal.valueOf(3000000))
                .lastUpdated(LocalDateTime.now())
                .build();

        when(getReportCounterUseCase.obtenerContadorPrestamosAprobados())
                .thenReturn(Mono.just(reportMock));

        StepVerifier.create(reportController.obtenerReportePrestamosAprobados(adminAuthentication))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    JwtUserPrincipal principal = (JwtUserPrincipal) adminAuthentication.getPrincipal();
                    assertThat(principal.getIdRol()).isEqualTo("1");
                    assertThat(principal.getEmail()).contains("admin");
                })
                .verifyComplete();
    }
}
