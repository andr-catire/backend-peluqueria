-- Herencia JOINED: cada tabla comparte la clave primaria con usuarios.
CREATE TABLE personal (
    id BIGINT PRIMARY KEY REFERENCES usuarios (id)
);

CREATE TABLE personal_servicios (
    personal_id BIGINT NOT NULL REFERENCES personal (id),
    servicio_id BIGINT NOT NULL REFERENCES servicios (id),
    PRIMARY KEY (personal_id, servicio_id)
);

CREATE TABLE clientes (
    id                    BIGINT PRIMARY KEY REFERENCES usuarios (id),
    cedula                VARCHAR(20) NOT NULL,
    telefono              VARCHAR(30) NOT NULL,
    estilista_habitual_id BIGINT REFERENCES personal (id),
    CONSTRAINT ux_clientes_cedula UNIQUE (cedula)
);
