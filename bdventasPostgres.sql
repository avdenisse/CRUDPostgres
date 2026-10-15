
-- Creacion de db
CREATE DATABASE bdventas;

-- Creacion de schemas
CREATE SCHEMA catalogos;

-- Creacion de tabla de marcas
CREATE TABLE catalogos.marcas
(
	id_marca SERIAL PRIMARY KEY,
	nombre varchar(80) NOT NULL UNIQUE,
	pais_origen VARCHAR(80) NOT NULL,
	estado SMALLINT NOT NULL DEFAULT 1

	CONSTRAINT chk_marca_estado 
	CHECK (estado IN (0, 1))
);


-- Pruebas para validar constraints
--#1 No permite NULL
	INSERT INTO catalogos.marcas 
	VALUES (NULL, 'Nicaragua', 1);

--#2 No se permiten duplicados
	INSERT INTO catalogos.marcas (nombre, pais_origen, estado)
	VALUES ('Almohada', 'Nicaragua', 1);

--#3
	INSERT INTO catalogos.marcas (nombre, pais_origen)
	VALUES ('Audifinos', 'Nicaragua');
--- Insercion de datos
INSERT INTO catalogos.marcas (nombre, pais_origen, estado)
VALUES
('HP', 'Estados Unidos', 1),
('Logitech', 'Suiza', 1),
('Redragon', 'China', 1),
('Samsung', 'Corea del Sur', 1),
('Sony', 'Japon', 1);

---- TABLA 2: Categorias
	CREATE TABLE catalogos.categorias
	(
		id_categoria SERIAL PRIMARY KEY,
		nombre VARCHAR(80) NOT NULL UNIQUE,
		descripcion VARCHAR(250),
		estado SMALLINT NOT NULL DEFAULT 1

		CONSTRAINT chk_categoria_estado 
		CHECK(estado IN (0,1))

	);
	-- Pruebas para validar constraints
--#1 No permite NULL en nombre
	INSERT INTO catalogos.categorias
	VALUES (NULL, 'Descripcion', 1);

--#2 No se permiten duplicados
	INSERT INTO catalogos.categorias (nombre, descripcion, estado)
	VALUES ('Mariana', NULL , 1);

--#3 Default
	INSERT INTO catalogos.categorias (nombre, descripcion)
	VALUES ('Alberto', NULL);
---- Insercion de datos
INSERT INTO catalogos.categorias (nombre, descripcion, estado)
VALUES
('Computadoras', 'Computadoras portatiles y de escritorio', 1),
('Mouse', 'Dispositivos para controlar el cursor', 1),
('Teclados', 'Teclados mecanicos y convencionales', 1),
('Monitores', 'Pantallas para computadoras', 1),
('Audifonos', 'Dispositivos de audio', 1);

-- TABLA 3: Productos
CREATE TABLE catalogos.productos
(
    id_producto SERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL UNIQUE,
    descripcion VARCHAR(200),
    precio NUMERIC(10, 2) NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    fecha_creacion DATE NOT NULL DEFAULT CURRENT_DATE,
    id_categoria INTEGER NOT NULL,
    id_marca INTEGER NOT NULL,
    estado SMALLINT NOT NULL DEFAULT 1,

    CONSTRAINT fk_producto_categoria
        FOREIGN KEY (id_categoria)
        REFERENCES catalogos.categorias(id_categoria),

    CONSTRAINT fk_producto_marca
        FOREIGN KEY (id_marca)
        REFERENCES catalogos.marcas(id_marca),

    CONSTRAINT chk_producto_precio
        CHECK (precio > 0),

    CONSTRAINT chk_producto_stock
        CHECK (stock >= 0),

    CONSTRAINT chk_producto_estado
        CHECK (estado IN (0, 1))
);

-- Validaciones
-- Insercuin de datos
INSERT INTO catalogos.productos
(codigo, nombre, descripcion, precio, stock, id_categoria, id_marca, estado)
VALUES
('P001', 'Laptop HP', 'Laptop de 16GB RAM y 512GB SSD', 25000.00, 15, 1, 1, 1),
('P002', 'Mouse Logitech', 'Mouse inalámbrico', 650.00, 30, 1, 1, 1),
('P003', 'Teclado Redragon', 'Teclado mecánico RGB', 1800.00, 20, 1, 1, 1),
('P004', 'Monitor Samsung', 'Monitor LED de 24 pulgadas', 5500.00, 10, 1, 1, 1),
('P005', 'Audífonos Sony', 'Audífonos Bluetooth', 2200.00, 25, 1, 1, 1),
('P006', 'Laptop HP Pavilion', 'Laptop de 8GB RAM y 256GB SSD',18500.00, 12, 1, 1, 1),
('P007', 'Laptop HP Victus', 'Laptop gamer de 16GB RAM y 1TB SSD', 35000.00, 8, 1, 1, 1),
('P008', 'Mouse Logitech G502', 'Mouse gamer con sensor optico', 1450.00, 25, 2, 2, 1),
('P009', 'Mouse Logitech M185', 'Mouse inalambrico compacto',450.00, 40, 2, 2, 1),
('P010', 'Teclado Redragon Kumara', 'Teclado mecanico con iluminacion RGB',1650.00, 18, 3, 3, 1),
('P011', 'Teclado Redragon Fizz', 'Teclado mecanico compacto de 60 por ciento',1350.00, 22, 3, 3, 1),
('P012', 'Monitor Samsung Odyssey G5', 'Monitor gamer curvo de 27 pulgadas',9500.00, 10, 4, 4, 1),
('P013', 'Monitor Samsung Essential', 'Monitor Full HD de 24 pulgadas',4800.00, 16, 4, 4, 1),
('P014', 'Audifonos Sony WH-CH520', 'Audifonos inalambricos Bluetooth', 2100.00, 30, 5, 5, 1),
('P015', 'Audifonos Sony WH-1000XM5', 'Audifonos con cancelacion de ruido',12500.00, 7, 5, 5, 1);


---Consultas: 
SELECT * FROM catalogos.productos WHERE  stock > 0 AND  estado = 1;

SELECT * FROM catalogos.productos WHERE precio >= 50;


SELECT * FROM catalogos.productos WHERE precio >= 20 AND precio <=100;

SELECT * FROM catalogos.productos WHERE  precio BETWEEN 20 AND 100;


SELECT * FROM catalogos.productos WHERE nombre ILIKE '%Mouse%';

SELECT * FROM catalogos.productos WHERE codigo ILIKE '%P0%' OR nombre ILIKE '%P0%';