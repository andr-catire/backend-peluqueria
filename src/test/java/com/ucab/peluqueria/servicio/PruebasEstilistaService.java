package com.ucab.peluqueria.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.ucab.peluqueria.excepcion.ExcepcionRecursoNoEncontrado;
import com.ucab.peluqueria.excepcion.ExcepcionReglaNegocio;
import com.ucab.peluqueria.modelo.Rol;
import com.ucab.peluqueria.modelo.Servicio;
import com.ucab.peluqueria.modelo.Usuario;
import com.ucab.peluqueria.repositorio.RepositorioCliente;
import com.ucab.peluqueria.repositorio.RepositorioEstilista;
import com.ucab.peluqueria.repositorio.RepositorioServicio;
import com.ucab.peluqueria.repositorio.RepositorioUsuario;
import com.ucab.peluqueria.transferencia.EstilistaRequest;
import com.ucab.peluqueria.transferencia.EstilistaRespuesta;
import com.ucab.peluqueria.transferencia.UsuarioBusquedaRespuesta;

/** Pruebas de búsqueda, promoción, perfil y desvinculación de estilistas. */
class PruebasEstilistaService {

    private RepositorioUsuario repositorioUsuario;
    private RepositorioCliente repositorioCliente;
    private RepositorioEstilista repositorioEstilista;
    private RepositorioServicio repositorioServicio;
    private EstilistaService servicio;

    @BeforeEach
    void preparar() {
        repositorioUsuario = mock(RepositorioUsuario.class);
        repositorioCliente = mock(RepositorioCliente.class);
        repositorioEstilista = mock(RepositorioEstilista.class);
        repositorioServicio = mock(RepositorioServicio.class);
        servicio = new EstilistaService(
                repositorioUsuario, repositorioCliente, repositorioEstilista, repositorioServicio);
    }

    private static Usuario usuario(Long id, String nombre, String email, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setRol(rol);
        return usuario;
    }

    private static Servicio servicio(Long id, String nombre) {
        Servicio servicio = new Servicio();
        servicio.setId(id);
        servicio.setNombre(nombre);
        servicio.setCosto(new BigDecimal("20.00"));
        servicio.setDuracionMinutos(30);
        return servicio;
    }

    @Test
    void buscaUnaCuentaPorCorreoNormalizandoElTexto() {
        when(repositorioUsuario.findByEmailIgnoreCase("ana@correo.com"))
                .thenReturn(Optional.of(usuario(3L, "Ana", "ana@correo.com", Rol.CLIENTE)));

        UsuarioBusquedaRespuesta respuesta = servicio.buscarPorEmail("  Ana@Correo.com ");

        assertEquals(3L, respuesta.id());
        assertEquals("Ana", respuesta.nombre());
        assertEquals(Rol.CLIENTE, respuesta.rol());
    }

    @Test
    void respondeNoEncontradoSiElCorreoNoExiste() {
        when(repositorioUsuario.findByEmailIgnoreCase("nadie@correo.com"))
                .thenReturn(Optional.empty());

        assertThrows(ExcepcionRecursoNoEncontrado.class,
                () -> servicio.buscarPorEmail("nadie@correo.com"));
    }

    @Test
    void noBuscaCuentasQueNoSeanCliente() {
        when(repositorioUsuario.findByEmailIgnoreCase("marta@correo.com"))
                .thenReturn(Optional.of(usuario(5L, "Marta", "marta@correo.com", Rol.PERSONAL)));

        ExcepcionReglaNegocio excepcion = assertThrows(ExcepcionReglaNegocio.class,
                () -> servicio.buscarPorEmail("marta@correo.com"));

        assertEquals("NOT_PROMOTABLE", excepcion.getCodigo());
    }

