-- 1. Creación de la tabla intermedia normalizada
CREATE TABLE conductor_vehiculo (
    email VARCHAR(100) NOT NULL,
    patente VARCHAR(10) NOT NULL,
    
    -- Clave primaria compuesta para evitar duplicar el mismo vehículo en el mismo conductor
    CONSTRAINT pk_conductor_vehiculo PRIMARY KEY (email, patente),
    
    -- Clave foránea que conecta con la tabla de usuarios
    CONSTRAINT fk_conductor_vehiculo_usuario FOREIGN KEY (email) 
        REFERENCES usuario (email) ON DELETE CASCADE,
        
    -- Clave foránea que conecta con la tabla de vehículos
    CONSTRAINT fk_conductor_vehiculo_vehiculo FOREIGN KEY (patente) 
        REFERENCES vehiculo (patente) ON DELETE CASCADE
);

-- 2. Comentarios de documentación técnica en pgAdmin
COMMENT ON TABLE conductor_vehiculo IS 'Tabla intermedia normalizada que asocia a los conductores con sus rodados habilitados.';
COMMENT ON COLUMN conductor_vehiculo.email IS 'Email del usuario que posee el rol de conductor.';
COMMENT ON COLUMN conductor_vehiculo.patente IS 'Patente del vehículo asignado (El primer registro insertado por email actúa como principal).';


-- 3. Inserción de las relaciones en la tabla intermedia 'conductor_vehiculo'
-- (Mapea secuencialmente todas las patentes secundarias distribuidas en el archivo de texto)
INSERT INTO conductor_vehiculo (email, patente) VALUES
('juan.perez@email.com', 'AD123BC'),
('juan.perez@email.com', '123ABC'),
('juan.perez@email.com', 'AD012HI'),
('ana.martinez@email.com', 'AB456DE'),
('diego.sanchez@email.com', 'AC789FG'),
('paula.vazquez@email.com', '456DEF'),
('ricardo.silva@email.com', 'AE345JK'),
('ricardo.silva@email.com', 'AJ890TU'),
('gabriela.flores@email.com', 'AF678LM'),
('manuel.rios@email.com', '789GHI'),
('gonzalo.perez@email.com', '345MNO'),
('gonzalo.perez@email.com', '012JKL'),
('victoria.martinez@email.com', 'AG901NO'),
('victoria.martinez@email.com', 'AH234PQ'),
('alicia.garcia@email.com', 'AI567RS');
