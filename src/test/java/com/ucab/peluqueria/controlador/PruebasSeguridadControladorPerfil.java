package com.ucab.peluqueria.controlador;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.ucab.peluqueria.excepcion.ManejadorGlobalExcepciones;
import com.ucab.peluqueria.repositorio.RepositorioUsuario;
import com.ucab.peluqueria.seguridad.FiltroAutenticacionJwt;
import com.ucab.peluqueria.seguridad.ServicioJwt;
import com.ucab.peluqueria.seguridad.UsuarioActual;
import com.ucab.peluqueria.servicio.GestorPerfil;
import com.ucab.peluqueria.transferencia.PerfilRespuesta;
import com.ucab.peluqueria.transferencia.SolicitudCambioPassword;
import com.ucab.peluqueria.transferencia.SolicitudPerfil;

/** Pruebas de acceso al perfil con la seguridad real configurada. */
@WebMvcTest(ControladorPerfil.class)
@Import({
        ConfiguracionSeguridad.class,
        ConfiguracionSeguridadMetodos.class,
        ManejadorGlobalExcepciones.class,
        FiltroAutenticacionJwt.class
})
@TestPropertySource(properties = "app.frontend-url=http://localhost:5173")
class PruebasSeguridadControladorPerfil {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GestorPerfil gestorPerfil;

    @MockitoBean
    private UsuarioActual usuarioActual;

    @MockitoBean
    private ServicioJwt servicioJwt;

    @MockitoBean
    private RepositorioUsuario repositorioUsuario;

    @Test
    void sinSesionNoPuedeConsultarNiModificar() throws Exception {
        mockMvc.perform(get("/api/perfil"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));

        mockMvc.perform(put("/api/perfil")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana Pérez","cedula":"V-12345678","telefono":"0414-1234567"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void unClienteConsultaYModificaSuPerfil() throws Exception {
        when(gestorPerfil.obtener()).thenReturn(
                new PerfilRespuesta("Ana Pérez", "ana@correo.com", "V-12345678", "0414-1234567"));
        when(gestorPerfil.actualizar(any(SolicitudPerfil.class))).thenReturn(
                new PerfilRespuesta("Ana Pérez", "ana@correo.com", "V-12345678", "0414-1234567"));

        mockMvc.perform(get("/api/perfil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cedula").value("V-12345678"));

        mockMvc.perform(put("/api/perfil")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana Pérez","cedula":"V-12345678","telefono":"0414-1234567"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/perfil/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"passwordActual":"actual123","passwordNueva":"nuevaClave123"}
                                """))
                .andExpect(status().isNoContent());

        verify(gestorPerfil).cambiarPassword(any(SolicitudCambioPassword.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void unAdministradorNoPuedeConsultarElPerfilDeCliente() throws Exception {
        mockMvc.perform(get("/api/perfil"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PERSONAL")
    void unEstilistaNoPuedeModificarElPerfilDeCliente() throws Exception {
        mockMvc.perform(put("/api/perfil")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana Pérez","cedula":"V-12345678","telefono":"0414-1234567"}
                                """))
                .andExpect(status().isForbidden());
    }
}
