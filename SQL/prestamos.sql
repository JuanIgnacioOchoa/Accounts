CREATE TABLE "Personas" 
	("_id" INTEGER NOT NULL, 
	"Nombre" varchar (50), 
	"Active" BOOL not null DEFAULT 1, 
	PRIMARY KEY("_id"))

CREATE TABLE "Prestamos" 
    ( "_id" INTEGER NOT NULL, 
    "Cantidad" DOUBLE NOT NULL, 
    "Fecha" DATETIME NOT NULL DEFAULT CURRENT_DATE, 
    "IdTotales" INTEGER NOT NULL, 
    "IdMoneda" INTEGER NOT NULL, 
    "Comment" varchar ( 255 ), 
    "IdPersona" INTEGER NOT NULL,
    "Cambio" DOUBLE,
    "IdMovimiento" INTEGER,
    "Cerrada" BOOL not null DEFAULT 0,
    PRIMARY KEY("_id"), 
    FOREIGN KEY("IdMoneda") REFERENCES "Moneda"("_id"),
    FOREIGN KEY("IdMovimiento") REFERENCES "Movimiento"("_id"), 
    FOREIGN KEY("IdTotales") REFERENCES "Totales"("_id"), 
    FOREIGN KEY("IdPersona") REFERENCES "Personas"("_id"))

CREATE TABLE "PrestamosDetalle" 
    ( "_id" INTEGER NOT NULL, 
    "Cantidad" DOUBLE NOT NULL, 
    "Fecha" DATETIME NOT NULL DEFAULT CURRENT_DATE, 
    "IdTotales" INTEGER NOT NULL, 
    "IdMoneda" INTEGER NOT NULL,
    "Cambio" DOUBLE, 
    "IdPrestamo" varchar ( 255 ), 
    PRIMARY KEY("_id"), 
    FOREIGN KEY("IdMoneda") REFERENCES "Moneda"("_id"), 
    FOREIGN KEY("IdTotales") REFERENCES "Totales"("_id"), 
    FOREIGN KEY("IdPrestamo") REFERENCES "Prestamos"("_id"))

Alter table "Prestamos"
insert into Totales 
(_id, Cuenta, CantidadInicial, CurrentCantidad, IdMoneda, Activa, Tipo)
values
(1, "Prestamos", 0, 0, 1, 0, 1)