package com.crediya.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import static org.junit.jupiter.api.Assertions.*;

class UseCasesConfigTest {

    @Test
    void deberiaEstarAnotadaComoConfiguration() {
        Class<UseCasesConfig> configClass = UseCasesConfig.class;

        assertTrue(configClass.isAnnotationPresent(Configuration.class),
                "UseCasesConfig debe estar anotada con @Configuration");
    }

    @Test
    void deberiaEstarAnotadaConComponentScan() {
        Class<UseCasesConfig> configClass = UseCasesConfig.class;

        assertTrue(configClass.isAnnotationPresent(ComponentScan.class),
                "UseCasesConfig debe estar anotada con @ComponentScan");
    }

    @Test
    void deberiaTenerBasePackagesConfigurado() {
        Class<UseCasesConfig> configClass = UseCasesConfig.class;
        ComponentScan annotation = configClass.getAnnotation(ComponentScan.class);

        assertNotNull(annotation, "Debe tener anotación @ComponentScan");
        String[] basePackages = annotation.basePackages();
        assertEquals(1, basePackages.length, "Debe tener exactamente un base package");
        assertEquals("com.crediya.usecase", basePackages[0], 
                "El base package debe ser 'com.crediya.usecase'");
    }

    @Test
    void deberiaTenerIncludeFiltersConfigurado() {
        Class<UseCasesConfig> configClass = UseCasesConfig.class;
        ComponentScan annotation = configClass.getAnnotation(ComponentScan.class);

        assertNotNull(annotation, "Debe tener anotación @ComponentScan");
        ComponentScan.Filter[] includeFilters = annotation.includeFilters();
        assertEquals(1, includeFilters.length, "Debe tener exactamente un include filter");
        
        ComponentScan.Filter filter = includeFilters[0];
        assertEquals(FilterType.REGEX, filter.type(), "El filtro debe ser de tipo REGEX");
        assertEquals("^.+UseCase$", filter.pattern()[0], 
                "El patrón debe ser '^.+UseCase$'");
    }

    @Test
    void deberiaDeshabilitarDefaultFilters() {
        Class<UseCasesConfig> configClass = UseCasesConfig.class;
        ComponentScan annotation = configClass.getAnnotation(ComponentScan.class);

        assertNotNull(annotation, "Debe tener anotación @ComponentScan");
        assertFalse(annotation.useDefaultFilters(), 
                "useDefaultFilters debe estar configurado como false");
    }

    @Test
    void deberiaPoderCrearInstancia() {
        assertDoesNotThrow(() -> new UseCasesConfig(),
                "Debe poder crear una instancia de UseCasesConfig sin errores");
    }

    @Test
    void deberiaSerClasePublica() {
        Class<UseCasesConfig> configClass = UseCasesConfig.class;

        assertTrue(java.lang.reflect.Modifier.isPublic(configClass.getModifiers()),
                "UseCasesConfig debe ser una clase pública");
    }
}