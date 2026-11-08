package com.ucab.peluqueria.transferencia;

import java.util.Comparator;
import java.util.List;

import com.ucab.peluqueria.modelo.Estilista;
import com.ucab.peluqueria.modelo.Rol;

/** Datos del estilista para el panel de administración y para su propio perfil. */
public record EstilistaRespuesta(
        Long id, String nombre, String email, Rol rol, List<ServicioRespuesta> servicios) {

    public static EstilistaRespuesta desde(Estilista estilista) {
        List<ServicioRespuesta> servicios = estilista.getServicios().stream()
                .sorted(Comparator.comparing(
                        servicio -> servicio.getNombre().toLowerCase(),
                        Comparator.naturalOrder()))
                .map(ServicioRespuesta::desde)
                .toList();
        return new EstilistaRespuesta(
                estilista.getId(),
                estilista.getNombre(),
                estilista.getEmail(),
                estilista.getRol(),
                servicios);
    }
}
