package com.lala;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

/**
 * Created by Juan on 07/02/2016.
 */
public class DataBase extends SQLiteOpenHelper {
    private static SQLiteDatabase db;
    private static final String DB_NAME = "Account.sqlite"; // nombre de la base de datos sqlite, no es necesario que tenga extencion
    private static final int DB_SCHEME_VERSION = 0; //es el numero de nuestra version de nuestar base de dator, es decir cada que modificamos
    // la tabla este numero cambia
    private final String TOTALES = "Totales";
    public DataBase(Context context, File AppDir, int version) {
        super(context, AppDir.getAbsolutePath() + "/" + DB_NAME, null, version);

    }

    public void saveDB(SQLiteDatabase db) {
        this.db = db;
    }

    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE \"Moneda\" (" +
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL , " +
                "\"Moneda\" VARCHAR(5) NOT NULL UNIQUE , " +
                "\"Active\" INTEGER NOT NULL DEFAULT 1)");
        db.execSQL("CREATE TABLE Trips ( _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL " +
                ", Nombre varchar ( 50 ) NOT NULL , Descripcion varchar ( 250 ) , FechaCreacion DATETIME NOT NULL DEFAULT CURRENT_DATE ," +
                " FechaCierre DATETIME, FechaInicio DATETIME, FechaFin DATETIME, Total DOUBLE NOT NULL DEFAULT 0.0," +
                " IdMoneda INTEGER NOT NULL, FOREIGN KEY(IdMoneda) REFERENCES Moneda(_id) )");
        db.execSQL("CREATE TABLE \"Totales\" (" +
                "\"_id\" INTEGER PRIMARY KEY NOT NULL ," +
                "\"Cuenta\" VARCHAR(20) NOT NULL ," +
                "\"CantidadInicial\" DOUBLE NOT NULL ," +
                "\"CurrentCantidad\" DOUBLE NOT NULL ," +
                "\"IdMoneda\" INTEGER NOT NULL DEFAULT (1) ," +
                "\"Activa\" BOOL NOT NULL DEFAULT (1) ," +
                "\"Tipo\" INTEGER DEFAULT (1) )");
        db.execSQL("CREATE TABLE \"Motivo\" (" +
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL , " +
                "\"Motivo\" VARCHAR(50) NOT NULL UNIQUE , " +
                "\"Active\" BOOL NOT NULL DEFAULT 1)");
        db.execSQL("CREATE TABLE \"Movimiento\" ( " +
                "_id INTEGER NOT NULL, " +
                "Cantidad DOUBLE NOT NULL, " +
                "Fecha DATETIME NOT NULL DEFAULT CURRENT_DATE, " +
                "IdTotales INTEGER NOT NULL, " +
                "IdMotivo INTEGER NOT NULL, " +
                "IdMoneda INTEGER NOT NULL, " +
                "Cambio DOUEBLE, " +
                "Traspaso INTEGER, " +
                "comment varchar ( 255 ), " +
                "IdViaje INTEGER, " +
                "PRIMARY KEY(_id), " +
                "FOREIGN KEY(IdMoneda) REFERENCES Moneda(_id), " +
                "FOREIGN KEY(IdTotales) REFERENCES Totales(_id), " +
                "FOREIGN KEY(Traspaso) REFERENCES Totales(_id), " +
                "FOREIGN KEY(IdMotivo) REFERENCES Motivo(_id), " +
                "FOREIGN KEY(IdViaje) REFERENCES Trips(_id))");
        db.execSQL("CREATE TABLE \"TiposCuentas\" (" +
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL , " +
                "\"Tipo\" VARCHAR NOT NULL )");
        db.execSQL("CREATE TABLE CambioMoneda(_id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL, " +
                "IdMoneda1 INTEGER NOT NULL, IdMoneda2 INTEGER  NOT NULL, Tipo_de_cambio DOUBLE NOT NULL, " +
                "FOREIGN KEY(IdMoneda1) REFERENCES Moneda(_id), FOREIGN KEY(IdMoneda2) REFERENCES Moneda(_id) )");
        db.execSQL("CREATE TABLE \"Personas\" \n" +
                "\t(\"_id\" INTEGER NOT NULL, \n" +
                "\t\"Nombre\" varchar (50), \n" +
                "\t\"Active\" BOOL not null DEFAULT 1, \n" +
                "\tPRIMARY KEY(\"_id\"))");
        db.execSQL("CREATE TABLE \"Prestamos\" \n" +
                "    ( \"_id\" INTEGER NOT NULL, \n" +
                "    \"Cantidad\" DOUBLE NOT NULL, \n" +
                "    \"Fecha\" DATETIME NOT NULL DEFAULT CURRENT_DATE, \n" +
                "    \"IdTotales\" INTEGER NOT NULL, \n" +
                "    \"IdMoneda\" INTEGER NOT NULL, \n" +
                "    \"Comment\" varchar ( 255 ), \n" +
                "    \"IdPersona\" INTEGER NOT NULL,\n" +
                "    \"Cambio\" DOUBLE,\n" +
                "    \"IdMovimiento\" INTEGER,\n" +
                "    \"Cerrada\" BOOL not null DEFAULT 0,\n" +
                "    PRIMARY KEY(\"_id\"), \n" +
                "    FOREIGN KEY(\"IdMoneda\") REFERENCES \"Moneda\"(\"_id\"),\n" +
                "    FOREIGN KEY(\"IdMovimiento\") REFERENCES \"Movimiento\"(\"_id\"), \n" +
                "    FOREIGN KEY(\"IdTotales\") REFERENCES \"Totales\"(\"_id\"), \n" +
                "    FOREIGN KEY(\"IdPersona\") REFERENCES \"Personas\"(\"_id\"))");

        db.execSQL("CREATE TABLE \"PrestamosDetalle\" \n" +
                "    ( \"_id\" INTEGER NOT NULL, \n" +
                "    \"Cantidad\" DOUBLE NOT NULL, \n" +
                "    \"Fecha\" DATETIME NOT NULL DEFAULT CURRENT_DATE, \n" +
                "    \"IdTotales\" INTEGER NOT NULL, \n" +
                "    \"IdMoneda\" INTEGER NOT NULL,\n" +
                "    \"Cambio\" DOUBLE, \n" +
                "    \"IdPrestamo\" varchar ( 255 ), \n" +
                "    PRIMARY KEY(\"_id\"), \n" +
                "    FOREIGN KEY(\"IdMoneda\") REFERENCES \"Moneda\"(\"_id\"), \n" +
                "    FOREIGN KEY(\"IdTotales\") REFERENCES \"Totales\"(\"_id\"), \n" +
                "    FOREIGN KEY(\"IdPrestamo\") REFERENCES \"Prestamos\"(\"_id\"))");

        db.execSQL("insert into Totales \n" +
                "(_id, Cuenta, CantidadInicial, CurrentCantidad, IdMoneda, Activa, Tipo)\n" +
                "values\n" +
                "(1, \"Prestamos\", 0, 0, 1, 0, 1)");
        //db.execSQL(ManagerSettings.CREATE_TABLE);
        //db.delete(ManagerMotivo.TABLE_NAME,null,null);
    }


    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
