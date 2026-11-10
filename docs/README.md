# Sistema para Peluquería

Sistema web para gestionar una peluquería: catálogo de servicios, personal, clientes y agenda de citas.
Proyecto académico de la UCAB (Ingeniería de Software, Equipo 57-6) con proyección comercial.

## Stack

| Capa | Tecnología |
|---|---|
| Frontend | Vue 3 (Composition API), Vue Router, Pinia, Axios y Vite |
| Backend | Java 25, Spring Boot y Maven (`./mvnw`) |
| Persistencia | Spring Data JPA (Hibernate) |
| Esquema | Hibernate (`ddl-auto=update`, sin Flyway en esta iteración) |
| Seguridad | Spring Security + BCrypt (JWT pendiente) |
| Base de datos | PostgreSQL alojado en Neon |

## Estructura

```text
backend-peluqueria/
├─ docs/                    Brief, contrato de API y documentación
├─ src/                     Código, configuración y pruebas
├─ pom.xml
└─ mvnw
```

El frontend está en un repositorio separado:

```text
https://github.com/andr-catire/frotend-peluqueria.git
```

## Requisitos

- Java 25.
- Node.js 22 LTS y npm (la versión esperada está en `.nvmrc`).
- PostgreSQL 16 local o un proyecto PostgreSQL en Neon.
- Git.

Comprueba las versiones:

```bash
java --version
node --version
npm --version
```

Si usas nvm, activa la versión del proyecto:

```bash
nvm install
nvm use
```

## Configuración local

### 1. Base de datos en Neon

