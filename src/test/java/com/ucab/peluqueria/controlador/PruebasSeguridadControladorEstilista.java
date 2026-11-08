package com.ucab.peluqueria.controlador;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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
import com.ucab.peluqueria.modelo.Rol;
import com.ucab.peluqueria.repositorio.RepositorioUsuario;
import com.ucab.peluqueria.seguridad.FiltroAutenticacionJwt;
import com.ucab.peluqueria.seguridad.ServicioJwt;
import com.ucab.peluqueria.seguridad.UsuarioActual;
import com.ucab.peluqueria.servicio.GestorEstilistas;
import com.ucab.peluqueria.transferencia.EstilistaRespuesta;

/** Pruebas de acceso al alta y listado de personal con la seguridad real configurada. */
@WebMvcTest(ControladorEstilista.class)
@Import({
        ConfiguracionSeguridad.class,
        ConfiguracionSeguridadMetodos.class,
        ManejadorGlobalExcepciones.class,
        FiltroAutenticacionJwt.class
})
@TestPropertySource(properties = "app.frontend-url=http://localhost:5173")
class PruebasSeguridadControladorEstilista {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GestorEstilistas gestorEstilistas;

    @MockitoBean
    private UsuarioActual usuarioActual;

    @MockitoBean
    private ServicioJwt servicioJwt;

    @MockitoBean
    private RepositorioUsuario repositorioUsuario;

    @Test
    void sinSesionNoPuedeCrearNiListar() throws Exception {
        mockMvc.perform(post("/api/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Luis","email":"luis@correo.com",
                                 "password":"secreta123","serviciosIds":[1]}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));

        mockMvc.perform(get("/api/personal"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void unClienteNoPuedeCrearNiListar() throws Exception {
        mockMvc.perform(post("/api/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Luis","email":"luis@correo.com",
                                 "password":"secreta123","serviciosIds":[1]}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/personal"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PERSONAL")
    void unEstilistaNoPuedeCrearNiListar() throws Exception {
        mockMvc.perform(post("/api/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Luis","email":"luis@correo.com",
                                 "password":"secreta123","serviciosIds":[1]}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/personal"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void unAdministradorCreaYLista() throws Exception {
        when(usuarioActual.email()).thenReturn("admin@correo.com");
        when(gestorEstilistas.crear(any(), any())).thenReturn(
                new EstilistaRespuesta(10L, "Luis", "luis@correo.com", Rol.PERSONAL, List.of()));
        when(gestorEstilistas.listar()).thenReturn(List.of(
                new EstilistaRespuesta(10L, "Luis", "luis@correo.com", Rol.PERSONAL, List.of())));

        mockMvc.perform(post("/api/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Luis","email":"luis@correo.com",
                                 "password":"secreta123","serviciosIds":[1]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));

        mockMvc.perform(get("/api/personal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Luis"));

        verify(gestorEstilistas).listar();
    }
}
