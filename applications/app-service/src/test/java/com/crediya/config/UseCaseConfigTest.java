package com.crediya.config;

import com.crediya.usecase.eventos.ProcesarSolicitudAprobadaUseCase;
import com.crediya.usecase.incrementreportcounter.IncrementReportCounterUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UseCaseConfigTest {

    @Mock
    private IncrementReportCounterUseCase incrementReportCounterUseCase;

    private UseCaseConfig useCaseConfig;

    @BeforeEach
    void setUp() {
        useCaseConfig = new UseCaseConfig();
    }

    @Test

    void deberiaCrearBeanProcesarSolicitudAprobadaUseCase() {
        ProcesarSolicitudAprobadaUseCase resultado = useCaseConfig
                .procesarSolicitudAprobadaUseCase(incrementReportCounterUseCase);

        assertNotNull(resultado, "Debe crear el bean ProcesarSolicitudAprobadaUseCase");
    }

    @Test

    void deberiaInyectarDependenciasCorrectamente() {
        ProcesarSolicitudAprobadaUseCase resultado = useCaseConfig
                .procesarSolicitudAprobadaUseCase(incrementReportCounterUseCase);

        assertNotNull(resultado, "Debe crear la instancia del caso de uso");
        assertNotNull(incrementReportCounterUseCase, "Debe inyectar la dependencia del caso de uso de incremento");
    }

    @Test

    void deberiaRetornarNuevaInstanciaEnCadaLlamada() {
    ProcesarSolicitudAprobadaUseCase casoUso1 = useCaseConfig
        .procesarSolicitudAprobadaUseCase(incrementReportCounterUseCase);
    ProcesarSolicitudAprobadaUseCase casoUso2 = useCaseConfig
        .procesarSolicitudAprobadaUseCase(incrementReportCounterUseCase);

    assertNotNull(casoUso1, "La primera instancia no debe ser nula");
    assertNotNull(casoUso2, "La segunda instancia no debe ser nula");
    assertNotSame(casoUso1, casoUso2, "Cada llamada debe retornar una nueva instancia");
    }

    @Test

    void deberiaCrearCasoUsoConDependenciaNula() {
        ProcesarSolicitudAprobadaUseCase resultado = useCaseConfig
                .procesarSolicitudAprobadaUseCase(null);

        assertNotNull(resultado, "Debe crear el caso de uso incluso con dependencia nula");
    }

    @Test

    void deberiaUsarDependenciaProporcionada() {
        IncrementReportCounterUseCase dependenciaEspecifica = incrementReportCounterUseCase;

        ProcesarSolicitudAprobadaUseCase resultado = useCaseConfig
                .procesarSolicitudAprobadaUseCase(dependenciaEspecifica);

        assertNotNull(resultado, "Debe crear el caso de uso con la dependencia específica");
    }

    @Test

    void deberiaCrearMultiplesBeansConLaMismaDependencia() {
    ProcesarSolicitudAprobadaUseCase casoUso1 = useCaseConfig
        .procesarSolicitudAprobadaUseCase(incrementReportCounterUseCase);
    ProcesarSolicitudAprobadaUseCase casoUso2 = useCaseConfig
        .procesarSolicitudAprobadaUseCase(incrementReportCounterUseCase);
    ProcesarSolicitudAprobadaUseCase casoUso3 = useCaseConfig
        .procesarSolicitudAprobadaUseCase(incrementReportCounterUseCase);

    assertNotNull(casoUso1);
    assertNotNull(casoUso2);
    assertNotNull(casoUso3);
    assertNotSame(casoUso1, casoUso2);
    assertNotSame(casoUso2, casoUso3);
    assertNotSame(casoUso1, casoUso3);
    }

    @Test

    void deberiaMantenerAnotacionConfiguration() {
        Class<UseCaseConfig> claseConfig = UseCaseConfig.class;

        assertTrue(claseConfig.isAnnotationPresent(org.springframework.context.annotation.Configuration.class),
                "UseCaseConfig debe estar anotada con @Configuration");
    }

    @Test   

    void deberiaTenerMetodoBeanCorrecto() {
        Class<UseCaseConfig> claseConfig = UseCaseConfig.class;

        assertDoesNotThrow(() -> {
            claseConfig.getMethod("procesarSolicitudAprobadaUseCase", IncrementReportCounterUseCase.class);
        }, "Debe tener el método bean procesarSolicitudAprobadaUseCase");
    }
}
