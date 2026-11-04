-- 1. Creación de la tabla servicio con codificación por enteros
CREATE TABLE servicio (
    nombre VARCHAR(50) NOT NULL,
    tarifa_base NUMERIC(10, 2) NOT NULL,
    precio_km NUMERIC(10, 2) NOT NULL,
    precio_minuto NUMERIC(10, 2) NOT NULL,
    tipo_vehiculo INT NOT NULL,       -- Representación entera del enum TipoVehiculo
    categoria_vehiculo INT NOT NULL,  -- Representación entera del enum CategoriaVehiculo
    tipo_servicio INT NOT NULL,       -- Representación entera del enum TipoServicio
    
    -- Restricción de Clave Primaria
    CONSTRAINT pk_servicio PRIMARY KEY (nombre),
    
    -- Validaciones de rango para los enteros de los Enums (Basado en los ordinales de Java)
    CONSTRAINT chk_tipo_vehiculo CHECK (tipo_vehiculo IN (0, 1)),                  -- 0: AUTO, 1: MOTO
    CONSTRAINT chk_categoria_vehiculo CHECK (categoria_vehiculo IN (0, 1, 2)),     -- 0: ESTANDAR, 1: CONFORT, 2: PREMIUM
    CONSTRAINT chk_tipo_servicio CHECK (tipo_servicio IN (0, 1)),                  -- 0: PASAJEROS, 1: ENVIOS
    
    -- Validaciones de seguridad para importes comerciales
    CONSTRAINT chk_tarifa_base CHECK (tarifa_base >= 0),
    CONSTRAINT chk_precio_km CHECK (precio_km >= 0),
    CONSTRAINT chk_precio_minuto CHECK (precio_minuto >= 0)
);

-- 2. Documentación de la codificación de enteros mediante comentarios en el catálogo
COMMENT ON COLUMN servicio.tipo_vehiculo IS 'Mapeo de TipoVehiculo: 0 = AUTO, 1 = MOTO';
COMMENT ON COLUMN servicio.categoria_vehiculo IS 'Mapeo de CategoriaVehiculo: 0 = ESTANDAR, 1 = CONFORT, 2 = PREMIUM';
COMMENT ON COLUMN servicio.tipo_servicio IS 'Mapeo de TipoServicio: 0 = PASAJEROS, 1 = ENVIOS';

-- 3. Inserción inicial traduciendo los literales de servicios.txt a sus ordinales enteros
INSERT INTO servicio (nombre, tarifa_base, precio_km, precio_minuto, tipo_vehiculo, categoria_vehiculo, tipo_servicio) VALUES
('Estandar', 2000.00, 200.00, 100.00, 0, 0, 0), -- AUTO (0), ESTANDAR (0), PASAJEROS (0)
('Confort', 2500.00, 250.00, 125.00, 0, 1, 0),  -- AUTO (0), CONFORT (1), PASAJEROS (0)
('Premium', 3000.00, 300.00, 150.00, 0, 2, 0),  -- AUTO (0), PREMIUM (2), PASAJEROS (0)
('Envios', 1500.00, 150.00, 75.00, 0, 0, 1),    -- AUTO (0), ESTANDAR (0), ENVIOS (1)
('Mototaxi', 1000.00, 100.00, 50.00, 1, 0, 0),  -- MOTO (1), ESTANDAR (0), PASAJEROS (0)
('Mensajería', 500.00, 50.00, 25.00, 1, 0, 1);  -- MOTO (1), ESTANDAR (0), ENVIOS (1)
