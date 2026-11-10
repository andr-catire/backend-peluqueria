# API de asistencia
El empleado (`PERSONAL`) usa la sesión; la hora la decide el servidor.

## GET /api/asistencia?fecha=YYYY-MM-DD
Solo `ADMIN`. Lista estilistas activos con su asistencia de la fecha (hoy por defecto):
nombre, entrada, salida, tardanza, salida anticipada y motivo. Sin entrada, `trabajo=false`.

- `200`: lista. `401`: sin sesión. `403`: no es `ADMIN`.

## GET /api/asistencia/horario
Solo `ADMIN`. Devuelve `apertura`, `cierre`, `zona` y `toleranciaMinutos`.

## PUT /api/asistencia/horario
Solo `ADMIN`. Cambia apertura y cierre; deben ser `HH:MM` y apertura < cierre.

```json
{ "apertura": "09:00", "cierre": "20:00" }
```

- `200`: horario actualizado. `400`: `VALIDATION_ERROR` o `HORARIO_INVALIDO`.
## GET /api/asistencia/hoy
Solo `PERSONAL`. Estado del día con horas, retraso y flags de entrada/salida.

## POST /api/asistencia/entrada
Solo `PERSONAL`. Sin cuerpo. `201`: actualizado. `409`: `ENTRADA_YA_REGISTRADA` o `ENTRADA_FUERA_DE_HORARIO`.

## POST /api/asistencia/salida
Solo `PERSONAL`. Cuerpo opcional con `motivo`; antes del cierre es obligatorio. `200`; `400`: `MOTIVO_REQUERIDO`; `409`: `SIN_ENTRADA` o `SALIDA_YA_REGISTRADA`.

## Errores: `{ "error": "CODIGO", "message": "..." }`
