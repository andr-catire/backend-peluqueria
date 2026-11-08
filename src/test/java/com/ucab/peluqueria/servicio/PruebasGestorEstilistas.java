package com.ucab.peluqueria.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ucab.peluqueria.excepcion.ExcepcionRecursoNoEncontrado;
import com.ucab.peluqueria.excepcion.ExcepcionReglaNegocio;
import com.ucab.peluqueria.modelo.Estilista;
import com.ucab.peluqueria.modelo.Rol;
import com.ucab.peluqueria.modelo.Servicio;
import com.ucab.peluqueria.repositorio.RepositorioEstilista;
import com.ucab.peluqueria.repositorio.RepositorioServicio;
import com.ucab.peluqueria.repositorio.RepositorioUsuario;
import com.ucab.peluqueria.transferencia.EstilistaRequest;
import com.ucab.peluqueria.transferencia.EstilistaRespuesta;

/** Pruebas de las reglas de negocio de alta, listado y desactivación de estilistas. */
class PruebasGestorEstilistas {

    private RepositorioUsuario repositorioUsuario;
    private RepositorioEstilista repositorioEstilista;
    private RepositorioServicio repositorioServicio;
    private PasswordEncoder passwordEncoder;
    private GestorEstilistas gestor;

    @BeforeEach
    void preparar() {
        repositorioUsuario = mock(RepositorioUsuario.class);
        repositorioEstilista = mock(RepositorioEstilista.class);
        repositorioServicio = mock(RepositorioServicio.class);
        passwordEncoder = mock(PasswordEncoder.class);
        gestor = new GestorEstilistas(
                repositorioUsuario, repositorioEstilista, repositorioServicio, passwordEncoder);
    }

    private static Servicio servicio(Long id, String nombre, boolean activo) {
        Servicio servicio = new Servicio();
        servicio.setId(id);
        servicio.setNombre(nombre);
        servicio.setCosto(new BigDecimal("20.00"));
        servicio.setDuracionMinutos(30);
        servicio.setActivo(activo);
        return servicio;
    }

    private static Estilista estilista(Long id, String nombre, boolean activo, Servicio... servicios) {
        Estilista estilista = new Estilista();
        estilista.setId(id);
        estilista.setNombre(nombre);
        estilista.setEmail(nombre.toLowerCase() + "@correo.com");
        estilista.setRol(Rol.PERSONAL);
        estilista.setActivo(activo);
        estilista.setServicios(Set.of(servicios));
        return estilista;
    }

    @Test
    void creaUnEstilistaActivoConLaContrasenaCifrada() {
        when(repositorioUsuario.existsByEmailIgnoreCase("luis@correo.com")).thenReturn(false);
        when(repositorioServicio.findAllById(Set.of(1L)))
                .thenReturn(List.of(servicio(1L, "Corte", true)));
        when(passwordEncoder.encode("secreta123")).thenReturn("hash-bcrypt");
        when(repositorioEstilista.save(any(Estilista.class))).thenAnswer(invocacion -> {
            Estilista guardado = invocacion.getArgument(0);
            guardado.setId(10L);
            return guardado;
        });

        EstilistaRespuesta respuesta = gestor.crear(
                new EstilistaRequest(" Luis ", "Luis@Correo.com ", "secreta123", Set.of(1L)),
                "admin@correo.com");

        ArgumentCaptor<Estilista> captor = ArgumentCaptor.forClass(Estilista.class);
        verify(repositorioEstilista).save(captor.capture());
        Estilista guardado = captor.getValue();

        assertEquals("Luis", guardado.getNombre());
        assertEquals("luis@correo.com", guardado.getEmail());
        assertEquals("hash-bcrypt", guardado.getPassword());
        assertEquals(Rol.PERSONAL, guardado.getRol());
        assertTrue(guardado.isActivo());
        assertEquals(1, guardado.getServicios().size());
        assertEquals(10L, respuesta.id());
        assertEquals(Rol.PERSONAL, respuesta.rol());
    }

