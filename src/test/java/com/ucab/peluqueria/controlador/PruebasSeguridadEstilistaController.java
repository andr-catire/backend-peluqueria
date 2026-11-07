package com.ucab.peluqueria.controlador;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ucab.peluqueria.configuracion.ConfiguracionSeguridad;
import com.ucab.peluqueria.configuracion.ConfiguracionSeguridadMetodos;
import com.ucab.peluqueria.excepcion.ManejadorGlobalExcepciones;
import com.ucab.peluqueria.modelo.Rol;
import com.ucab.peluqueria.seguridad.UsuarioActual;
import com.ucab.peluqueria.servicio.EstilistaService;
import com.ucab.peluqueria.transferencia.EstilistaRespuesta;
import com.ucab.peluqueria.transferencia.UsuarioBusquedaRespuesta;

/** Pruebas de acceso a los endpoints de personal con la seguridad real configurada. */
@WebMvcTest(EstilistaController.class)
@Import({ConfiguracionSeguridad.class, ConfiguracionSeguridadMetodos.class, ManejadorGlobalExcepciones.class})
@TestPropertySource(properties = "app.frontend-url=http://localhost:5173")
class PruebasSeguridadEstilistaController {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EstilistaService estilistaService;

    @MockitoBean
    private UsuarioActual usuarioActual;

    @Test
    void sinSesionResponde401() throws Exception {
        mockMvc.perform(get("/api/personal/buscar").param("email", "ana@correo.com"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void unClienteNoPuedeBuscarCuentas() throws Exception {
        mockMvc.perform(get("/api/personal/buscar").param("email", "ana@correo.com"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void unAdministradorBuscaPorCorreo() throws Exception {
        when(estilistaService.buscarPorEmail("ana@correo.com"))
                .thenReturn(new UsuarioBusquedaRespuesta(3L, "Ana", "ana@correo.com", Rol.CLIENTE));

        mockMvc.perform(get("/api/personal/buscar").param("email", "ana@correo.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.nombre").value("Ana"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    @Test
    @WithMockUser(roles = "PERSONAL")
    void elPerfilUsaElIdDelContextoDeSeguridadYNoDeLaUrl() throws Exception {
        when(usuarioActual.id()).thenReturn(42L);
        when(estilistaService.obtenerMiPerfil(42L))
                .thenReturn(new EstilistaRespuesta(42L, "Marta", "marta@correo.com", Rol.PERSONAL, List.of()));

        mockMvc.perform(get("/api/personal/mi-perfil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42));

        verify(estilistaService).obtenerMiPerfil(42L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void elAdministradorNoPuedeVerElPerfilDePersonal() throws Exception {
        mockMvc.perform(get("/api/personal/mi-perfil"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PERSONAL")
    void unEstilistaNoPuedeDesvincularAOtro() throws Exception {
        mockMvc.perform(delete("/api/personal/9/desvincular"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void unAdministradorDesvinculaYResponde204() throws Exception {
        mockMvc.perform(delete("/api/personal/9/desvincular"))
                .andExpect(status().isNoContent());

        verify(estilistaService).desvincularEstilista(9L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rechazaLaPromocionSinServicios() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/personal/promover")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {"idUsuario":8,"serviciosIds":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        org.mockito.Mockito.verifyNoInteractions(estilistaService);
    }
}
