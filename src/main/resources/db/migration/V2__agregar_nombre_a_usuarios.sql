-- Si ya existe algún usuario (por ejemplo el administrador inicial) recibe un nombre provisional.
ALTER TABLE usuarios ADD COLUMN nombre VARCHAR(120) NOT NULL DEFAULT 'Sin nombre';
ALTER TABLE usuarios ALTER COLUMN nombre DROP DEFAULT;
