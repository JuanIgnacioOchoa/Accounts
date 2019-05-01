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

        db.execSQL("CREATE TABLE " + TOTALES + "(_id INTEGER PRIMARY KEY  NOT NULL  UNIQUE , " +
                "Cuenta VARCHAR(20) NOT NULL , Cantidad DOUBLE NOT NULL ," +
                "IdMoneda INTEGER NOT NULL  DEFAULT 1, " +
                "FOREIGN KEY (IdMoneda) REFERENCES Moneda(_id))");
        db.execSQL("CREATE TABLE Motivo (_id INTEGER PRIMARY KEY  NOT NULL , " +
                "Motivo VARCHAR(50) NOT NULL )");
        db.execSQL("CREATE TABLE Movimiento (_id INTEGER PRIMARY KEY  NOT NULL , " +
                "Cantidad DOUBLE NOT NULL ,Fecha DATETIME NOT NULL  DEFAULT CURRENT_DATE, " +
                "IdTotales INTEGER NOT NULL, " +
                "IdMotivo INTEGER NOT NULL, IdMoneda INTEGER NOT NULL, " +
                "Comment VARCHAR(255),"+
                "FOREIGN KEY (IdMotivo) REFERENCES Motivo(_id), " +
                " FOREIGN KEY (IdMoneda) REFERENCES Moneda(_id), " +
                "FOREIGN KEY (IdTotales) REFERENCES Totales(_id))");
        db.execSQL("CREATE TABLE MovTotales (_id INTEGER PRIMARY KEY  NOT NULL ," +
                "IdTotales INTEGER NOT NULL , IdMov INTEGER NOT NULL ," +
                "Cant INTEGER NOT NULL," +
                "FOREIGN KEY(IdTotales) REFERENCES Totales(_id)," +
                "FOREIGN KEY(IdMov) REFERENCES Movimientos(_id) )");
        db.execSQL("CREATE TABLE CambioMoneda(_id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL, " +
                "IdMoneda1 INTEGER NOT NULL, IdMoneda2 INTEGER  NOT NULL, Tipo_de_cambio DOUBLE NOT NULL, " +
                "FOREIGN KEY(IdMoneda1) REFERENCES Moneda(_id), FOREIGN KEY(IdMoneda2) REFERENCES Moneda(_id) )");
        db.execSQL("CREATE TABLE Moneda (_id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , " +
                "Moneda VARCHAR(5) NOT NULL  UNIQUE)");
        db.execSQL("CREATE TABLE Trips ( _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL " +
                ", Nombre varchar ( 50 ) NOT NULL , Descripcion varchar ( 250 ) , FechaCreacion DATETIME NOT NULL DEFAULT CURRENT_DATE ," +
                " FechaCierre DATETIME, FechaInicio DATETIME, FechaFin DATETIME, Total DOUBLE NOT NULL DEFAULT 0.0," +
                " IdMoneda INTEGER NOT NULL, FOREIGN KEY(IdMoneda) REFERENCES Moneda(_id) )");
        //db.execSQL(ManagerSettings.CREATE_TABLE);
        //db.delete(ManagerMotivo.TABLE_NAME,null,null);
    }


    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
