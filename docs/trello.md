# Tablero de Trello — Ingeniería de Software

Este documento vincula el tablero de Trello con el proyecto. **La fuente de verdad de las tareas es
Trello**; esta página solo registra el enlace, las convenciones y una foto del estado al momento de
integrarlo.

## Datos del tablero

- **Nombre:** ingeniería de software
- **URL:** <https://trello.com/b/gM0zISBq/ingenieria-de-software>
- **ID corto:** `gM0zISBq`
- **ID de API:** `6abdc22d8841903b2acd8214`
- **Equipo:** UCAB, Ingeniería de Software, Equipo 57-6
- **Integrantes:** Andrés Alejandro González Mayorga, María De Jesús Hernández Pérez, Miguel Oswaldo
  Madera Coraspe, Santiago Romero.
- **Vinculado al proyecto:** 2026-11-01

## Listas y significado

| Lista | ID | Uso |
|---|---|---|
| Inicio | `6ad140af2821dc7bb0c7a693` | Entrada de documentos e ideas sin refinar. |
| Planificación | `6ad141445ceed0ddae9061f7` | Requisitos, stack y decisiones listas para entrar al sprint. |
| Ejecución 1er Sprint | `6abdc3ce277eb5c126e60f9c` | Historias y tareas en desarrollo. |
| Seguimiento | `6abdc4ae623c8482ee6a40c2` | Revisión, bloqueos y reuniones. |
| Cierre | `6ad1415b157c65ad1a2594c5` | Terminado y verificado. |

## Flujo de trabajo

El agente **solo consulta** el tablero: no crea, mueve ni edita tarjetas. Los cambios de estado los
hacen las personas del equipo en Trello.

1. Antes de implementar una historia, el agente lee su tarjeta y sus criterios de aceptación.
2. La rama se llama igual que la tarjeta: `feature/<nombre-de-la-tarjeta>`.
3. Al empezar, una persona mueve la tarjeta a **Ejecución 1er Sprint**.
4. Al abrir el Pull Request, una persona la mueve a **Seguimiento** y enlaza el PR en la tarjeta.
5. Al fusionar y verificar los criterios, una persona la mueve a **Cierre**.
6. Usa los prefijos de commit del proyecto (`feat:`, `fix:`, `docs:`, `chore:`, `refactor:`, `test:`)
   y abre el PR hacia `develop`.
7. No crees tarjetas duplicadas: busca primero si la historia ya existe.
8. Respeta el plan de releases del proyecto: lo que pertenece al Release 2 no se adelanta.

## Estado al 2026-11-01

| Lista | Tarjetas |
|---|---:|
| Inicio | 1 |
| Planificación | 9 |
| Ejecución 1er Sprint | 10 |
| Seguimiento | 2 |
| Cierre | 0 |

Tarjetas en **Ejecución 1er Sprint**:

| Tarjeta | Vence | Enlace |
|---|---|---|
| Crear un nuevo Servicio — Andrés González | 2026-11-04 | <https://trello.com/c/0KMrzrxQ> |
| Consultar Servicios Disponibles — Santiago Romero | 2026-11-04 | <https://trello.com/c/GvrpIEwA> |
| Agendar una cita — Miguel Madera | 2026-11-04 | <https://trello.com/c/Nop777Vd> |
| Consultar las citas agendadas | 2026-11-04 | <https://trello.com/c/LXKuzPI3> |
| Consultar la agenda de la peluquería | 2026-11-03 | <https://trello.com/c/iaPjAEc5> |
| Crear Cuenta | 2026-11-04 | <https://trello.com/c/2zkzHpDl> |
| Modificar perfil | 2026-11-03 | <https://trello.com/c/fZErk6Hd> |
| Registrar Empleado | 2026-11-04 | <https://trello.com/c/2Aejcqzg> |
| Ingresar Entrada | 2026-11-03 | <https://trello.com/c/Zk0uJTJR> |
| Especificación de requisitos no funcionales | — | <https://trello.com/c/KFxj5WwJ> |

> **Observación de planificación:** «Consultar la agenda de la peluquería» figura en el sprint, pero
> el plan de releases la ubica en el Release 2. Conviene confirmarlo con el equipo antes de implementarla.

## Acceso desde OpenCode (solo lectura)

El agente **solo consulta** el tablero como contexto: no tiene herramientas para crear, mover ni editar
tarjetas. Los cambios de estado los hace el equipo directamente en Trello.

La consulta no necesita credenciales porque el tablero es público. Los datos se leen con la API de
Trello:

- Tablero: `https://api.trello.com/1/boards/gM0zISBq?fields=name,url,desc`
- Listas: `https://api.trello.com/1/boards/gM0zISBq/lists?fields=name,pos,closed`
- Tarjetas: `https://api.trello.com/1/boards/gM0zISBq/cards?fields=name,idList,pos,closed,due,shortUrl,desc&limit=1000`
- Integrantes: `https://api.trello.com/1/boards/gM0zISBq/members?fields=fullName,username`

No se configura ningún MCP de escritura. Si en el futuro se agrega uno, debe activarse con
`TRELLO_READ_ONLY=true` y sus credenciales, si existen, nunca deben guardarse en el repositorio.