    @Test
    void promueveLaCuentaYLeAsignaLosServicios() {
        Usuario cliente = usuario(8L, "Luis", "luis@correo.com", Rol.CLIENTE);
        Servicio corte = servicio(1L, "Corte");
        Servicio tinte = servicio(2L, "Tinte");
        when(repositorioUsuario.findById(8L)).thenReturn(Optional.of(cliente));
        when(repositorioServicio.findAllById(Set.of(1L, 2L))).thenReturn(List.of(corte, tinte));

        EstilistaRespuesta respuesta = servicio.promoverAEmpleado(
                new EstilistaRequest(8L, Set.of(1L, 2L)));

        assertEquals(Rol.PERSONAL, cliente.getRol());
        assertEquals(Rol.PERSONAL, respuesta.rol());
        assertEquals(2, respuesta.servicios().size());
        verify(repositorioUsuario).saveAndFlush(cliente);
        verify(repositorioEstilista).insertarPerfil(8L);
        verify(repositorioEstilista).asignarServicio(8L, 1L);
        verify(repositorioEstilista).asignarServicio(8L, 2L);
    }

    @Test
    void alPromoverEliminaLaFichaDeClienteParaNoDejarDosPerfiles() {
        Usuario cliente = usuario(8L, "Luis", "luis@correo.com", Rol.CLIENTE);
        when(repositorioUsuario.findById(8L)).thenReturn(Optional.of(cliente));
        when(repositorioCliente.existePerfil(8L)).thenReturn(true);
        when(repositorioCliente.tieneCitas(8L)).thenReturn(false);
        when(repositorioServicio.findAllById(Set.of(1L)))
                .thenReturn(List.of(servicio(1L, "Corte")));

        servicio.promoverAEmpleado(new EstilistaRequest(8L, Set.of(1L)));

        verify(repositorioCliente).eliminarPerfil(8L);
        verify(repositorioEstilista).insertarPerfil(8L);
    }

    @Test
    void noPromueveUnClienteQueTieneCitasRegistradas() {
        Usuario cliente = usuario(8L, "Luis", "luis@correo.com", Rol.CLIENTE);
        when(repositorioUsuario.findById(8L)).thenReturn(Optional.of(cliente));
        when(repositorioCliente.existePerfil(8L)).thenReturn(true);
        when(repositorioCliente.tieneCitas(8L)).thenReturn(true);
        when(repositorioServicio.findAllById(Set.of(1L)))
                .thenReturn(List.of(servicio(1L, "Corte")));

        ExcepcionReglaNegocio excepcion = assertThrows(ExcepcionReglaNegocio.class,
                () -> servicio.promoverAEmpleado(new EstilistaRequest(8L, Set.of(1L))));

        assertEquals("CLIENT_WITH_APPOINTMENTS", excepcion.getCodigo());
        assertEquals(Rol.CLIENTE, cliente.getRol());
        verify(repositorioCliente, org.mockito.Mockito.never()).eliminarPerfil(8L);
        verify(repositorioUsuario, org.mockito.Mockito.never()).saveAndFlush(cliente);
    }

    @Test
    void rechazaLaPromocionConServiciosInactivos() {
        Usuario cliente = usuario(8L, "Luis", "luis@correo.com", Rol.CLIENTE);
        Servicio inactivo = servicio(1L, "Corte");
        inactivo.setActivo(false);
        when(repositorioUsuario.findById(8L)).thenReturn(Optional.of(cliente));
        when(repositorioServicio.findAllById(Set.of(1L))).thenReturn(List.of(inactivo));

        ExcepcionReglaNegocio excepcion = assertThrows(ExcepcionReglaNegocio.class,
                () -> servicio.promoverAEmpleado(new EstilistaRequest(8L, Set.of(1L))));

        assertEquals("INACTIVE_SERVICE", excepcion.getCodigo());
        verify(repositorioEstilista, org.mockito.Mockito.never()).insertarPerfil(8L);
    }

    @Test
    void noPromueveUnaCuentaQueYaEsEstilista() {
        Usuario estilista = usuario(8L, "Luis", "luis@correo.com", Rol.PERSONAL);
        when(repositorioUsuario.findById(8L)).thenReturn(Optional.of(estilista));

        ExcepcionReglaNegocio excepcion = assertThrows(ExcepcionReglaNegocio.class,
                () -> servicio.promoverAEmpleado(new EstilistaRequest(8L, Set.of(1L))));

        assertEquals("ALREADY_EMPLOYEE", excepcion.getCodigo());
        assertEquals(HttpStatus.CONFLICT, excepcion.getStatus());
    }

