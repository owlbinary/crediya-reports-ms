package com.crediya.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class JacksonConfigTest {

    private JacksonConfig jacksonConfig;

    @BeforeEach
    void setUp() {
        jacksonConfig = new JacksonConfig();
    }

    @Test
    void deberiaEstarAnotadaComoConfiguration() {
        Class<JacksonConfig> configClass = JacksonConfig.class;

        assertTrue(configClass.isAnnotationPresent(Configuration.class),
                "JacksonConfig debe estar anotada con @Configuration");
    }

    @Test
    void deberiaCrearObjectMapperBean() {
        ObjectMapper objectMapper = jacksonConfig.jacksonObjectMapper();

        assertNotNull(objectMapper, "El ObjectMapper no debe ser nulo");
        assertInstanceOf(ObjectMapper.class, objectMapper, 
                "Debe retornar una instancia de ObjectMapper");
    }

    @Test
    void deberiaRegistrarJavaTimeModule() {
        ObjectMapper objectMapper = jacksonConfig.jacksonObjectMapper();

        assertTrue(objectMapper.getRegisteredModuleIds().contains("jackson-datatype-jsr310"),
                "Debe registrar el JavaTimeModule para manejo de fechas Java 8+");
    }

    @Test
    void deberiaDeshabilitarWriteDatesAsTimestamps() {
        ObjectMapper objectMapper = jacksonConfig.jacksonObjectMapper();

        assertFalse(objectMapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS),
                "Debe deshabilitar WRITE_DATES_AS_TIMESTAMPS para serializar fechas como ISO strings");
    }

    @Test
    void deberiaSerializarLocalDateCorrectamente() throws Exception {
        ObjectMapper objectMapper = jacksonConfig.jacksonObjectMapper();
        LocalDate fecha = LocalDate.of(2023, 12, 25);

        String json = objectMapper.writeValueAsString(fecha);

        assertEquals("\"2023-12-25\"", json,
                "LocalDate debe serializarse como string ISO sin timestamps");
    }

    @Test
    void deberiaSerializarLocalDateTimeCorrectamente() throws Exception {
        ObjectMapper objectMapper = jacksonConfig.jacksonObjectMapper();
        LocalDateTime fechaHora = LocalDateTime.of(2023, 12, 25, 15, 30, 45);

        String json = objectMapper.writeValueAsString(fechaHora);

        assertEquals("\"2023-12-25T15:30:45\"", json,
                "LocalDateTime debe serializarse como string ISO sin timestamps");
    }

    @Test
    void deberiaDeserializarLocalDateCorrectamente() throws Exception {
        ObjectMapper objectMapper = jacksonConfig.jacksonObjectMapper();
        String json = "\"2023-12-25\"";

        LocalDate fecha = objectMapper.readValue(json, LocalDate.class);

        assertEquals(LocalDate.of(2023, 12, 25), fecha,
                "Debe deserializar string ISO a LocalDate correctamente");
    }

    @Test
    void deberiaDeserializarLocalDateTimeCorrectamente() throws Exception {
        ObjectMapper objectMapper = jacksonConfig.jacksonObjectMapper();
        String json = "\"2023-12-25T15:30:45\"";

        LocalDateTime fechaHora = objectMapper.readValue(json, LocalDateTime.class);

        assertEquals(LocalDateTime.of(2023, 12, 25, 15, 30, 45), fechaHora,
                "Debe deserializar string ISO a LocalDateTime correctamente");
    }

    @Test
    void deberiaRetornarNuevaInstanciaEnCadaLlamada() {
        ObjectMapper mapper1 = jacksonConfig.jacksonObjectMapper();
        ObjectMapper mapper2 = jacksonConfig.jacksonObjectMapper();

        assertNotNull(mapper1, "La primera instancia no debe ser nula");
        assertNotNull(mapper2, "La segunda instancia no debe ser nula");
        assertNotSame(mapper1, mapper2, 
                "Cada llamada debe retornar una nueva instancia de ObjectMapper");
    }

    @Test
    void deberiaTenerMetodoBeanCorrecto() {
        Class<JacksonConfig> configClass = JacksonConfig.class;

        assertDoesNotThrow(() -> {
            configClass.getMethod("jacksonObjectMapper");
        }, "Debe tener el método bean jacksonObjectMapper sin parámetros");
    }

    @Test
    void deberiaPoderCrearInstancia() {
        assertDoesNotThrow(JacksonConfig::new,
                "Debe poder crear una instancia de JacksonConfig sin errores");
    }
}