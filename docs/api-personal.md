# API de personal
## POST /api/personal
Solo `ADMIN`. Crea una cuenta de estilista.

```json
{"nombre":"...","email":"...","password":"********","serviciosIds":[1,2]}
```

- `201`: estilista creado.
- `400`: datos inválidos (`VALIDATION_ERROR`).
- `401`: sin sesión.
- `403`: sin rol `ADMIN`.
- `404`: algún servicio no existe.
- `409`: correo repetido (`EMAIL_TAKEN`) o servicio inactivo (`INACTIVE_SERVICE`).

## GET /api/personal
Solo `ADMIN`. Lista estilistas activos con sus servicios.

- `200`: lista ordenada por nombre.

## DELETE /api/personal/{id}/desvincular
Solo `ADMIN`. Desactiva la cuenta sin cambiar su rol.

- `204`: cuenta desactivada.
- `409`: citas futuras pendientes o confirmadas (`PENDING_APPOINTMENTS`).

## Errores
```json
{ "error": "CODIGO", "message": "..." }
```
