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
    PRIMARY KEY("_id"), 
    FOREIGN KEY("IdMoneda") REFERENCES "Moneda"("_id"), 
    FOREIGN KEY("IdTotales") REFERENCES "Totales"("_id"), 
    FOREIGN KEY("IdPersona") REFERENCES "Personas"("_id"))

CREATE TABLE "PrestamosDetalle" 
    ( "_id" INTEGER NOT NULL, 
    "Cantidad" DOUBLE NOT NULL, 
    "Fecha" DATETIME NOT NULL DEFAULT CURRENT_DATE, 
    "IdTotales" INTEGER NOT NULL, 
    "IdMoneda" INTEGER NOT NULL, 
    "IdPrestamo" varchar ( 255 ), 
    PRIMARY KEY("_id"), 
    FOREIGN KEY("IdMoneda") REFERENCES "Moneda"("_id"), 
    FOREIGN KEY("IdTotales") REFERENCES "Totales"("_id"), 
    FOREIGN KEY("IdPrestamo") REFERENCES "Prestamos"("_id"))