    @Test
    void rechazaLaPromocionSiFaltaAlgunServicio() {
        Usuario cliente = usuario(8L, "Luis", "luis@correo.com", Rol.CLIENTE);
        when(repositorioUsuario.findById(8L)).thenReturn(Optional.of(cliente));
        when(repositorioServicio.findAllById(Set.of(1L, 99L)))
                .thenReturn(List.of(servicio(1L, "Corte")));

        ExcepcionRecursoNoEncontrado excepcion = assertThrows(ExcepcionRecursoNoEncontrado.class,
                () -> servicio.promoverAEmpleado(new EstilistaRequest(8L, Set.of(1L, 99L))));

        assertTrue(excepcion.getMessage().contains("99"));
    }

    @Test
    void devuelveElPerfilConSusServicios() {
        Usuario estilista = usuario(5L, "Marta", "marta@correo.com", Rol.PERSONAL);
        when(repositorioEstilista.existePerfil(5L)).thenReturn(true);
        when(repositorioUsuario.findById(5L)).thenReturn(Optional.of(estilista));
        when(repositorioEstilista.buscarServicios(5L))
                .thenReturn(List.of(servicio(1L, "Corte"), servicio(2L, "Tinte")));

        EstilistaRespuesta respuesta = servicio.obtenerMiPerfil(5L);

        assertEquals(5L, respuesta.id());
        assertEquals(Rol.PERSONAL, respuesta.rol());
        assertEquals(List.of("Corte", "Tinte"),
                respuesta.servicios().stream().map(servicio -> servicio.nombre()).toList());
    }

    @Test
    void noDevuelvePerfilSiLaCuentaNoTieneFilaPersonal() {
        when(repositorioEstilista.existePerfil(5L)).thenReturn(false);

        assertThrows(ExcepcionRecursoNoEncontrado.class, () -> servicio.obtenerMiPerfil(5L));
    }

    @Test
    void desvinculaSinBorrarLaFilaYVaciaLosServicios() {
        Usuario estilista = usuario(5L, "Marta", "marta@correo.com", Rol.PERSONAL);
        when(repositorioEstilista.existePerfil(5L)).thenReturn(true);
        when(repositorioUsuario.findById(5L)).thenReturn(Optional.of(estilista));

        servicio.desvincularEstilista(5L);

        verify(repositorioEstilista).eliminarServicios(5L);
        verify(repositorioUsuario).actualizarRol(5L, Rol.CLIENTE);
        verify(repositorioEstilista, org.mockito.Mockito.never()).insertarPerfil(5L);
    }

    @Test
    void noDesvinculaUnaCuentaQueNoEsEstilistaActiva() {
        Usuario cliente = usuario(5L, "Marta", "marta@correo.com", Rol.CLIENTE);
        when(repositorioEstilista.existePerfil(5L)).thenReturn(true);
        when(repositorioUsuario.findById(5L)).thenReturn(Optional.of(cliente));

        ExcepcionReglaNegocio excepcion = assertThrows(ExcepcionReglaNegocio.class,
                () -> servicio.desvincularEstilista(5L));

        assertEquals("NOT_EMPLOYEE", excepcion.getCodigo());
        org.mockito.Mockito.verify(repositorioEstilista, org.mockito.Mockito.never()).eliminarServicios(5L);
    }

    @Test
    void noDesvinculaSiTieneCitasFuturasPendientesOConfirmadas() {
        Usuario estilista = usuario(5L, "Marta", "marta@correo.com", Rol.PERSONAL);
        when(repositorioEstilista.existePerfil(5L)).thenReturn(true);
        when(repositorioUsuario.findById(5L)).thenReturn(Optional.of(estilista));
        when(repositorioEstilista.tieneCitasFuturasActivas(5L)).thenReturn(true);

        ExcepcionReglaNegocio excepcion = assertThrows(ExcepcionReglaNegocio.class,
                () -> servicio.desvincularEstilista(5L));

        assertEquals("PENDING_APPOINTMENTS", excepcion.getCodigo());
        org.mockito.Mockito.verify(repositorioEstilista, org.mockito.Mockito.never()).eliminarServicios(5L);
        org.mockito.Mockito.verify(repositorioUsuario, org.mockito.Mockito.never())
                .actualizarRol(5L, Rol.CLIENTE);
    }
}
