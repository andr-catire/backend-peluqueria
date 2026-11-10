# API de perfil del cliente

Todos los endpoints requieren un JWT con rol `CLIENTE`. El cliente se obtiene del token; no se recibe
su identificador por la URL.

## Consultar el perfil

`GET /api/perfil`

```json
{
  "nombre": "Ana Pérez",
  "email": "ana@correo.com",
  "cedula": "V-12345678",
  "telefono": "0414-1234567"
}
```

## Actualizar datos personales

`PUT /api/perfil`

```json
{
  "nombre": "Ana Pérez",
  "cedula": "V-87654321",
  "telefono": "0414-7654321"
}
```

Reglas:

- `nombre`: obligatorio, máximo 120 caracteres.
- `cedula`: obligatoria, formato `V-12345678` (5 a 9 dígitos, prefijo `V`/`E` opcional). Es única entre clientes.
- `telefono`: obligatorio, solo números, espacios, guiones y paréntesis (máximo 30 caracteres).

Respuesta: el perfil actualizado con el mismo formato que `GET /api/perfil`.

Errores:

| HTTP | `error` | Cuándo |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Datos con formato inválido |
| 401 | `UNAUTHORIZED` | Sin sesión |
| 403 | `FORBIDDEN` | La cuenta no es `CLIENTE` |
| 409 | `CEDULA_TAKEN` | La cédula ya pertenece a otro cliente |

## Cambiar contraseña

`PUT /api/perfil/password`

```json
{
  "passwordActual": "actual123",
  "passwordNueva": "nuevaClave123"
}
```

Reglas:

- `passwordActual`: obligatoria y debe coincidir con la contraseña vigente.
- `passwordNueva`: obligatoria, entre 8 y 72 caracteres.

Respuesta: `204 No Content`.

Errores:

| HTTP | `error` | Cuándo |
|---|---|---|
| 400 | `PASSWORD_INCORRECT` | La contraseña actual no coincide |
| 400 | `VALIDATION_ERROR` | Falta un campo o la nueva contraseña no tiene la longitud permitida |
| 401 | `UNAUTHORIZED` | Sin sesión |
| 403 | `FORBIDDEN` | La cuenta no es `CLIENTE` |
