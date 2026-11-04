-- 1. Creación de la tabla principal de usuarios
CREATE TABLE usuario (
    email VARCHAR(100) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(30) NOT NULL,
    licencia_conducir VARCHAR(30) NULL,      -- NULL representa que está en blanco (solo cliente)
    
    -- Restricción de Clave Primaria solicitada
    CONSTRAINT pk_usuario PRIMARY KEY (email)
);


-- 2. Inserción de datos en la tabla principal 'usuario' (Carga total de usuarios.txt)
INSERT INTO usuario (nombre, telefono, email, licencia_conducir) VALUES
('Juan Perez', '+549117363900', 'juan.perez@email.com', 'A13-B1-D1-E1'),
('Maria Gomez', '+549112933912', 'maria.gomez@email.com', NULL),
('Carlos Rodriguez', '+549112209805', 'carlos.rodriguez@email.com', NULL),
('Ana Martinez', '+549118220576', 'ana.martinez@email.com', 'B1-D1'),
('Luis Lopez', '+549114307113', 'luis.lopez@email.com', NULL),
('Laura Gonzalez', '+549114054301', 'laura.gonzalez@email.com', NULL),
('Diego Sanchez', '+549113872427', 'diego.sanchez@email.com', 'B1-D1'),
('Sofia Alvarez', '+549113170528', 'sofia.alvarez@email.com', NULL),
('Javier Fernandez', '+549118177968', 'javier.fernandez@email.com', NULL),
('Lucia Garcia', '+549112859791', 'lucia.garcia@email.com', NULL),
('Andres Romero', '+549117676566', 'andres.romero@email.com', NULL),
('Elena Diaz', '+549118213171', 'elena.diaz@email.com', NULL),
('Fernando Heredia', '+549116574866', 'fernando.heredia@email.com', NULL),
('Marta Torres', '+549112729295', 'marta.torres@email.com', NULL),
('Gabriel Suarez', '+549116953410', 'gabriela.suarez@email.com', NULL),
('Paula Vazquez', '+549115539336', 'paula.vazquez@email.com', 'A12'),
('Santiago Castro', '+549112266612', 'santiago.castro@email.com', NULL),
('Valentina Ramirez', '+549112249957', 'valentina.ramirez@email.com', NULL),
('Ricardo Silva', '+549112785972', 'ricardo.silva@email.com', 'B1-D1-G'),
('Camila Benitez', '+549113834068', 'camila.benitez@email.com', NULL),
('Alejandro Mendoza', '+549113951701', 'alejandro.mendoza@email.com', NULL),
('Gabriela Flores', '+549116239227', 'gabriela.flores@email.com', 'B1-D1'),
('Martin Morales', '+549117050099', 'martin.morales@email.com', NULL),
('Natalia Acosta', '+549112222599', 'natalia.acosta@email.com', NULL),
('Manuel Rios', '+549116708064', 'manuel.rios@email.com', 'A13'),
('Isabel Gimenez', '+549113667971', 'isabel.gimenez@email.com', NULL),
('Jorge Medina', '+549118006407', 'jorge.medina@email.com', NULL),
('Clara Luna', '+549117451625', 'clara.luna@email.com', NULL),
('Francisco Herrera', '+549117883140', 'francisco.herrera@email.com', NULL),
('Beatriz Dominguez', '+549116571300', 'beatriz.dominguez@email.com', NULL),
('Gonzalo Perez', '+549115519187', 'gonzalo.perez@email.com', 'B1-A13'),
('Daniela Gomez', '+549113849189', 'daniela.gomez@email.com', NULL),
('Roberto Rodriguez', '+549115768238', 'roberto.rodriguez@email.com', NULL),
('Victoria Martinez', '+549116943118', 'victoria.martinez@email.com', 'B1-D1-D2'),
('Esteban Lopez', '+549114333632', 'esteban.lopez@email.com', NULL),
('Patricia Gonzalez', '+549118789994', 'patricia.gonzalez@email.com', NULL),
('Hugo Sanchez', '+549112054515', 'hugo.sanchez@email.com', NULL),
('Irene Alvarez', '+549118365337', 'irene.alvarez@email.com', NULL),
('Federico Fernandez', '+549118759701', 'federico.fernandez@email.com', NULL),
('Alicia Garcia', '+549113339319', 'alicia.garcia@email.com', 'B1-D1'),
('Mateo Romero', '+549117856417', 'mateo.romero@email.com', NULL),
('Sara Diaz', '+549115545146', 'sara.diaz@email.com', NULL),
('Lucas Heredia', '+549114854228', 'lucas.heredia@email.com', NULL),
('Julia Torres', '+549114330953', 'julia.torres@email.com', NULL),
('Bruno Suarez', '+549113304256', 'bruno.suarez@email.com', NULL),
('Olivia Vazquez', '+549113806182', 'olivia.vazquez@email.com', NULL),
('Joaquin Castro', '+549118404652', 'joaquin.castro@email.com', NULL),
('Emilia Ramirez', '+549114823559', 'emilia.ramirez@email.com', NULL),
('Tomas Silva', '+549112857401', 'tomas.silva@email.com', NULL),
('Florencia Benitez', '+549112778008', 'florencia.benitez@email.com', NULL);

