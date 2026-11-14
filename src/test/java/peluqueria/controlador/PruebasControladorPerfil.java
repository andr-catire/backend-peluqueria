package peluqueria.controlador;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import peluqueria.excepcion.ExcepcionReglaNegocio;
import peluqueria.excepcion.ManejadorGlobalExcepciones;
import peluqueria.servicio.GestorPerfil;
import peluqueria.transferencia.PerfilRespuesta;
import peluqueria.transferencia.SolicitudCambioPassword;
import peluqueria.transferencia.SolicitudPerfil;

/** Pruebas HTTP del perfil del cliente, sin base de datos ni seguridad real. */
class PruebasControladorPerfil {

    private GestorPerfil gestorPerfil;
    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        gestorPerfil = mock(GestorPerfil.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ControladorPerfil(gestorPerfil))
                .setControllerAdvice(new ManejadorGlobalExcepciones())
                .build();
    }

    @Test
    void devuelveElPerfilDelCliente() throws Exception {
        when(gestorPerfil.obtener()).thenReturn(
                new PerfilRespuesta("Ana Pérez", "ana@correo.com", "V-12345678", "0414-1234567"));

        mockMvc.perform(get("/api/perfil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana Pérez"))
                .andExpect(jsonPath("$.email").value("ana@correo.com"))
                .andExpect(jsonPath("$.cedula").value("V-12345678"))
                .andExpect(jsonPath("$.telefono").value("0414-1234567"));
    }

    @Test
    void actualizaLosDatosDelPerfil() throws Exception {
        when(gestorPerfil.actualizar(any(SolicitudPerfil.class))).thenReturn(
                new PerfilRespuesta("Ana Pérez", "ana@correo.com", "V-87654321", "0414-7654321"));

        mockMvc.perform(put("/api/perfil")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana Pérez","cedula":"V-87654321","telefono":"0414-7654321"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cedula").value("V-87654321"))
                .andExpect(jsonPath("$.telefono").value("0414-7654321"));

        verify(gestorPerfil).actualizar(any(SolicitudPerfil.class));
    }

    @Test
    void rechazaDatosInvalidosCon400() throws Exception {
        mockMvc.perform(put("/api/perfil")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana Pérez","cedula":"123","telefono":"abc"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void responde409CuandoLaCedulaYaExiste() throws Exception {
        when(gestorPerfil.actualizar(any(SolicitudPerfil.class))).thenThrow(
                ExcepcionReglaNegocio.conflicto("CEDULA_TAKEN", "Ya existe una cuenta con esa cédula."));

        mockMvc.perform(put("/api/perfil")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana Pérez","cedula":"V-12345678","telefono":"0414-1234567"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CEDULA_TAKEN"));
    }

    @Test
    void cambiaLaContrasenaYResponde204() throws Exception {
        mockMvc.perform(put("/api/perfil/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"passwordActual":"actual123","passwordNueva":"nuevaClave123"}
                                """))
                .andExpect(status().isNoContent());

        verify(gestorPerfil).cambiarPassword(any(SolicitudCambioPassword.class));
    }

    @Test
    void rechazaLaContrasenaActualIncorrectaCon400() throws Exception {
        doThrow(ExcepcionReglaNegocio.invalida(
                        "PASSWORD_INCORRECT", "La contraseña actual no es correcta."))
                .when(gestorPerfil).cambiarPassword(any(SolicitudCambioPassword.class));

        mockMvc.perform(put("/api/perfil/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"passwordActual":"incorrecta","passwordNueva":"nuevaClave123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("PASSWORD_INCORRECT"));
    }

    @Test
    void rechazaUnaContrasenaNuevaCortaCon400() throws Exception {
        mockMvc.perform(put("/api/perfil/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"passwordActual":"actual123","passwordNueva":"corta"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
