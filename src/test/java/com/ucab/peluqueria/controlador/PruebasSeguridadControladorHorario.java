package com.ucab.peluqueria.controlador;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ucab.peluqueria.configuracion.ConfiguracionSeguridad;
import com.ucab.peluqueria.configuracion.ConfiguracionSeguridadMetodos;
import com.ucab.peluqueria.configuracion.PropiedadesHorario;
import com.ucab.peluqueria.excepcion.ManejadorGlobalExcepciones;
import com.ucab.peluqueria.repositorio.RepositorioUsuario;
import com.ucab.peluqueria.seguridad.FiltroAutenticacionJwt;
import com.ucab.peluqueria.seguridad.ServicioJwt;
import com.ucab.peluqueria.seguridad.UsuarioActual;
import com.ucab.peluqueria.servicio.GestorHorario;

/** Pruebas de acceso al horario editable con la seguridad real configurada. */
@WebMvcTest(ControladorHorario.class)
@Import({
        ConfiguracionSeguridad.class,
        ConfiguracionSeguridadMetodos.class,
        ManejadorGlobalExcepciones.class,
        FiltroAutenticacionJwt.class
})
@TestPropertySource(properties = "app.frontend-url=http://localhost:5173")
class PruebasSeguridadControladorHorario {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GestorHorario gestorHorario;

    @MockitoBean
    private UsuarioActual usuarioActual;

    @MockitoBean
    private ServicioJwt servicioJwt;

    @MockitoBean
    private RepositorioUsuario repositorioUsuario;

    private static PropiedadesHorario horario() {
        return new PropiedadesHorario(
                ZoneId.of("America/Caracas"),
                LocalTime.of(8, 0),
                LocalTime.of(18, 0),
                0);
    }

    @Test
    void sinSesionResponde401() throws Exception {
        mockMvc.perform(get("/api/asistencia/horario"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/asistencia/horario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"apertura":"09:00","cierre":"20:00"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void unClienteNoPuedeCambiarElHorario() throws Exception {
        mockMvc.perform(get("/api/asistencia/horario"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PERSONAL")
    void unEmpleadoNoPuedeCambiarElHorario() throws Exception {
        mockMvc.perform(put("/api/asistencia/horario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"apertura":"09:00","cierre":"20:00"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void unAdministradorConsultaYActualizaElHorario() throws Exception {
        when(usuarioActual.email()).thenReturn("admin@correo.com");
        when(gestorHorario.actual()).thenReturn(horario());
        when(gestorHorario.actualizar(any(), any(), any())).thenReturn(horario());

        mockMvc.perform(get("/api/asistencia/horario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apertura").value("08:00:00"));

        mockMvc.perform(put("/api/asistencia/horario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"apertura":"09:00","cierre":"20:00"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cierre").value("18:00:00"));
    }
}
