-- 1. Creación de la tabla vehiculo
CREATE TABLE vehiculo (
    patente VARCHAR(10) NOT NULL,
    modelo VARCHAR(50) NOT NULL,
    capacidad_pasajeros INT NOT NULL,
    tipo_vehiculo INT NOT NULL,          -- 0 = AUTO, 1 = MOTO
    categoria_vehiculo INT NOT NULL,     -- 0 = ESTANDAR, 1 = CONFORT, 2 = PREMIUM
    tipo_servicios INT NOT NULL,         -- Máscara de bits: 1 = PASAJEROS, 2 = ENVIOS, 3 = AMBOS
    
    -- Restricción de Clave Primaria solicitada
    CONSTRAINT pk_vehiculo PRIMARY KEY (patente),
    
    -- Validaciones de rango para los enteros de los Enums
    CONSTRAINT chk_vehiculo_tipo CHECK (tipo_vehiculo IN (0, 1)),
    CONSTRAINT chk_vehiculo_categoria CHECK (categoria_vehiculo IN (0, 1, 2)),
    -- El valor máximo actual con ambos servicios es 3 (1 | 2). Si sumás un 3er servicio (4), este check subiría a 7.
    CONSTRAINT chk_vehiculo_servicios CHECK (tipo_servicios BETWEEN 1 AND 3), 
    
    -- Validación de capacidad física razonable
    CONSTRAINT chk_capacidad_pasajeros CHECK (capacidad_pasajeros >= 1)
);

-- 2. Documentación técnica de la máscara de bits mediante comentarios en pgAdmin
COMMENT ON TABLE vehiculo IS 'Tabla que almacena la flota de rodados simulados y sus habilitaciones comerciales.';
COMMENT ON COLUMN vehiculo.tipo_servicios IS 'Máscara Bitwise: 1 = Solo Pasajeros (bit 0), 2 = Solo Envíos (bit 1), 3 = Ambos servicios habilitados';

-- 3. Inserción inicial traduciendo el archivo vehiculos.txt
INSERT INTO vehiculo (patente, modelo, capacidad_pasajeros, tipo_vehiculo, categoria_vehiculo, tipo_servicios) VALUES
('AD123BC', 'Toyota Corolla', 4, 0, 2, 1), -- AUTO (0), PREMIUM (2), PASAJEROS (1)
('AB456DE', 'Ford Focus', 4, 0, 0, 3),     -- AUTO (0), ESTANDAR (0), PASAJEROS+ENVIOS (1+2=3)
('AC789FG', 'Chevrolet Cruze', 4, 0, 1, 3),  -- AUTO (0), CONFORT (1), PASAJEROS+ENVIOS (1+2=3)
('AD012HI', 'Volkswagen Gol', 4, 0, 0, 2),   -- AUTO (0), ESTANDAR (0), ENVIOS (2)
('AE345JK', 'Fiat Cronos', 4, 0, 0, 3),      -- AUTO (0), ESTANDAR (0), PASAJEROS+ENVIOS (1+2=3)
('AF678LM', 'Peugeot 208', 4, 0, 0, 3),      -- AUTO (0), ESTANDAR (0), PASAJEROS+ENVIOS (1+2=3)
('AG901NO', 'Renault Sandero', 4, 0, 0, 2),   -- AUTO (0), ESTANDAR (0), ENVIOS (2)
('AH234PQ', 'Honda Civic', 4, 0, 1, 1),      -- AUTO (0), CONFORT (1), PASAJEROS (1)
('AI567RS', 'Nissan Versa', 4, 0, 2, 1),      -- AUTO (0), PREMIUM (2), PASAJEROS (1)
('AJ890TU', 'Toyota Yaris', 4, 0, 1, 1),      -- AUTO (0), CONFORT (1), PASAJEROS (1)
('123ABC', 'Honda Wave', 1, 1, 0, 2),        -- MOTO (1), ESTANDAR (0), ENVIOS (2)
('456DEF', 'Yamaha YBR', 2, 1, 0, 3),        -- MOTO (1), ESTANDAR (0), PASAJEROS+ENVIOS (1+2=3)
('789GHI', 'Motomel S2', 1, 1, 0, 2),        -- MOTO (1), ESTANDAR (0), ENVIOS (2)
('012JKL', 'Zanella Custom', 1, 1, 0, 2),    -- MOTO (1), ESTANDAR (0), ENVIOS (2)
('345MNO', 'Bajaj Rouser', 2, 1, 0, 3);      -- MOTO (1), ESTANDAR (0), PASAJEROS+ENVIOS (1+2=3)
