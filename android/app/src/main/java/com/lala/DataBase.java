package com.lala;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;

/**
 * Created by Juan on 07/02/2016.
 */
public class DataBase extends SQLiteOpenHelper {
    private static SQLiteDatabase db;
    private static final String DB_NAME = "Account.sqlite"; // nombre de la base de datos sqlite, no es necesario que tenga extencion
    private static final int DB_SCHEME_VERSION = 2; //es el numero de nuestra version de nuestar base de dator, es decir cada que modificamos
    // la tabla este numero cambia
    private final String TOTALES = "Totales";
    public DataBase(Context context, File AppDir) {
        super(context, AppDir.getAbsolutePath() + "/" + DB_NAME, null, DB_SCHEME_VERSION);

    }

    public void saveDB(SQLiteDatabase db) {
        this.db = db;
    }

    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + DBMan.DBMoneda.TABLE_NAME + "  (" +
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL , " +
                "\"Moneda\" VARCHAR(5) NOT NULL UNIQUE , " +
                "\"Active\" INTEGER NOT NULL DEFAULT 1)");

        db.execSQL("CREATE TABLE "+ DBMan.DBViaje.TABLE_NAME + " ( _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL " +
                ", Nombre varchar ( 50 ) NOT NULL , Descripcion varchar ( 250 ) , FechaCreacion DATETIME NOT NULL DEFAULT CURRENT_DATE ," +
                " FechaCierre DATETIME, FechaInicio DATETIME, FechaFin DATETIME, Total DOUBLE NOT NULL DEFAULT 0.0," +
                " IdMoneda INTEGER NOT NULL, FOREIGN KEY(IdMoneda) REFERENCES Moneda(_id) )");

        db.execSQL("CREATE TABLE " + DBMan.DBTotales.TABLE_NAME + " (" +
                "\"_id\" INTEGER PRIMARY KEY NOT NULL ," +
                "\"Cuenta\" VARCHAR(20) NOT NULL ," +
                "\"CantidadInicial\" DOUBLE NOT NULL ," +
                "\"CurrentCantidad\" DOUBLE NOT NULL ," +
                "\"IdMoneda\" INTEGER NOT NULL DEFAULT (1) ," +
                "\"Activa\" BOOL NOT NULL DEFAULT (1) ," +
                "\"Tipo\" INTEGER DEFAULT (1) )");

        db.execSQL("CREATE TABLE " + DBMan.DBMotivo.TABLE_NAME + " (" +
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL , " +
                "\"Motivo\" VARCHAR(50) NOT NULL UNIQUE , " +
                "\"Active\" BOOL NOT NULL DEFAULT 1)");

        db.execSQL("CREATE TABLE " + DBMan.DBMovimientos.TABLE_NAME + " ( " +
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

        db.execSQL("CREATE TABLE " + DBMan.DBCambioMoneda.TABLE_NAME + "(_id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL, " +
                "IdMoneda1 INTEGER NOT NULL, IdMoneda2 INTEGER  NOT NULL, Tipo_de_cambio DOUBLE NOT NULL, " +
                "FOREIGN KEY(IdMoneda1) REFERENCES Moneda(_id), FOREIGN KEY(IdMoneda2) REFERENCES Moneda(_id) )");

        db.execSQL("CREATE TABLE " + DBMan.DBPersona.TABLE_NAME + " \n" +
                "\t(\"_id\" INTEGER NOT NULL, \n" +
                "\t\"Nombre\" varchar (50), \n" +
                "\t\"Active\" BOOL not null DEFAULT 1, \n" +
                "\tPRIMARY KEY(\"_id\"))");

        db.execSQL("CREATE TABLE " + DBMan.DBPrestamo.TABLE_NAME + " \n" +
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

        db.execSQL("CREATE TABLE " + DBMan.DBPrestamoDetalle.TABLE_NAME + " \n" +
                "    ( \"_id\" INTEGER NOT NULL, \n" +
                "    \"Cantidad\" DOUBLE NOT NULL, \n" +
                "    \"Fecha\" DATETIME NOT NULL DEFAULT CURRENT_DATE, \n" +
                "    \"IdTotales\" INTEGER NOT NULL, \n" +
                "    \"IdMoneda\" INTEGER NOT NULL,\n" +
                "    \"Cambio\" DOUBLE, \n" +
                "    \"IdPrestamo\" INTEGER NOT NULL, \n" +
                "    PRIMARY KEY(\"_id\"), \n" +
                "    FOREIGN KEY(\"IdMoneda\") REFERENCES \"Moneda\"(\"_id\"), \n" +
                "    FOREIGN KEY(\"IdTotales\") REFERENCES \"Totales\"(\"_id\"), \n" +
                "    FOREIGN KEY(\"IdPrestamo\") REFERENCES \"Prestamos\"(\"_id\"))");


        db.execSQL("CREATE TABLE "+ DBMan.DBTiposCuentas.TABLE_NAME +
                " (\"_id\" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , "+ DBMan.DBTiposCuentas.Tipo + " VARCHAR NOT NULL )");


        db.execSQL("CREATE TABLE " + DBMan.DBConfig.TABLE_NAME + " \n" +
                "(_id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , \n" +
                DBMan.DBConfig.Key + " varchar  not null, \n" +
                DBMan.DBConfig.Value + " varchar)");



        db.execSQL("insert into " + DBMan.DBTotales.TABLE_NAME + " \n" +
                "(_id," + DBMan.DBTotales.Cuenta + ", " + DBMan.DBTotales.CantidadInicial + "," +
                DBMan.DBTotales.CantidadActual + "," + DBMan.DBTotales.Moneda + ", " + DBMan.DBTotales.Activa + ", " +
                DBMan.DBTotales.Tipo +")\n" +
                "values\n" +
                "(1, \"Prestamos\", 0, 0, 1, 0, 1)");

        db.execSQL("insert into " + DBMan.DBTotales.TABLE_NAME + " \n" +
                "(_id," + DBMan.DBTotales.Cuenta + ", " + DBMan.DBTotales.CantidadInicial + "," +
                DBMan.DBTotales.CantidadActual + "," + DBMan.DBTotales.Moneda + ", " + DBMan.DBTotales.Activa + ", " +
                DBMan.DBTotales.Tipo +")\n" +
                "values\n" +
                "(20, \"xxxxx\", 0, 0, 1, 0, 1)");

        db.execSQL("insert into " + DBMan.DBMotivo.TABLE_NAME + " \n" +
                "(_id, " + DBMan.DBMotivo.Motivo + ", " + DBMan.DBMotivo.Activo + ") \n" +
                "values \n" +
                "(1, \"Traspaso\", 0)");

        db.execSQL("insert into " + DBMan.DBMotivo.TABLE_NAME +
                "(_id, " + DBMan.DBMotivo.Motivo + ", " + DBMan.DBMotivo.Activo + ") \n" +
                "values \n" +
                "(2, \"Retiro\", 0)");

        db.execSQL("insert into " + DBMan.DBMotivo.TABLE_NAME + " \n" +
                "(_id, " + DBMan.DBMotivo.Motivo + ", " + DBMan.DBMotivo.Activo + ") \n" +
                "values \n" +
                "(3, \"RetiroMonedaDiferente\", 0)");

        db.execSQL("INSERT into "+ DBMan.DBConfig.TABLE_NAME +
                " (_id, " + DBMan.DBConfig.Key + ", " + DBMan.DBConfig.Value + ") "
                + "values ("+ DBMan.DBConfig.LastUpdated + ", '2000-08-26 19:51:14', CURRENT_TIMESTAMP)");

        db.execSQL("INSERT into "+ DBMan.DBConfig.TABLE_NAME +
                " (_id, " + DBMan.DBConfig.Key + ", " + DBMan.DBConfig.Value + ") "
                + "values ("+ DBMan.DBConfig.LastSync + ", 'LastSync', '2000-08-26 19:51:14')");

        db.execSQL("INSERT into "+ DBMan.DBConfig.TABLE_NAME +
                " (_id, " + DBMan.DBConfig.Key + ", " + DBMan.DBConfig.Value + ") "
                + "values ("+ DBMan.DBConfig.Wifi + ", 'Use Wifi Only', '1')");

        db.execSQL("INSERT into "+ DBMan.DBTiposCuentas.TABLE_NAME +
                " (_id, " + DBMan.DBTiposCuentas.Tipo + ") "
                + "values ( 1 , 'Cartera')");

        db.execSQL("INSERT into "+ DBMan.DBTiposCuentas.TABLE_NAME +
                " (_id, " + DBMan.DBTiposCuentas.Tipo + ") "
                + "values ( 2 , 'Tarjeta Credito')");






        // version 2
        db.execSQL("UPDATE AccountsTiposCuentas set Tipo = 'Tarjeta de Credito' where _id = 2");
        db.execSQL("UPDATE AccountsTiposCuentas set Tipo = 'Efectivo' where _id = 1");
        db.execSQL("Insert into AccountsTiposCuentas (_id, Tipo) values (3, 'Tarjeta de Debito')");
        db.execSQL("Insert into AccountsTiposCuentas (_id, Tipo) values (4, 'Cuentas de Inversion')");
        db.execSQL("Insert into AccountsTiposCuentas (_id, Tipo) values (5, 'Prestamos')");

    }


    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("Accoubt DB", "onUpgrade from " + oldVersion + " to " + newVersion);
        if(oldVersion <= 1){
            db.execSQL("UPDATE AccountsTiposCuentas set Tipo = 'Tarjeta de Credito' where _id = 2");
            db.execSQL("UPDATE AccountsTiposCuentas set Tipo = 'Efectivo' where _id = 1");
            db.execSQL("Insert into AccountsTiposCuentas (_id, Tipo) values (3, 'Tarjeta de Debito')");
            db.execSQL("Insert into AccountsTiposCuentas (_id, Tipo) values (4, 'Cuentas de Inversion')");
            db.execSQL("Insert into AccountsTiposCuentas (_id, Tipo) values (5, 'Prestamos')");
        }
    }

}