    @Test
    void rechazaUnCorreoYaRegistrado() {
        when(repositorioUsuario.existsByEmailIgnoreCase("luis@correo.com")).thenReturn(true);

        ExcepcionReglaNegocio excepcion = assertThrows(ExcepcionReglaNegocio.class,
                () -> gestor.crear(
                        new EstilistaRequest("Luis", "luis@correo.com", "secreta123", Set.of(1L)),
                        "admin@correo.com"));

        assertEquals("EMAIL_TAKEN", excepcion.getCodigo());
        assertEquals(HttpStatus.CONFLICT, excepcion.getStatus());
        verify(repositorioEstilista, never()).save(any());
    }

    @Test
    void rechazaUnServicioInexistente() {
        when(repositorioUsuario.existsByEmailIgnoreCase("luis@correo.com")).thenReturn(false);
        when(repositorioServicio.findAllById(Set.of(1L, 99L)))
                .thenReturn(List.of(servicio(1L, "Corte", true)));

        ExcepcionRecursoNoEncontrado excepcion = assertThrows(ExcepcionRecursoNoEncontrado.class,
                () -> gestor.crear(
                        new EstilistaRequest("Luis", "luis@correo.com", "secreta123", Set.of(1L, 99L)),
                        "admin@correo.com"));

        assertTrue(excepcion.getMessage().contains("99"));
    }

    @Test
    void rechazaUnServicioInactivo() {
        when(repositorioUsuario.existsByEmailIgnoreCase("luis@correo.com")).thenReturn(false);
        when(repositorioServicio.findAllById(Set.of(1L)))
                .thenReturn(List.of(servicio(1L, "Corte", false)));

        ExcepcionReglaNegocio excepcion = assertThrows(ExcepcionReglaNegocio.class,
                () -> gestor.crear(
                        new EstilistaRequest("Luis", "luis@correo.com", "secreta123", Set.of(1L)),
                        "admin@correo.com"));

        assertEquals("INACTIVE_SERVICE", excepcion.getCodigo());
    }

    @Test
    void noDesactivaSiTieneCitasFuturas() {
        Estilista estilista = estilista(5L, "marta", true, servicio(1L, "Corte", true));
        when(repositorioEstilista.findById(5L)).thenReturn(Optional.of(estilista));
        when(repositorioEstilista.tieneCitasFuturasActivas(eq(5L), any(LocalDate.class)))
                .thenReturn(true);

        ExcepcionReglaNegocio excepcion = assertThrows(ExcepcionReglaNegocio.class,
                () -> gestor.desvincularEstilista(5L, "admin@correo.com"));

        assertEquals("PENDING_APPOINTMENTS", excepcion.getCodigo());
        assertTrue(estilista.isActivo());
        verify(repositorioEstilista, never()).save(any());
    }

    @Test
    void desactivaSinCambiarElRolNiEliminarServicios() {
        Estilista estilista = estilista(5L, "marta", true, servicio(1L, "Corte", true));
        when(repositorioEstilista.findById(5L)).thenReturn(Optional.of(estilista));
        when(repositorioEstilista.tieneCitasFuturasActivas(eq(5L), any(LocalDate.class)))
                .thenReturn(false);

        gestor.desvincularEstilista(5L, "admin@correo.com");

        assertFalse(estilista.isActivo());
        assertEquals(Rol.PERSONAL, estilista.getRol());
        assertEquals(1, estilista.getServicios().size());
        verify(repositorioEstilista).save(estilista);
    }

    @Test
    void listaEstilistasActivosConSusServicios() {
        Estilista marta = estilista(5L, "marta", true, servicio(1L, "Corte", true));
        Estilista luis = estilista(6L, "luis", true, servicio(2L, "Tinte", true));
        when(repositorioEstilista.findByActivoTrueOrderByNombreAsc())
                .thenReturn(List.of(luis, marta));

        List<EstilistaRespuesta> respuesta = gestor.listar();

        assertEquals(List.of("luis", "marta"),
                respuesta.stream().map(EstilistaRespuesta::nombre).toList());
        assertEquals(1, respuesta.getFirst().servicios().size());
    }
}
