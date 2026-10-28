CREATE SCHEMA movilidad_digital;
SET search_path TO movilidad_digital;
CREATE TABLE usuario (email varchar PRIMARY KEY, nombre varchar NOT NULL, telefono varchar NOT NULL);
CREATE TABLE conductor (email varchar PRIMARY KEY REFERENCES usuario(email), licencia_conducir varchar NOT NULL);
CREATE TABLE vehiculo (patente varchar PRIMARY KEY, modelo varchar NOT NULL, capacidad_pasajeros int NOT NULL,
  tipo_vehiculo varchar NOT NULL CHECK (tipo_vehiculo IN ('AUTO','MOTO')),
  categoria_vehiculo varchar NOT NULL CHECK (categoria_vehiculo IN ('ESTANDAR','CONFORT','PREMIUM')),
  email_conductor varchar REFERENCES conductor(email));
CREATE TABLE vehiculo_servicio (patente varchar REFERENCES vehiculo(patente), tipo_servicio varchar CHECK (tipo_servicio IN ('PASAJEROS','ENVIOS')), PRIMARY KEY (patente,tipo_servicio));
CREATE TABLE servicio (nombre varchar PRIMARY KEY, tarifa_base numeric NOT NULL, precio_km numeric NOT NULL, precio_minuto numeric NOT NULL,
  tipo_vehiculo varchar NOT NULL, categoria_vehiculo varchar NOT NULL, tipo_servicio varchar NOT NULL);
INSERT INTO usuario VALUES ('juan@x.com','Juan Perez','111'),('maria@x.com','Maria Gomez','222'),('ana@x.com','Ana Martinez','333');
INSERT INTO conductor VALUES ('juan@x.com','LIC-1'),('ana@x.com','LIC-3');
INSERT INTO vehiculo VALUES ('AD123BC','Corolla',4,'AUTO','PREMIUM','juan@x.com'),('AB456DE','Focus',4,'AUTO','ESTANDAR','juan@x.com'),('AC789FG','Wave',1,'MOTO','ESTANDAR','ana@x.com'),('ZZ999ZZ','Sin dueño',4,'AUTO','CONFORT',NULL);
INSERT INTO vehiculo_servicio VALUES ('AD123BC','PASAJEROS'),('AB456DE','PASAJEROS'),('AB456DE','ENVIOS'),('AC789FG','ENVIOS');
INSERT INTO servicio VALUES ('Viaje Estandar',1000,350,50,'AUTO','ESTANDAR','PASAJEROS'),('Envio Moto',800,250,30,'MOTO','ESTANDAR','ENVIOS');
GRANT ALL ON SCHEMA movilidad_digital TO estudiante; GRANT ALL ON ALL TABLES IN SCHEMA movilidad_digital TO estudiante;
