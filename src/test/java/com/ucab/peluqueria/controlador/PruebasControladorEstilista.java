package com.ucab.peluqueria.controlador;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ucab.peluqueria.excepcion.ExcepcionReglaNegocio;
import com.ucab.peluqueria.excepcion.ManejadorGlobalExcepciones;
import com.ucab.peluqueria.modelo.Rol;
import com.ucab.peluqueria.seguridad.UsuarioActual;
import com.ucab.peluqueria.servicio.GestorEstilistas;
import com.ucab.peluqueria.transferencia.EstilistaRespuesta;

/** Pruebas HTTP del alta de estilistas, sin base de datos ni seguridad real. */
class PruebasControladorEstilista {

    private GestorEstilistas gestorEstilistas;
    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        gestorEstilistas = mock(GestorEstilistas.class);
        UsuarioActual usuarioActual = mock(UsuarioActual.class);
        when(usuarioActual.email()).thenReturn("admin@correo.com");
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ControladorEstilista(gestorEstilistas, usuarioActual))
                .setControllerAdvice(new ManejadorGlobalExcepciones())
                .build();
    }

    @Test
    void creaUnEstilistaYResponde201() throws Exception {
        when(gestorEstilistas.crear(any(), any())).thenReturn(
                new EstilistaRespuesta(10L, "Luis", "luis@correo.com", Rol.PERSONAL, List.of()));

        mockMvc.perform(post("/api/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Luis","email":"luis@correo.com",
                                 "password":"secreta123","serviciosIds":[1]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.rol").value("PERSONAL"));
    }

    @ParameterizedTest
    @MethodSource("cuerposInvalidos")
    void rechazaDatosInvalidosCon400(String cuerpo) throws Exception {
        mockMvc.perform(post("/api/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    static Stream<String> cuerposInvalidos() {
        String serviciosExcedidos = IntStream.rangeClosed(1, 51)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(","));

        return Stream.of(
                """
                {"nombre":"Luis","email":"luis@correo.com","password":"corta","serviciosIds":[1]}
                """,
                """
                {"nombre":"Luis","email":"no-es-correo","password":"secreta123","serviciosIds":[1]}
                """,
                """
                {"nombre":"Luis","email":"luis@correo.com","password":"secreta123","serviciosIds":[]}
                """,
                """
                {"nombre":"Luis","email":"luis@correo.com","password":"secreta123",
                 "serviciosIds":[%s]}
                """.formatted(serviciosExcedidos));
    }

    @Test
    void responde409CuandoElCorreoYaExiste() throws Exception {
        when(gestorEstilistas.crear(any(), any())).thenThrow(
                ExcepcionReglaNegocio.conflicto("EMAIL_TAKEN", "Ya existe una cuenta con ese correo."));

        mockMvc.perform(post("/api/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Luis","email":"luis@correo.com",
                                 "password":"secreta123","serviciosIds":[1]}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("EMAIL_TAKEN"));
    }
}