Una persona del equipo crea el proyecto en [Neon](https://neon.tech/) y agrega a los demás integrantes.
Usen una rama `development` y la base `neondb` para el trabajo diario; mantengan `production` separada.

En **Connect**, seleccionen la rama `development`, la base `neondb` y el rol de desarrollo. Copien dos
cadenas conservando los parámetros SSL:

- **Connection pooling activado** para `DB_URL` (el host suele contener `-pooler`).

Spring necesita el formato JDBC. El usuario y la contraseña van separados de la URL:

```text
postgresql://USUARIO:CLAVE@HOST-pooler.REGION.aws.neon.tech/neondb?sslmode=require
        ↓
jdbc:postgresql://HOST-pooler.REGION.aws.neon.tech/neondb?sslmode=require
```

También pueden usar un PostgreSQL local y crear allí la base `peluqueria`.

### 2. Variables del backend

Crea `.env` en la raíz del repositorio del backend (archivo local, no se sube a Git) con:

```dotenv
PORT=3000
FRONTEND_URL=http://localhost:5173

DB_URL=jdbc:postgresql://HOST-pooler.REGION.aws.neon.tech/neondb?sslmode=require
DB_USER=
DB_PASSWORD=

APP_ADMIN_EMAIL=
APP_ADMIN_PASSWORD=
TELEFONO_PELUQUERIA=

HORARIO_ZONA=America/Caracas
HORARIO_APERTURA=08:00
HORARIO_CIERRE=18:00
HORARIO_TOLERANCIA_MINUTOS=0

JPA_SHOW_SQL=false
# JWT_SECRET=  (pendiente: historia de inicio de sesión)
```

Dónde obtener cada valor:

- **`PORT`**: puerto local del backend; por defecto `3000`. En producción puede inyectarlo la plataforma.
- **`FRONTEND_URL`**: URL del frontend permitida por CORS. Local: `http://localhost:5173`.
  Producción: el dominio real del frontend.
- **`DB_URL`**: Neon Dashboard > proyecto > **Connect**; rama `development`, base `neondb`, rol de
  desarrollo y **Connection pooling activado** (host con `-pooler`). Convierte
  `postgresql://...` en `jdbc:postgresql://...`.
- **`DB_USER` y `DB_PASSWORD`**: usuario y contraseña del rol de Neon; aparecen en la cadena de
  conexión o en **Project > Roles**.
- **`APP_ADMIN_EMAIL` y `APP_ADMIN_PASSWORD`**: los elige el equipo; crean el primer `ADMIN` solo si
  no existe ninguno. La contraseña debe tener 8 caracteres o más.
- **`TELEFONO_PELUQUERIA`**: teléfono corporativo que muestra la interfaz; lo proporciona el dueño
  del salón.
- **`HORARIO_ZONA`, `HORARIO_APERTURA`, `HORARIO_CIERRE` y `HORARIO_TOLERANCIA_MINUTOS`**: horario
  del local para la asistencia del personal. La apertura debe ser anterior al cierre.
- **`JPA_SHOW_SQL`**: `true` solo para depurar SQL; normalmente `false`.
- **`JWT_SECRET`**: pendiente para la historia de inicio de sesión; se genera con
  `openssl rand -base64 48`.

`application.properties` solo contiene referencias `${...}`; nunca valores reales.

### 3. Backend

```bash
./mvnw spring-boot:run
```

La API queda en `http://localhost:3000`. Al arrancar, Hibernate crea o actualiza las tablas a partir
de las entidades `@Entity` (`spring.jpa.hibernate.ddl-auto=update`).

### 4. Frontend

El frontend está en su propio repositorio:

```text
https://github.com/andr-catire/frotend-peluqueria.git
```

```bash
git clone https://github.com/andr-catire/frotend-peluqueria.git
cd frotend-peluqueria
npm install
npm run dev
```

Crea `.env` en la raíz del repositorio del frontend con:

```dotenv
VITE_API_URL=http://localhost:3000/api
```

`VITE_API_URL` es la URL del backend terminada en `/api`; en producción usa el dominio público del
backend. Reinicia Vite si cambias este archivo.

Abre la dirección que muestra Vite, normalmente `http://localhost:5173`.

## Endpoint de salud

```bash
curl http://localhost:3000/api/health
```

Respuesta esperada:

```json
{ "status": "ok" }
```

La vista inicial del frontend también muestra este estado.

## Registro y gestión de personal

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/api/auth/registro` | Público | Crea una cuenta con rol `CLIENTE` y su ficha de cliente. |
| `POST` | `/api/auth/login` | Público | Inicia sesión y devuelve el token JWT. |
| `POST` | `/api/personal` | `ADMIN` | Crea una cuenta nueva de estilista y le asigna servicios. |
| `GET` | `/api/personal` | `ADMIN` | Lista estilistas activos con sus servicios. |
| `GET` | `/api/personal/mi-perfil` | `PERSONAL` | Perfil del estilista autenticado, con sus servicios. |
| `DELETE` | `/api/personal/{idEstilista}/desvincular` | `ADMIN` | Desactiva la cuenta sin cambiar su rol ni borrar su historial. |

Reglas de integridad aplicadas:

- El empleado se crea directamente con `Rol.PERSONAL`; ya no se promueve una cuenta de cliente.
- El correo debe ser único (`EMAIL_TAKEN`), la contraseña inicial debe tener entre 8 y 72 caracteres
  y se cifra con BCrypt.
- Se deben asignar entre 1 y 50 servicios activos (`INACTIVE_SERVICE` si alguno está inactivo).
- La desactivación conserva el rol `PERSONAL` y bloquea si hay citas `PENDIENTE` o `CONFIRMADA` desde
  la fecha de hoy en `America/Caracas` (`PENDING_APPOINTMENTS`).
- Una cuenta desactivada no puede iniciar sesión ni usar un token válido.

El registro no emite token: después de crear la cuenta, el usuario inicia sesión para obtenerlo.
Para probar la API por `curl` o Postman, consulta
[`api-personal.md`](api-personal.md). El backend incluye HTTP Basic opcional con
`APP_BASIC_AUTH=true`, apagado por defecto y solo para desarrollo local.

## Base de datos y esquema

- En esta iteración no se usa Flyway ni existen scripts en `src/main/resources/db/migration/`.
- Hibernate administra el esquema con `spring.jpa.hibernate.ddl-auto=update`.
- `mvn compile` no crea tablas; se crean o actualizan al arrancar la aplicación con `./mvnw spring-boot:run`.
- `update` no crea la restricción de exclusión anti-doble-reserva ni los índices parciales que definían
  las migraciones. Esa garantía queda pendiente para cuando se reintroduzca Flyway.
- No usar `update` en producción.

## Comandos de calidad

```bash
# Backend
./mvnw clean verify

# Frontend (en su repositorio)
npm run lint
npm run build
```

La prueba que levanta todo el contexto está deshabilitada hasta que el equipo configure una base de
datos PostgreSQL de pruebas.

## Gestión de tareas

El proyecto se gestiona en el tablero de Trello
[ingeniería de software](https://trello.com/b/gM0zISBq/ingenieria-de-software). Allí están las listas,
las tareas y el estado actual del equipo.

## Documentación

- [`BRIEF EQUIPO 57 6.pdf`](BRIEF%20EQUIPO%2057%206.pdf): brief del producto.
- Contrato de la API: `api-servicios.md`, `api-personal.md`, `api-asistencia.md` y `api-perfil.md`.

## Seguridad

- No subas archivos `.env`, credenciales de Neon ni secretos.
- Las contraseñas se guardan con BCrypt.
- Por defecto todas las rutas requieren autenticación, salvo `GET /api/health` y `POST /api/auth/registro`.
- Los errores de la API usan el formato `{ "error": "CODIGO", "message": "..." }`.
