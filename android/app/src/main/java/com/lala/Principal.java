package com.lala;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/** Accounts
 * Created by Juan on 07/02/2016.
 */
public class Principal {
    public static final String TAG = "Account";
    private static SQLiteDatabase db;
    private static Context context;
    public Principal(SQLiteDatabase db, Context con){
        this.db = db;
        context = con;
    }

    // Totales
    public static Cursor getTotalMoves(int id){
        return db.rawQuery("SELECT * FROM AccountsMovimiento WHERE Fecha BETWEEN date('now', '-1 month') and date('now') " +
                "and (IdTotales = ? or Traspaso = ?) ORDER BY Fecha DESC, _id DESC", new String[]{""+id, ""+id});
    }
    public static Cursor getTotalMoves(int id, String year){
        return db.rawQuery("SELECT * FROM AccountsMovimiento WHERE strftime('%Y',Fecha) == ? " +
                "and (IdTotales = ? or Traspaso = ?) ORDER BY Fecha DESC, _id DESC", new String[]{year,""+id, ""+id});
    }

    public static Cursor getTotalMoves(int id, String month, String year){
        return db.rawQuery("SELECT * FROM AccountsMovimiento WHERE strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? " +
                "and (IdTotales = ? or Traspaso = ?) ORDER BY Fecha DESC, _id DESC", new String[]{year, month,""+id, ""+id});
    }
    public static Cursor getTotalCredit(){
        return db.rawQuery("SELECT * FROM AccountsTotales WHERE Activa == 1 and Tipo = 2",null);
    }
    public static Cursor getTotalWallet(){
        return db.rawQuery("SELECT * FROM AccountsTotales WHERE Tipo == 1 and Activa == 1",null);
    }
    public static Cursor getTotalCredit(int id){
        return db.rawQuery("SELECT * FROM AccountsTotales WHERE (Activa == 1 or _id == ?) and Tipo = 2",new String[]{id+""});
    }
    public static Cursor getTotalWallet(int id){
        return db.rawQuery("SELECT * FROM AccountsTotales WHERE Tipo == 1 and (Activa == 1 or _id == ?)",new String[]{id+""});
    }
    public static Cursor getTotal(int id){
        return db.rawQuery("SELECT * FROM AccountsTotales WHERE _id = ?",new String[]{""+id});
    }
    public static Cursor getTotales(boolean inactivos){
        String activa = "";
        if(!inactivos){
            activa = "Activa == 1 and ";
        }
        return db.rawQuery("SELECT AccountsTotales._id, AccountsMoneda.Moneda, AccountsTotales.Cuenta, COUNT(AccountsTotales.Cuenta) as Count , " +
                "AccountsTotales.CurrentCantidad, AccountsTotales.Activa FROM AccountsTotales, AccountsMoneda LEFT JOIN " +
                "AccountsMovimiento on AccountsMovimiento.IdTotales= AccountsTotales._id and date('now','-1 month') <= date('now') " +
                "WHERE " + activa + " AccountsTotales.IdMoneda == AccountsMoneda._id and AccountsTotales._id > 20 GROUP BY AccountsTotales._id " +
                "ORDER by activa desc, Count DESC" ,null);
    }
    public static Cursor getTotalesWithPrestamo(){
        return db.rawQuery("SELECT AccountsTotales._id, AccountsMoneda.Moneda, AccountsTotales.Cuenta, COUNT(AccountsTotales.Cuenta) as Count , " +
                "AccountsTotales.CurrentCantidad FROM AccountsTotales, AccountsMoneda LEFT JOIN " +
                "AccountsMovimiento on AccountsMovimiento.IdTotales = AccountsTotales._id and date('now','-1 month') <= date('now') " +
                "WHERE Activa == 1 and AccountsTotales.IdMoneda == AccountsMoneda._id and AccountsTotales._id > 20 GROUP BY AccountsTotales._id " +
                "union\n" +
                "SELECT AccountsTotales._id, AccountsMoneda.Moneda, AccountsTotales.Cuenta, (-1) as Count , AccountsTotales.CurrentCantidad " +
                "FROM AccountsTotales, AccountsMoneda\n" +
                "where AccountsTotales.IdMoneda == AccountsMoneda._id and AccountsTotales._id == 1\n" +
                "ORDER by Count DESC",null);
    }
    public static Cursor getSingleTotales(int id){
        return db.rawQuery("SELECT AccountsTotales._id, AccountsMoneda.Moneda, AccountsTotales.Cuenta, COUNT(AccountsTotales.Cuenta) as Count , " +
                " AccountsTotales.CurrentCantidad FROM AccountsTotales, AccountsMoneda WHERE AccountsTotales.IdMoneda = AccountsMoneda._id and " +
                "AccountsTotales._id = ?", new String[]{""+id});
    }
    public static Cursor getTotales(int id){
        return db.rawQuery("SELECT AccountsTotales._id, AccountsMoneda.Moneda, AccountsTotales.Cuenta, COUNT(AccountsTotales.Cuenta) as Count , " +
                "AccountsTotales.CurrentCantidad FROM AccountsTotales, AccountsMoneda LEFT JOIN " +
                "AccountsMovimiento on AccountsMovimiento.IdTotales= AccountsTotales._id and date('now','-1 month') <= date('now') " +
                "WHERE (Activa == 1 or AccountsTotales._id == ?) and AccountsTotales.IdMoneda == AccountsMoneda._id " +
                "GROUP BY AccountsTotales._id ORDER by Fecha DESC, Count DESC" ,new String[]{id+""});
    }
    public static void insertTotales(String cuenta, Double cantidad, int moneda){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBTotales.Cuenta, cuenta);
        contentValues.put(DBMan.DBTotales.CantidadInicial, cantidad);
        contentValues.put(DBMan.DBTotales.CantidadActual, cantidad);
        contentValues.put(DBMan.DBTotales.Moneda, moneda);
        db.insert(DBMan.DBTotales.TABLE_NAME, null, contentValues);
        updateLast();
    }
    public static int getMonedaId(int id){
        Cursor c = db.rawQuery("SELECT IdMoneda FROM AccountsTotales " +
                "WHERE _id = ?", new String[]{""+id});
        c.moveToFirst();
        return c.getInt(c.getColumnIndex(DBMan.DBTotales.Moneda));
    }
    public static String getMonedaTotales(int cuenta){
        Cursor c = db.rawQuery("SELECT AccountsMoneda.Moneda as Moneda " +
                "FROM AccountsMoneda, AccountsTotales " +
                "WHERE AccountsMoneda._id = AccountsTotales.IdMoneda " +
                "and AccountsTotales._id = ?",new String[]{String.valueOf(cuenta)});
        c.moveToFirst();
        return c.getString(c.getColumnIndex(DBMan.DBMoneda.Moneda));
    }
    public static int getIdMonedaTotales(int cuenta){
        Cursor c = db.rawQuery("SELECT IdMoneda " +
                "FROM AccountsTotales " +
                "WHERE AccountsTotales._id = ?",new String[]{String.valueOf(cuenta)});
        c.moveToFirst();
        return c.getInt(c.getColumnIndex("IdMoneda"));
    }
    public static String getCuentaTotales(int cuenta){
        Cursor c = db.rawQuery("SELECT Cuenta " +
                "FROM AccountsTotales WHERE " +
                "AccountsTotales._id = ?",new String[]{String.valueOf(cuenta)});
        c.moveToFirst();
        return c.getString(c.getColumnIndex("Cuenta"));
    }
    public static void newMoveCuenta(double cantidad, int id){
        Cursor c = db.rawQuery("SELECT CurrentCantidad FROM AccountsTotales WHERE _id = ?", new String[]{""+id});
        c.moveToFirst();
        Double priorCant = c.getDouble(c.getColumnIndex(DBMan.DBTotales.CantidadActual));
        //db.execSQL("UPDATE AccountsTotales SET CurrentCantidad = " +(priorCant + cantidad)+ " WHERE _id = " + id);
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBTotales.CantidadActual, (priorCant+cantidad) + "");
        db.update(DBMan.DBTotales.TABLE_NAME, contentValues, "_id = ?", new String[]{id+""});
        updateLast();
    }
    public static void updateTotalesFromPrestamo(double cant, int idCuenta){
        Cursor c = db.rawQuery("SELECT " + DBMan.DBTotales.CantidadActual + " FROM " + DBMan.DBTotales.TABLE_NAME +
                " WHERE _id == " + idCuenta, null);
        c.moveToFirst();
        double can = c.getDouble(c.getColumnIndex(DBMan.DBTotales.CantidadActual));
        //db.execSQL("UPDATE " + DBMan.DBTotales.TABLE_NAME + " SET " + DBMan.DBTotales.CantidadActual + " = " + (can + cant) +
        //        " WHERE _id = " +idCuenta);
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBTotales.CantidadActual, (can + cant));
        db.update(DBMan.DBTotales.TABLE_NAME, contentValues, "_id = ?", new String[]{idCuenta+""});
        updateLast();
    }

    public static void updateTotalesInfo(double cant, String cuenta, int id, boolean activa){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBTotales.CantidadActual, cant);
        contentValues.put(DBMan.DBTotales.Cuenta, cuenta);
        if(activa){
            contentValues.put(DBMan.DBTotales.Activa, 1);
        } else {
            contentValues.put(DBMan.DBTotales.Activa, 0);
        }
        db.update(DBMan.DBTotales.TABLE_NAME, contentValues, "_id = ?", new String[]{id+""});
        updateLast();
    }
    public static Cursor getTotalesTotales(int moneda){
        return db.rawQuery(" SELECT * FROM (" +
                "          select Sum(CurrentCantidad) as Positivo from AccountsTotales where Activa = 1 and CurrentCantidad > 0 and IdMoneda = ? " +
                "        ) as t1, (" +
                "          select Sum(CurrentCantidad) as Negativo from AccountsTotales where Activa = 1 and CurrentCantidad < 0 and IdMoneda = ? " +
                "        ) as t2", new String[]{""+moneda, ""+moneda});
    }
    //Movimientos
    public static Cursor getMovimientos() {
        return db.rawQuery("SELECT * FROM AccountsMovimiento WHERE strftime('%Y',Fecha) = strftime('%Y', date('now'))and " +
                "strftime('%m',Fecha) = strftime('%m',date('now')) and strftime('%d',Fecha) >= 1 and " +
                "Traspaso is null ORDER BY Fecha DESC, _id DESC", null);

        //return db.rawQuery("SELECT * FROM Movimiento WHERE date('now','-1 month') <= date('now') ORDER BY Fecha",null);
    }
    public static long newMove(Double cantidad, int cuenta, String comment,String motivo, String moneda, double cambio){

        ContentValues contentValues;
        updateLast();

        contentValues = new ContentValues();
        contentValues.put(DBMan.DBMovimientos.Cantidad,cantidad);

        contentValues.put(DBMan.DBMovimientos.Comment, comment);
        contentValues.put(DBMan.DBMovimientos.IdMotivo,getMotiveId(motivo));
        contentValues.put("IdMoneda", getIdMoneda(moneda));
        contentValues.put("IdTotales", cuenta);
        if(cambio != -1) contentValues.put("Cambio", cambio);

        return db.insert(DBMan.DBMovimientos.TABLE_NAME,null,contentValues);
    }
    public static Cursor getData(int id){
        return db.rawQuery("SELECT _id, Cantidad, (case strftime('%m', Fecha) when '01' then 'Jan'" +
                " when '02' then 'Feb' when '03' then 'Mar' when '04' then 'Apr' when '05' then 'May' " +
                "when '06' then 'Jun' when '07' then 'Jul' when '08' then 'Aug' when '09' then 'Sep' " +
                "when '10' then 'Oct' when '11' then 'Nov' when '12' then 'Dec' else '' " +
                "end ||'-'|| strftime('%d-%Y', Fecha))  as Fecha, Fecha as nFecha, " +
                "IdTotales, Comment, IdMotivo, IdMoneda, Cambio, Traspaso " + ", " + DBMan.DBMovimientos.IdTrip +
                " FROM AccountsMovimiento WHERE _id = ?",new String[]{""+id});
    }
    public static void eliminarMov(int id) {
        deshacerMov(id);
        db.delete(DBMan.DBMovimientos.TABLE_NAME, "_id = ?", new String[]{id+""});
        updateLast();
    }
    public static void eliminarTras(int id) {
        deshacerTras(id);
        db.delete(DBMan.DBMovimientos.TABLE_NAME, "_id = ?", new String[]{id+""});
        updateLast();
    }


    public static void actualizarMovimiento(int id, Double cantidad, int cuenta, String comment,int motivo, int moneda, double cambio, String date){
        deshacerMov(id);
        String sCambio = null;
        if(cambio != -1.0) sCambio = cambio + "";
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBMovimientos.Cantidad, cantidad);
        contentValues.put(DBMan.DBMovimientos.IdTotales, cuenta);
        contentValues.put(DBMan.DBMovimientos.IdMotivo, motivo);
        contentValues.put(DBMan.DBMovimientos.IdMoneda, moneda);
        contentValues.put(DBMan.DBMovimientos.Cambio, sCambio);
        contentValues.put(DBMan.DBMovimientos.Fecha, date);
        contentValues.put(DBMan.DBMovimientos.Comment, comment);
        db.update(DBMan.DBMovimientos.TABLE_NAME,contentValues, "_id = ?", new String[]{id+""});
        Cursor c = db.rawQuery("SELECT " + DBMan.DBTotales.CantidadActual + " FROM " + DBMan.DBTotales.TABLE_NAME + " WHERE _id = ?", new String[]{cuenta+""});
        c.moveToFirst();
        if(cambio == -1.0) cantidad = cantidad + c.getDouble(c.getColumnIndex(DBMan.DBTotales.CantidadActual));
        else cantidad = (cantidad*cambio) + c.getDouble(c.getColumnIndex(DBMan.DBTotales.CantidadActual));
        contentValues = new ContentValues();
        contentValues.put(DBMan.DBTotales.CantidadActual, cantidad);
        db.update(DBMan.DBTotales.TABLE_NAME, contentValues, "_id = ?", new String[]{cuenta+""});
        updateLast();
    }

    public static Double getIngresoTotal(int Moneda){
        Cursor c = db.rawQuery("SELECT SUM(Ingreso) as Ingreso FROM (\n" +
                "  SELECT sum(Cantidad ) as Ingreso FROM AccountsMovimiento WHERE Cantidad > 0 and IdMoneda == ? and strftime('%Y',Fecha) == strftime('%Y', date('now'))and \n" +
                "  strftime('%m',Fecha) == strftime('%m',date('now')) and strftime('%d',Fecha) >= 1 and strftime('%d',Fecha) <= strftime('%d',date('now'))\n" +
                "union\n" +
                "\n" +
                "SELECT SUM( CASE WHEN (SELECT AccountsTotales.idMoneda FROM AccountsTotales, AccountsMovimiento WHERE AccountsTotales._id == IdTotales and Cambio > 0) <> AccountsMovimiento.IdMoneda\n" +
                "                then Cantidad * -1 end) as Ingreso FROM AccountsMovimiento WHERE Cantidad < 0 and strftime('%Y',Fecha) == strftime('%Y', date('now')) " +
                "and strftime('%m',Fecha) == strftime('%m',date('now')) and strftime('%d',Fecha) >= 1 and strftime('%d',Fecha) <= strftime('%d',date('now')) " +
                "and IdMoneda == ? and Cambio IS NOT NULL and Cambio <> 1 \n" +
                "union\n" +
                "SELECT  SUM(Cantidad) as Ingreso From AccountsTotales, AccountsMovimiento WHERE IdMotivo == 3 and Traspaso == AccountsTotales._id and AccountsTotales.IdMoneda == ? and strftime('%Y',Fecha) == strftime('%Y', date('now')) and strftime('%m',Fecha) == strftime('%m',date('now')) and strftime('%d',Fecha) >= 1 and strftime('%d',Fecha) <= strftime('%d',date('now'))\n" +
                ")\n",new String[]{Moneda+"",Moneda+"",Moneda+""});
        c.moveToFirst();
        Double ingreso;
        try {
            ingreso = c.getDouble(c.getColumnIndex("Ingreso"));
            ingreso++;
            ingreso--;
        } catch (Exception e){
            ingreso = 0.0;
        }
        return ingreso;
    }

    public static Double getIngresoTotalMonthly(int Moneda,String month, String year){
        Cursor c = db.rawQuery("SELECT SUM(Ingreso) as Ingreso FROM (\n" +
                "  SELECT sum(Cantidad ) as Ingreso FROM AccountsMovimiento WHERE Cantidad > 0 and IdMoneda == ? and strftime('%Y',Fecha) == \"" + year +"\" and \n" +
                "  strftime('%m',Fecha) == \"" + month + "\" \n" +
                "union\n" +
                "\n" +
                "SELECT SUM( CASE WHEN (SELECT AccountsTotales.idMoneda FROM AccountsTotales, AccountsMovimiento WHERE AccountsTotales._id == IdTotales and Cambio > 0) <> AccountsMovimiento.IdMoneda\n" +
                "                then Cantidad * -1 end) as Ingreso FROM AccountsMovimiento WHERE Cantidad < 0 and strftime('%Y',Fecha) == \"" + year + "\" and strftime('%m',Fecha) == \"" + month + "\" and " +
                "IdMoneda == ? and Cambio IS NOT NULL and Cambio <> 1 \n" +
                "union\n" +
                "SELECT  SUM(Cantidad) as Ingreso From AccountsTotales, AccountsMovimiento WHERE IdMotivo == 3 and Traspaso == AccountsTotales._id and AccountsTotales.IdMoneda == ? and strftime('%Y',Fecha) == \"" + year +"\" and strftime('%m',Fecha) == \"" + month + "\"\n" +
                ")\n",new String[]{Moneda+"",Moneda+"",Moneda+""});
        c.moveToFirst();
        Double ingreso;
        try {
            ingreso = c.getDouble(c.getColumnIndex("Ingreso"));
            ingreso++;
            ingreso--;
        } catch (Exception e){
            ingreso = 0.0;
        }
        return ingreso;
    }
    public static Double getIngresoTotalYearly(int Moneda, String year){
        Cursor c = db.rawQuery("SELECT SUM(Ingreso) as Ingreso FROM (\n" +
                "  SELECT sum(Cantidad ) as Ingreso FROM AccountsMovimiento WHERE Cantidad > 0 and IdMoneda == ? and strftime('%Y',Fecha) == \"" + year +"\" \n" +
                "union\n" +
                "\n" +
                "SELECT SUM( CASE WHEN (SELECT AccountsTotales.idMoneda FROM AccountsTotales, AccountsMovimiento WHERE AccountsTotales._id == IdTotales and Cambio > 0) <> AccountsMovimiento.IdMoneda\n" +
                "                then Cantidad * -1 end) as Ingreso FROM AccountsMovimiento WHERE Cantidad < 0 and strftime('%Y',Fecha) == \"" + year + "\"  and IdMoneda == ? and Cambio IS NOT NULL and Cambio <> 1 \n" +
                "union\n" +
                "SELECT  SUM(Cantidad) as Ingreso From AccountsTotales, AccountsMovimiento WHERE IdMotivo == 3 and Traspaso == AccountsTotales._id and AccountsTotales.IdMoneda == ? and strftime('%Y',Fecha) == \"" + year +"\"\n" +
                ")\n",new String[]{Moneda+"",Moneda+"",Moneda+""});
        c.moveToFirst();
        Double ingreso;
        try {
            ingreso = c.getDouble(c.getColumnIndex("Ingreso"));
            ingreso++;
            ingreso--;
        } catch (Exception e){
            ingreso = 0.0;
        }
        return ingreso;
    }

    public static Double getGastoTotal(int Moneda){
        Cursor c = db.rawQuery("SELECT SUM(Gasto) as Gasto FROM(\n" +
                "SELECT sum(Cantidad ) as Gasto FROM AccountsMovimiento WHERE Cantidad < 0 and IdMoneda == ? and strftime('%Y',Fecha) == strftime('%Y', date('now'))and \n" +
                "strftime('%m',Fecha) == strftime('%m',date('now')) and strftime('%d',Fecha) >= 1 and strftime('%d',Fecha) <= strftime('%d',date('now'))\n" +
                "union\n" + "SELECT SUM( CASE WHEN (SELECT AccountsTotales.idMoneda FROM AccountsTotales, AccountsMovimiento WHERE AccountsTotales._id == IdTotales and Cambio > 0) == ?\n" +
                " then Cantidad * Cambio end) FROM AccountsMovimiento WHERE Cantidad < 0 and strftime('%Y',Fecha) == strftime('%Y', date('now')) and strftime('%m',Fecha)" +
                " == strftime('%m',date('now')) and strftime('%d',Fecha) >= 1 and strftime('%d',Fecha) <= strftime('%d',date('now')) and Cambio <> 1 \n" +
                "union\n" + "SELECT SUM (CASE WHEN idMotivo == 3 and (SELECT AccountsTotales.idMoneda FROM AccountsTotales, AccountsMovimiento WHERE AccountsTotales._id == IdTotales) == ? THEN" +
                " Cantidad * Cambio * -1 end) FROM AccountsMovimiento WHERE strftime('%Y',Fecha) == strftime('%Y', date('now')) and strftime('%m',Fecha) == strftime('%m',date('now'))" +
                " and strftime('%d',Fecha) >= 1 and strftime('%d',Fecha) <= strftime('%d',date('now'))\n" + ")",new String[]{Moneda+"",Moneda+"",Moneda+""});
        c.moveToFirst();
        Double gasto;
        gasto = c.getDouble(c.getColumnIndex("Gasto"));

        return gasto;
    }
    public static Double getGastoTotalMonthly(int Moneda, String month, String year){
        Cursor c = db.rawQuery("SELECT SUM(Gasto) as Gasto FROM(\n" +
                "SELECT sum(Cantidad ) as Gasto FROM AccountsMovimiento WHERE Cantidad < 0 and IdMoneda == ? and strftime('%Y',Fecha) == \"" +year +"\" and \n" +
                "strftime('%m',Fecha) == \"" + month + "\"\n" +
                "union\n" + "SELECT SUM( CASE WHEN (SELECT AccountsTotales.idMoneda FROM AccountsTotales, AccountsMovimiento WHERE AccountsTotales._id == IdTotales and Cambio > 0) == ?\n" +
                " then Cantidad * Cambio end) FROM AccountsMovimiento WHERE Cantidad < 0 and strftime('%Y',Fecha) == \"" +year +"\" and strftime('%m',Fecha)" +
                " == \"" + month +"\" and Cambio <> 1 \n" +
                "union\n" + "SELECT SUM (CASE WHEN idMotivo == 3 and (SELECT AccountsTotales.idMoneda FROM AccountsTotales, AccountsMovimiento WHERE AccountsTotales._id == IdTotales) == ? THEN" +
                " Cantidad * Cambio * -1 end) FROM AccountsMovimiento WHERE strftime('%Y',Fecha) == \"" +year +"\" and strftime('%m',Fecha) == \"" + month +"\"" +
                " \n" + ")",new String[]{Moneda+"",Moneda+"",Moneda+""});
        c.moveToFirst();
        Double gasto;
        gasto = c.getDouble(c.getColumnIndex("Gasto"));

        return gasto;
    }
    public static Double getGastoTotalYearly(int Moneda, String year){
        Cursor c = db.rawQuery("SELECT SUM(Gasto) as Gasto FROM(\n" +
                "SELECT sum(Cantidad ) as Gasto FROM AccountsMovimiento WHERE Cantidad < 0 and IdMoneda == ? and strftime('%Y',Fecha) == \"" +year +"\"\n" +
                "union\n" + "SELECT SUM( CASE WHEN (SELECT AccountsTotales.idMoneda FROM AccountsTotales, AccountsMovimiento WHERE AccountsTotales._id == IdTotales and Cambio > 0) == ?\n" +
                " then Cantidad * Cambio end) FROM AccountsMovimiento WHERE Cantidad < 0 and strftime('%Y',Fecha) == \"" +year +"\" and Cambio <> 1 \n" +
                "union\n" + "SELECT SUM (CASE WHEN idMotivo == 3 and (SELECT AccountsTotales.idMoneda FROM AccountsTotales, AccountsMovimiento WHERE AccountsTotales._id == IdTotales) == ? THEN" +
                " Cantidad * Cambio * -1 end) FROM AccountsMovimiento WHERE strftime('%Y',Fecha) == \"" +year +"\")",new String[]{Moneda+"",Moneda+"",Moneda+""});
        c.moveToFirst();
        Double gasto;
        gasto = c.getDouble(c.getColumnIndex("Gasto"));

        return gasto;
    }

    public static Cursor getSumByMotivesMonth(int Moneda, String month, String year){
        return db.rawQuery("SELECT \n" +
                        "    AccountsMotivo._id as _id, SUM(Gasto) as Gasto, Ingreso , AccountsMotivo.Motivo as Motivo, (COALESCE(Ingreso,0) - COALESCE(SUM(Gasto),0)) as count1, (0) as isViaje\n" +
                        "    FROM(\n" +
                        "        SELECT \n" +
                        "            sum(Cantidad ) as Gasto, IdMotivo\n" +
                        "            FROM AccountsMovimiento \n" +
                        "            WHERE  IdMoneda == ? and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? and Cantidad < 0 GROUP BY IdMotivo\n" +
                        "        union\n" +
                        "        SELECT \n" +
                        "            SUM( CASE WHEN (\n" +
                        "                SELECT \n" +
                        "                    AccountsTotales.idMoneda\n" +
                        "                    FROM AccountsTotales, AccountsMovimiento \n" +
                        "                    WHERE AccountsTotales._id == IdTotales and Cambio > 0) == ? \n" +
                        "                then Cantidad * Cambio end) as Gasto,\n" +
                        "                IdMotivo\n" +
                        "            FROM AccountsMovimiento \n" +
                        "            WHERE Cantidad < 0 and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? and Cambio <> 1 GROUP BY IdMotivo\n" +
                        "        union\n" +
                        "        SELECT \n" +
                        "            SUM (CASE WHEN idMotivo == 3 and (\n" +
                        "                SELECT \n" +
                        "                    AccountsTotales.idMoneda\n" +
                        "                    FROM AccountsTotales, AccountsMovimiento \n" +
                        "                    WHERE AccountsTotales._id == IdTotales) == ? THEN\n" +
                        "                    Cantidad * Cambio * -1 end) as Gasto, \n" +
                        "                IdMotivo\n" +
                        "                FROM AccountsMovimiento \n" +
                        "                WHERE strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? GROUP BY IdMotivo) as table1 \n" +
                        "LEFT OUTER JOIN (\n" +
                        "SELECT \n" +
                        "    SUM(Ingreso) as Ingreso, IdMotivo2 \n" +
                        "    FROM (\n" +
                        "        SELECT \n" +
                        "            sum(Cantidad ) as Ingreso, IdMotivo as IdMotivo2\n" +
                        "            FROM AccountsMovimiento WHERE Cantidad > 0 and IdMoneda == ? and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? Group BY IdMotivo2\n" +
                        "        union\n" +
                        "        SELECT \n" +
                        "        SUM( CASE WHEN (\n" +
                        "            SELECT \n" +
                        "                AccountsTotales.idMoneda \n" +
                        "                FROM AccountsTotales, AccountsMovimiento \n" +
                        "                WHERE AccountsTotales._id == IdTotales and Cambio > 0) <> AccountsMovimiento.IdMoneda\n" +
                        "            then Cantidad * -1 end) as Ingreso, IdMotivo as IdMotivo2\n" +
                        "            FROM AccountsMovimiento \n" +
                        "            WHERE Cantidad < 0 and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? and IdMoneda == ? and Cambio IS NOT NULL Group BY IdMotivo2\n" +
                        "        union\n" +
                        "        SELECT  \n" +
                        "            SUM(Cantidad) as Ingreso, IdMotivo as IdMotivo2\n" +
                        "            From AccountsTotales, AccountsMovimiento \n" +
                        "            WHERE IdMotivo2 == 3 and Traspaso == AccountsTotales._id and AccountsTotales.IdMoneda == ? and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? Group BY IdMotivo ) as table3, AccountsMotivo \n" +
                        "    WHERE table3.IdMotivo2 == AccountsMotivo._id GROUP BY IdMotivo2\n" +
                        ") as table2 on table1.IdMotivo = table2.IdMotivo2 ,  AccountsMotivo WHERE table1.IdMotivo == AccountsMotivo._id and (Gasto IS NOT NULL or Ingreso IS NOT NULL) GROUP BY Motivo \n" +
                        "                        union\n" +
                        "                        SELECT \n" +
                        "                            AccountsTrips._id as _id, SUM(Gasto) as Gasto, Ingreso , AccountsTrips.Nombre as Motivo, (COALESCE(Ingreso,0) - COALESCE(SUM(Gasto),0)) as count1, (1) as isViaje\n" +
                        "                            FROM(\n" +
                        "                                SELECT \n" +
                        "                                    sum(Cantidad ) as Gasto, IdViaje\n" +
                        "                                    FROM AccountsMovimiento \n" +
                        "                                    WHERE  IdMoneda == ? and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? and Cantidad < 0 GROUP BY IdViaje\n" +
                        "                                union\n" +
                        "                                SELECT \n" +
                        "                                    SUM( CASE WHEN (\n" +
                        "                                        SELECT \n" +
                        "                                            AccountsTotales.IdMoneda \n" +
                        "                                            FROM AccountsTotales, AccountsMovimiento \n" +
                        "                                            WHERE AccountsTotales._id == IdTotales and Cambio > 0) == ? \n" +
                        "                                    then Cantidad * Cambio end) as Gasto, IdViaje\n" +
                        "                                    FROM AccountsMovimiento \n" +
                        "                                    WHERE Cantidad < 0 and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? and Cambio <> 1 GROUP BY IdViaje\n" +
                        "                                ) as table1 \n" +
                        "                        LEFT OUTER JOIN (\n" +
                        "                        SELECT \n" +
                        "                            SUM(Ingreso) as Ingreso, IdViaje\n" +
                        "                            FROM (\n" +
                        "                                SELECT \n" +
                        "                                    sum(Cantidad ) as Ingreso, IdViaje\n" +
                        "                                    FROM AccountsMovimiento \n" +
                        "                                    WHERE Cantidad > 0 and IdMoneda == ? and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ?\n" +
                        "                                    Group BY IdViaje\n" +
                        "                                union\n" +
                        "                                SELECT SUM( CASE WHEN (\n" +
                        "                                    SELECT AccountsTotales.idMoneda \n" +
                        "                                    FROM AccountsTotales, AccountsMovimiento \n" +
                        "                                    WHERE AccountsTotales._id == IdTotales and Cambio > 0) <> AccountsMovimiento.IdMoneda\n" +
                        "                                then Cantidad * -1 end) as Ingreso, IdViaje\n" +
                        "                                FROM AccountsMovimiento \n" +
                        "                                WHERE Cantidad < 0 and IdMoneda == ? and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? and Cambio IS NOT NULL Group BY IdViaje\n" +
                        "                                ) as table3, AccountsTrips\n" +
                        "                            WHERE table3.IdViaje == AccountsTrips._id GROUP BY IdViaje\n" +
                        "                        ) as table2 on table1.IdViaje = table2.IdViaje ,  AccountsTrips\n" +
                        "                        WHERE table1.IdViaje == AccountsTrips._id and (Gasto IS NOT NULL or Ingreso IS NOT NULL) GROUP BY Motivo ORDER BY count1 DESC",
                new String[]{Moneda+"", year, month, Moneda+"", year, month, Moneda+"", year, month, Moneda+"", year, month, Moneda+"", year, month, Moneda+"", year, month, Moneda+"", year, month, Moneda+"", year, month, Moneda+"", year, month});
    }
    public static Cursor getSumByMotivesYear(int Moneda, String year){
        return db.rawQuery("SELECT \n" +
                        "    AccountsMotivo._id as _id, SUM(Gasto) as Gasto, Ingreso , AccountsMotivo.Motivo as Motivo, (COALESCE(Ingreso,0) - COALESCE(SUM(Gasto),0)) as count1, (0) as isViaje\n" +
                        "    FROM(\n" +
                        "        SELECT \n" +
                        "            sum(Cantidad ) as Gasto, IdMotivo\n" +
                        "            FROM AccountsMovimiento \n" +
                        "            WHERE  IdMoneda == ? and strftime('%Y',Fecha) == ? and Cantidad < 0 GROUP BY IdMotivo\n" +
                        "        union\n" +
                        "        SELECT \n" +
                        "            SUM( CASE WHEN (\n" +
                        "                SELECT \n" +
                        "                    AccountsTotales.idMoneda\n" +
                        "                    FROM AccountsTotales, AccountsMovimiento \n" +
                        "                    WHERE AccountsTotales._id == IdTotales and Cambio > 0) == ? \n" +
                        "                then Cantidad * Cambio end) as Gasto,\n" +
                        "                IdMotivo\n" +
                        "            FROM AccountsMovimiento \n" +
                        "            WHERE Cantidad < 0 and strftime('%Y',Fecha) == ? and Cambio <> 1 GROUP BY IdMotivo\n" +
                        "        union\n" +
                        "        SELECT \n" +
                        "            SUM (CASE WHEN idMotivo == 3 and (\n" +
                        "                SELECT \n" +
                        "                    AccountsTotales.idMoneda\n" +
                        "                    FROM AccountsTotales, AccountsMovimiento \n" +
                        "                    WHERE AccountsTotales._id == IdTotales) == ? THEN\n" +
                        "                    Cantidad * Cambio * -1 end) as Gasto, \n" +
                        "                IdMotivo\n" +
                        "                FROM AccountsMovimiento \n" +
                        "                WHERE strftime('%Y',Fecha) == ? GROUP BY IdMotivo) as table1 \n" +
                        "LEFT OUTER JOIN (\n" +
                        "SELECT \n" +
                        "    SUM(Ingreso) as Ingreso, IdMotivo2 \n" +
                        "    FROM (\n" +
                        "        SELECT \n" +
                        "            sum(Cantidad ) as Ingreso, IdMotivo as IdMotivo2\n" +
                        "            FROM AccountsMovimiento WHERE Cantidad > 0 and IdMoneda == ? and strftime('%Y',Fecha) == ? Group BY IdMotivo2\n" +
                        "        union\n" +
                        "        SELECT \n" +
                        "        SUM( CASE WHEN (\n" +
                        "            SELECT \n" +
                        "                AccountsTotales.idMoneda \n" +
                        "                FROM AccountsTotales, AccountsMovimiento \n" +
                        "                WHERE AccountsTotales._id == IdTotales and Cambio > 0) <> AccountsMovimiento.IdMoneda\n" +
                        "            then Cantidad * -1 end) as Ingreso, IdMotivo as IdMotivo2\n" +
                        "            FROM AccountsMovimiento \n" +
                        "            WHERE Cantidad < 0 and strftime('%Y',Fecha) == ? and IdMoneda == ? and Cambio IS NOT NULL Group BY IdMotivo2\n" +
                        "        union\n" +
                        "        SELECT  \n" +
                        "            SUM(Cantidad) as Ingreso, IdMotivo as IdMotivo2\n" +
                        "            From AccountsTotales, AccountsMovimiento \n" +
                        "            WHERE IdMotivo2 == 3 and Traspaso == AccountsTotales._id and AccountsTotales.IdMoneda == ? and strftime('%Y',Fecha) == ? Group BY IdMotivo ) as table3, AccountsMotivo \n" +
                        "    WHERE table3.IdMotivo2 == AccountsMotivo._id GROUP BY IdMotivo2\n" +
                        ") as table2 on table1.IdMotivo = table2.IdMotivo2 ,  AccountsMotivo WHERE table1.IdMotivo == AccountsMotivo._id and (Gasto IS NOT NULL or Ingreso IS NOT NULL) GROUP BY Motivo \n" +
                        "                        union\n" +
                        "                        SELECT \n" +
                        "                            AccountsTrips._id as _id, SUM(Gasto) as Gasto, Ingreso , AccountsTrips.Nombre as Motivo, (COALESCE(Ingreso,0) - COALESCE(SUM(Gasto),0)) as count1, (1) as isViaje\n" +
                        "                            FROM(\n" +
                        "                                SELECT \n" +
                        "                                    sum(Cantidad ) as Gasto, IdViaje\n" +
                        "                                    FROM AccountsMovimiento \n" +
                        "                                    WHERE  IdMoneda == ? and strftime('%Y',Fecha) == ? and Cantidad < 0 GROUP BY IdViaje\n" +
                        "                                union\n" +
                        "                                SELECT \n" +
                        "                                    SUM( CASE WHEN (\n" +
                        "                                        SELECT \n" +
                        "                                            AccountsTotales.IdMoneda \n" +
                        "                                            FROM AccountsTotales, AccountsMovimiento \n" +
                        "                                            WHERE AccountsTotales._id == IdTotales and Cambio > 0) == ? \n" +
                        "                                    then Cantidad * Cambio end) as Gasto, IdViaje\n" +
                        "                                    FROM AccountsMovimiento \n" +
                        "                                    WHERE Cantidad < 0 and strftime('%Y',Fecha) == ? and Cambio <> 1  GROUP BY IdViaje\n" +
                        "                                ) as table1 \n" +
                        "                        LEFT OUTER JOIN (\n" +
                        "                        SELECT \n" +
                        "                            SUM(Ingreso) as Ingreso, IdViaje\n" +
                        "                            FROM (\n" +
                        "                                SELECT \n" +
                        "                                    sum(Cantidad ) as Ingreso, IdViaje\n" +
                        "                                    FROM AccountsMovimiento \n" +
                        "                                    WHERE Cantidad > 0 and IdMoneda == ? and strftime('%Y',Fecha) == ?\n" +
                        "                                    Group BY IdViaje\n" +
                        "                                union\n" +
                        "                                SELECT SUM( CASE WHEN (\n" +
                        "                                    SELECT AccountsTotales.idMoneda \n" +
                        "                                    FROM AccountsTotales, AccountsMovimiento \n" +
                        "                                    WHERE AccountsTotales._id == IdTotales and Cambio > 0) <> AccountsMovimiento.IdMoneda\n" +
                        "                                then Cantidad * -1 end) as Ingreso, IdViaje\n" +
                        "                                FROM AccountsMovimiento \n" +
                        "                                WHERE Cantidad < 0 and IdMoneda == ? and strftime('%Y',Fecha) == ? and Cambio IS NOT NULL Group BY IdViaje\n" +
                        "                                ) as table3, AccountsTrips\n" +
                        "                            WHERE table3.IdViaje == AccountsTrips._id GROUP BY IdViaje\n" +
                        "                        ) as table2 on table1.IdViaje = table2.IdViaje , AccountsTrips\n" +
                        "                        WHERE table1.IdViaje == AccountsTrips._id and (Gasto IS NOT NULL or Ingreso IS NOT NULL) GROUP BY Motivo ORDER BY count1 DESC\n",
                new String[]{Moneda+"", year, Moneda+"", year ,Moneda+"",year, Moneda+"",year, Moneda+"",year, Moneda+"", year ,Moneda+"",year, Moneda+"",year, Moneda+"",year});
    }

    public static int getIdPrestamoByMoveId(int id){
        Cursor c = db.rawQuery("SELECT * FROM " + DBMan.DBPrestamo.TABLE_NAME + " WHERE " + DBMan.DBPrestamo.IdMovimiento + " == " +
                id + " LIMIT 1", null);
        if(c.getCount()<= 0){
            return -1;
        }
        c.moveToFirst();
        return c.getInt(c.getColumnIndex("_id"));
    }
    public static void updateMoveFromPrestamo(int idMove, double cantidad, double cambio, int idMoneda){

        ContentValues contentValues = new ContentValues();

        contentValues.put(DBMan.DBMovimientos.Cantidad, cantidad);
        contentValues.put(DBMan.DBMovimientos.Cambio, cambio);
        contentValues.put(DBMan.DBMovimientos.IdMoneda, idMoneda);
        db.update(DBMan.DBMovimientos.TABLE_NAME, contentValues, "_id = ?", new String[]{idMove+""});

        updateLast();
    }
    public static Cursor getSumByMotive(int id, String month, String year){
        if(month == null){
            return db.rawQuery("SELECT AccountsMovimiento._id, cuenta, Cantidad, Comment, Fecha, AccountsMovimiento.IdMoneda FROM AccountsMovimiento, AccountsTotales \n" +
                    "WHERE idTotales = AccountsTotales._id and strftime('%Y',Fecha) == \""+ year+ "\" " +
                    "and AccountsMovimiento.idMotivo == " + id,null);
        }
        return db.rawQuery("SELECT AccountsMovimiento._id, cuenta, Cantidad, Comment, Fecha, AccountsMovimiento.IdMoneda FROM AccountsMovimiento, AccountsTotales \n" +
                "WHERE idTotales = AccountsTotales._id and strftime('%Y',Fecha) == \""+ year+ "\" and " +
                "strftime('%m',Fecha) == \"" + month +"\" and AccountsMovimiento.idMotivo == " + id ,null);
    }
    public static Cursor getTotalesCuentasByMonth(String month, String year){
        return db.rawQuery("select AccountsTotales._id, AccountsTotales.Cuenta, COALESCE(Gasto,0) as Gasto, COALESCE(Ingreso,0) as Ingreso\n" +
                "from(\n" +
                "\tSELECT t._id as idTotales, sum(Gasto) as Gasto \n" +
                "\tfrom(\n" +
                "\t\tSelect _id \n" +
                "\t\tfrom(\n" +
                "\t\t\tselect AccountsTotales.*, AccountsMovimiento.* \n" +
                "\t\t\tfrom AccountsTotales\n" +
                "\t\t\tLEFT JOIN AccountsMovimiento\n" +
                "\t\t\t\ton AccountsTotales._id = AccountsMovimiento.IdTotales\n" +
                "\t\t\t\tWHERE ((strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ?))\n" +
                "\t\t\tGroup by idTotales\n" +
                "\t\tUnion all\n" +
                "\t\t\tSelect AccountsTotales.* , AccountsMovimiento.*\n" +
                "\t\t\tfrom AccountsMovimiento\n" +
                "\t\t\tLEFT JOIN AccountsTotales\n" +
                "\t\t\t\tOn AccountsTotales._id = AccountsMovimiento.IdTotales Where AccountsTotales.Activa\n" +
                "\t\t\tGroup by idTotales\n" +
                "\t\t) group by _id\n" +
                "\t) as t \n" +
                "\tLeft outer join (\n" +
                "\t\tSELECT (sum(CASE WHEN Cambio is not null then cantidad*Cambio else cantidad end )) as Gasto, idTotales \n" +
                "\t\tfrom AccountsMovimiento \n" +
                "\t\t\tWHERE Cantidad < 0 and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? \n" +
                "\t\tGroup by idTotales\n" +
                "\t\tunion\n" +
                "\t\tselect (-1*sum(CASE WHEN Cambio is not null and IdMotivo == 2 then cantidad*Cambio else cantidad end )) as Gasto, idTotales \n" +
                "\t\tfrom AccountsMovimiento \n" +
                "\t\t\tWHERE Traspaso is not null and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? \n" +
                "\t\tGroup by idTotales\n" +
                "\t) as table3 on t._id == table3.idTotales Group by t._id\n" +
                ") as table1\n" +
                "\tLEFT OUTER JOIN (\n" +
                "\tselect idTotales, sum(Ingreso) as Ingreso \n" +
                "\t\tfrom(\n" +
                "\t\t\tselect idTotales, sum(CASE WHEN Cambio is not null then cantidad*Cambio else cantidad end ) as Ingreso \n" +
                "\t\t\tfrom AccountsMovimiento \n" +
                "\t\t\t\tWHERE Cantidad > 0 and Traspaso is null and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? \n" +
                "\t\t\tGroup by idTotales\n" +
                "\t\tunion \n" +
                "\t\tselect  Traspaso as idTotales, sum(CASE WHEN Cambio is not null and IdMotivo <> 2 then cantidad*Cambio else cantidad end ) as Ingreso \n" +
                "\t\tfrom AccountsMovimiento \n" +
                "\t\t\tWHERE Traspaso is not null and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? \n" +
                "\t\tgroup by Traspaso\n" +
                "\t) Group by idTotales\n" +
                ") as table2 on table1.idTotales == table2.idTotales, AccountsMovimiento, AccountsTotales \n" +
                "\tWHERE (AccountsTotales._id == table1.idTotales or AccountsTotales._id == table2.idTotales) \n" +
                "group by AccountsTotales._id\n" +
                "\n",new String[]{year, month,year, month,year, month,year, month,year, month});
    }
    public static Cursor getTotalesCuentasByYear(String year){
        return db.rawQuery("select AccountsTotales._id, AccountsTotales.Cuenta, COALESCE(Gasto,0) as Gasto, COALESCE(Ingreso,0) as Ingreso\n" +
                "from(\n" +
                "\tSELECT t._id as idTotales, sum(Gasto) as Gasto \n" +
                "\tfrom(\n" +
                "\t\tSelect _id \n" +
                "\t\tfrom(\n" +
                "\t\t\tselect Accountstotales.*, AccountsMovimiento.* \n" +
                "\t\t\tfrom AccountsTotales\n" +
                "\t\t\tLEFT JOIN AccountsMovimiento\n" +
                "\t\t\t\ton AccountsTotales._id = AccountsMovimiento.IdTotales\n" +
                "\t\t\t\tWHERE strftime('%Y',Fecha) == ?\n" +
                "\t\t\tGroup by idTotales\n" +
                "\t\tUnion all\n" +
                "\t\t\tSelect AccountsTotales.* , AccountsMovimiento.*\n" +
                "\t\t\tfrom AccountsMovimiento\n" +
                "\t\t\tLEFT JOIN AccountsTotales\n" +
                "\t\t\t\tOn AccountsTotales._id = AccountsMovimiento.IdTotales Where AccountsTotales.Activa\n" +
                "\t\t\tGroup by idTotales\n" +
                "\t\t) group by _id\n" +
                "\t) as t \n" +
                "\tLeft outer join (\n" +
                "\t\tSELECT (sum(CASE WHEN Cambio is not null then cantidad*Cambio else cantidad end )) as Gasto, idTotales \n" +
                "\t\tfrom AccountsMovimiento \n" +
                "\t\t\tWHERE Cantidad < 0 and strftime('%Y',Fecha) == ? \n" +
                "\t\tGroup by idTotales\n" +
                "\t\tunion\n" +
                "\t\tselect (-1*sum(CASE WHEN Cambio is not null and IdMotivo == 2 then cantidad*Cambio else cantidad end )) as Gasto, idTotales \n" +
                "\t\tfrom AccountsMovimiento \n" +
                "\t\t\tWHERE Traspaso is not null and strftime('%Y',Fecha) == ? \n" +
                "\t\tGroup by idTotales\n" +
                "\t) as table3 on t._id == table3.idTotales Group by t._id\n" +
                ") as table1\n" +
                "\tLEFT OUTER JOIN (\n" +
                "\tselect idTotales, sum(Ingreso) as Ingreso \n" +
                "\t\tfrom(\n" +
                "\t\t\tselect idTotales, sum(CASE WHEN Cambio is not null then cantidad*Cambio else cantidad end ) as Ingreso \n" +
                "\t\t\tfrom AccountsMovimiento \n" +
                "\t\t\t\tWHERE Cantidad > 0 and Traspaso is null and strftime('%Y',Fecha) == ? \n" +
                "\t\t\tGroup by idTotales\n" +
                "\t\tunion \n" +
                "\t\tselect  Traspaso as idTotales, sum(CASE WHEN Cambio is not null and IdMotivo <> 2 then cantidad*Cambio else cantidad end ) as Ingreso \n" +
                "\t\tfrom AccountsMovimiento \n" +
                "\t\t\tWHERE Traspaso is not null and strftime('%Y',Fecha) == ? \n" +
                "\t\tgroup by Traspaso\n" +
                "\t) Group by idTotales\n" +
                ") as table2 on table1.idTotales == table2.idTotales, AccountsMovimiento, AccountsTotales \n" +
                "\tWHERE (AccountsTotales._id == table1.idTotales or AccountsTotales._id == table2.idTotales) \n" +
                "group by AccountsTotales._id\n" +
                "\n",new String[]{year, year, year, year, year});
    }
    //Motivos
    public static void insertMotive(String mot){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBMotivo.Motivo,mot);
        db.insert(DBMan.DBMotivo.TABLE_NAME,null, contentValues);

        updateLast();
    }
    public static Cursor getMotive(){
        return db.rawQuery("SELECT AccountsMotivo._id, AccountsMotivo.Motivo, COUNT(AccountsMotivo.Motivo) as Cuenta FROM AccountsMotivo LEFT JOIN " +
                "AccountsMovimiento on AccountsMovimiento.IdMotivo = AccountsMotivo._id and date('now','-1 month') <= date('now') " +
                " WHERE Active == 1 GROUP BY AccountsMotivo._id ORDER by Fecha DESC, Cuenta DESC ",null);
    }
    public static Cursor getMotiveAll(){
        return db.rawQuery("SELECT AccountsMotivo._id, AccountsMotivo.Motivo, AccountsMotivo.Active, COUNT(AccountsMotivo.Motivo) as Cuenta FROM AccountsMotivo LEFT JOIN " +
                "AccountsMovimiento on AccountsMovimiento.IdMotivo = AccountsMotivo._id and date('now','-1 month') <= date('now') " +
                "WHERE AccountsMotivo._id > 15 GROUP BY AccountsMotivo._id ORDER by Active DESC, Fecha DESC, Cuenta DESC ",null);
    }
    public static Cursor getMotive(int id){
        return db.rawQuery("SELECT AccountsMotivo._id, AccountsMotivo.Motivo, COUNT(AccountsMotivo.Motivo) as Cuenta FROM AccountsMotivo LEFT JOIN " +
                "AccountsMovimiento on AccountsMovimiento.IdMotivo = AccountsMotivo._id and date('now','-1 month') <= date('now') " +
                "WHERE Active == 1 or AccountsMotivo._id == ? GROUP BY AccountsMotivo._id ORDER by Fecha DESC, Cuenta DESC ",new String[]{""+id});
    }
    public static int getMotiveId(String motivo){
        Cursor c = db.rawQuery("SELECT _id FROM AccountsMotivo WHERE Motivo = ?",new String[]{motivo});
        c.moveToFirst();
        return c.getInt(c.getColumnIndex("_id"));
    }
    public static String getMotiveId(int id){
        Cursor c = db.rawQuery("SELECT Motivo FROM AccountsMotivo WHERE _id = ?",new String[]{""+id});
        c.moveToFirst();
        return c.getString(c.getColumnIndex("Motivo"));

    }
    public static void updateActiveMotive(int act, int id){

        ContentValues contentValues = new ContentValues();

        contentValues.put(DBMan.DBMotivo.Activo, act);
        db.update(DBMan.DBMotivo.TABLE_NAME, contentValues, "_id = ?", new String[]{id+""});

        updateLast();
    }
    public static void updateNameMotive(String motivo, int id){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBMotivo.Motivo, motivo);
        db.update(DBMan.DBMotivo.TABLE_NAME, contentValues, "_id = ?", new String[]{id+""});

        updateLast();

    }
    //Moneda
    public static Cursor getMoneda(){
        return db.rawQuery("SELECT AccountsMoneda._id, AccountsMoneda.Moneda, COUNT(AccountsMoneda.Moneda) as Cuenta FROM AccountsMoneda LEFT JOIN" +
                " AccountsMovimiento on AccountsMovimiento.IdMoneda = AccountsMoneda._id and date('now','-1 month') <= date('now') WHERE Active == 1" +
                " GROUP BY AccountsMoneda._id ORDER by Fecha DESC, Cuenta DESC ",null);
    }
    public static Cursor getSingleMoneda(int id){
        return db.rawQuery("SELECT AccountsMoneda._id, AccountsMoneda.Moneda FROM AccountsMoneda WHERE _id = ?" +
                " ",new String[]{""+id});
    }
    public static String getTipodeCambio(String MonedaFrom, String MonedaTo){
        int id1 = getIdMoneda(MonedaFrom);
        int id2 = getIdMoneda(MonedaTo);
        MonedaFrom = String.valueOf(id1);
        MonedaTo = String.valueOf(id2);
        Cursor c = db.rawQuery("SELECT AccountsCambioMoneda.Tipo_de_cambio " +
                "FROM AccountsCambioMoneda " +
                "JOIN AccountsMoneda Moneda1 ON Moneda1._id = AccountsCambioMoneda.IdMoneda1 " +
                "JOIN AccountsMoneda Moneda2 ON Moneda2._id = AccountsCambioMoneda.IdMoneda2 " +
                "WHERE AccountsCambioMoneda.IdMoneda1 = ? and AccountsCambioMoneda.IdMoneda2 = ?", new String[]{MonedaFrom,MonedaTo});
        c.moveToFirst();
        return c.getString(c.getColumnIndex("Tipo_de_cambio"));
    }
    public static String getTipodeCambio(int MonedaFrom, int MonedaTo){
        Cursor c = db.rawQuery("SELECT AccountsCambioMoneda.Tipo_de_cambio " +
                "FROM AccountsCambioMoneda " +
                "JOIN AccountsMoneda Moneda1 ON Moneda1._id = AccountsCambioMoneda.IdMoneda1 " +
                "JOIN AccountsMoneda Moneda2 ON Moneda2._id = AccountsCambioMoneda.IdMoneda2 " +
                "WHERE AccountsCambioMoneda.IdMoneda1 = ? and AccountsCambioMoneda.IdMoneda2 = ?", new String[]{MonedaFrom+"",MonedaTo+""});
        c.moveToFirst();
        return c.getString(c.getColumnIndex("Tipo_de_cambio"));
    }
    public static int getIdMoneda(String moneda){
        Cursor c = db.rawQuery("SELECT _id " +
                "FROM AccountsMoneda " +
                "WHERE Moneda = ?", new String[]{moneda});
        c.moveToFirst();
        return c.getInt(c.getColumnIndex("_id"));
    }
    public static String getIdMoneda(int moneda){
        Cursor c = db.rawQuery("SELECT Moneda " +
                "FROM AccountsMoneda " +
                "WHERE _id = ?", new String[]{""+moneda});
        c.moveToFirst();
        return c.getString(c.getColumnIndex("Moneda"));
    }
    public static void actualizarTipoDeCambio(String moneda1,String moneda2, double cambio){
        int id1 = getIdMoneda(moneda1);
        int id2 = getIdMoneda(moneda2);
        moneda1 = String.valueOf(id1);
        moneda2 = String.valueOf(id2);
        Cursor c = db.rawQuery("SELECT AccountsCambioMoneda._id " +
                "FROM AccountsCambioMoneda " +
                "JOIN AccountsMoneda Moneda1 ON Moneda1._id = AccountsCambioMoneda.IdMoneda1 " +
                "JOIN AccountsMoneda Moneda2 ON Moneda2._id = AccountsCambioMoneda.IdMoneda2 " +
                "WHERE AccountsCambioMoneda.IdMoneda1 = ? and AccountsCambioMoneda.IdMoneda2 = ?", new String[]{moneda1,moneda2});
        c.moveToFirst();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBCambioMoneda.Cambio, cambio);
        db.update(DBMan.DBCambioMoneda.TABLE_NAME, contentValues, "_id = ?", new String[]{""+c.getInt(c.getColumnIndex("_id"))});

        c = db.rawQuery("SELECT AccountsCambioMoneda._id " +
                "FROM AccountsCambioMoneda " +
                "JOIN AccountsMoneda Moneda1 ON Moneda1._id = AccountsCambioMoneda.IdMoneda1 " +
                "JOIN AccountsMoneda Moneda2 ON Moneda2._id = AccountsCambioMoneda.IdMoneda2 " +
                "WHERE AccountsCambioMoneda.IdMoneda1 = ? and AccountsCambioMoneda.IdMoneda2 = ?", new String[]{moneda2,moneda1});

        c.moveToFirst();
        contentValues = new ContentValues();
        contentValues.put(DBMan.DBCambioMoneda.Cambio, (1/cambio));
        db.update(DBMan.DBCambioMoneda.TABLE_NAME, contentValues, "_id = ?", new String[]{""+c.getInt(c.getColumnIndex("_id"))});

        updateLast();
    }

    public static void actualizarTipoDeCambio(int moneda1,int moneda2, double cambio){
        Cursor c = db.rawQuery("SELECT AccountsCambioMoneda._id " +
                "FROM AccountsCambioMoneda " +
                "JOIN AccountsMoneda Moneda1 ON Moneda1._id = AccountsCambioMoneda.IdMoneda1 " +
                "JOIN AccountsMoneda Moneda2 ON Moneda2._id = AccountsCambioMoneda.IdMoneda2 " +
                "WHERE AccountsCambioMoneda.IdMoneda1 = ? and AccountsCambioMoneda.IdMoneda2 = ?", new String[]{moneda1+"",moneda2+""});
        c.moveToFirst();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBCambioMoneda.TABLE_NAME, DBMan.DBCambioMoneda.Cambio);
        db.update(DBMan.DBCambioMoneda.TABLE_NAME, contentValues, "_id = ?", new String[]{""+c.getInt(c.getColumnIndex("_id"))});
        updateLast();
    }
///Aqui voy
    //Traspaso
    public static void newTraspaso(int cuentaFrom, int cuentaTo, double cantidad, double cambio, String comment){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBMovimientos.Cantidad,cantidad);
        contentValues.put(DBMan.DBMovimientos.Comment, comment);
        contentValues.put(DBMan.DBMovimientos.IdMotivo,"1");
        contentValues.put(DBMan.DBMovimientos.IdMoneda,"-1");
        contentValues.put(DBMan.DBMovimientos.IdTotales, cuentaFrom);
        contentValues.put(DBMan.DBMovimientos.Traspaso,cuentaTo);
        if(cambio != -1) contentValues.put("Cambio", cambio);
        db.insert(DBMan.DBMovimientos.TABLE_NAME,null,contentValues);
        updateLast();
    }
    //Retiro
    public static void newRetiro(int cuentaFrom, int cuentaTo, double cantidad, double cambio, String comment){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBMovimientos.Cantidad,cantidad);
        contentValues.put(DBMan.DBMovimientos.Comment, comment);
        contentValues.put(DBMan.DBMovimientos.IdMotivo,"2");
        contentValues.put(DBMan.DBMovimientos.IdMoneda,"-2");
        contentValues.put(DBMan.DBMovimientos.IdTotales, cuentaFrom);
        contentValues.put(DBMan.DBMovimientos.Traspaso,cuentaTo);
        if(cambio != -1) contentValues.put("Cambio", cambio);
        db.insert(DBMan.DBMovimientos.TABLE_NAME,null,contentValues);
        updateLast();
    }
    public static void newTipoCambio(int Moneda1, int Moneda2){
        ContentValues c1 = new ContentValues();
        ContentValues c2 = new ContentValues();
        c1.put(DBMan.DBCambioMoneda.Moneda1,Moneda1);
        c1.put(DBMan.DBCambioMoneda.Moneda2,Moneda2);
        c2.put(DBMan.DBCambioMoneda.Moneda1,Moneda2);
        c2.put(DBMan.DBCambioMoneda.Moneda2,Moneda1);
        db.insert(DBMan.DBCambioMoneda.TABLE_NAME,null,c1);
        db.insert(DBMan.DBCambioMoneda.TABLE_NAME,null,c2);
        updateLast();
    }

    public static void guardarMoneda(String moneda){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBMoneda.Moneda,moneda);
        db.insert(DBMan.DBMoneda.TABLE_NAME,null,contentValues);
        Cursor c = db.rawQuery("SELECT * FROM AccountsMoneda",null);
        int N = c.getCount();
        c.moveToLast();
        int idMonedaNueva = c.getInt(c.getColumnIndex("_id"));
        c.moveToPrevious();
        for(int i = 0; i < N-1; i++){
            newTipoCambio(idMonedaNueva,c.getInt(c.getColumnIndex("_id")));
            c.moveToPrevious();
        }
        updateLast();
    }
    public static void actualizarTraspaso(int id,Double cantidad,int idFrom,int idTo,
                                          String comment,int motivo,Double cambio, String date){
        deshacerTras(id);
        String sCambio = null;

        if(Principal.getMonedaId(idFrom) != Principal.getMonedaId(idTo)) sCambio = cambio + "";
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBMovimientos.Cantidad, cantidad);
        contentValues.put(DBMan.DBMovimientos.IdTotales, idFrom);
        contentValues.put(DBMan.DBMovimientos.IdMotivo, motivo);
        contentValues.put(DBMan.DBMovimientos.Traspaso, idTo);
        contentValues.put(DBMan.DBMovimientos.Cambio, sCambio);
        contentValues.put(DBMan.DBMovimientos.Fecha, date);
        contentValues.put(DBMan.DBMovimientos.Comment, comment);
        db.update(DBMan.DBMovimientos.TABLE_NAME, contentValues, "_id = ?", new String[]{""+id});
        if(motivo == 1){
            Principal.newMoveCuenta(cantidad * -1, idFrom);
            if(Principal.getMonedaId(idFrom) != Principal.getMonedaId(idTo)){
                Principal.newMoveCuenta(cantidad * cambio, idTo);
                Principal.actualizarTipoDeCambio(Principal.getMonedaTotales(idFrom), Principal.getMonedaTotales(idTo), cambio);
            }
            else Principal.newMoveCuenta(cantidad, idTo);
        } else {
            Principal.newMoveCuenta(cantidad, idTo);
            if(Principal.getMonedaId(idFrom) != Principal.getMonedaId(idTo)){
                Principal.newMoveCuenta(cantidad * cambio * -1, idFrom);
                Principal.actualizarTipoDeCambio(Principal.getMonedaTotales(idFrom), Principal.getMonedaTotales(idTo), cambio);
            }
            else Principal.newMoveCuenta(cantidad * -1, idFrom);
        }
        updateLast();

    }
    public static void deshacerTras(int id){
        Double cantidad;
        int motivo, idCuentaTo, idCuentaFrom;
        Cursor c = db.rawQuery("SELECT *" + " FROM " +
                DBMan.DBMovimientos.TABLE_NAME + " WHERE _id = ?", new String[]{id+""});
        c.moveToFirst();
        motivo = c.getInt(c.getColumnIndex(DBMan.DBMovimientos.IdMotivo));
        idCuentaTo = c.getInt(c.getColumnIndex(DBMan.DBMovimientos.Traspaso));
        idCuentaFrom = c.getInt(c.getColumnIndex(DBMan.DBMovimientos.IdTotales));
        String cambio = c.getString(c.getColumnIndex(DBMan.DBMovimientos.Cambio));
        cantidad = c.getDouble(c.getColumnIndex(DBMan.DBMovimientos.Cantidad));
        ContentValues contentValues;
        //if(cambio != null) cantidad = cantidad * Double.parseDouble(cambio);
        if(motivo == 1){
            Toast.makeText(context,"Motivo 1:  ",Toast.LENGTH_SHORT).show();
            Cursor cCuenta = (db.rawQuery("SELECT "+ DBMan.DBTotales.CantidadActual + " " +
                    "FROM " + DBMan.DBTotales.TABLE_NAME + " WHERE _id = ?", new String[]{""+idCuentaFrom}));
            cCuenta.moveToFirst();
            Double cantCuenta = cCuenta.getDouble(cCuenta.getColumnIndex(DBMan.DBTotales.CantidadActual));
            cantCuenta = cantCuenta + cantidad;
            contentValues = new ContentValues();
            contentValues.put(DBMan.DBTotales.CantidadActual, cantCuenta);
            db.update(DBMan.DBTotales.TABLE_NAME, contentValues, "_id = ?", new String[]{""+idCuentaFrom});
            cCuenta = db.rawQuery("SELECT "+ DBMan.DBTotales.CantidadActual + " " +
                    "FROM " + DBMan.DBTotales.TABLE_NAME + " WHERE _id = ?", new String[]{""+idCuentaTo});
            cCuenta.moveToFirst();
            cantCuenta = cCuenta.getDouble(cCuenta.getColumnIndex(DBMan.DBTotales.CantidadActual));
            if(cambio != null) cantidad = cantidad * Double.parseDouble(cambio);
            cantCuenta = cantCuenta - cantidad;
            contentValues = new ContentValues();
            contentValues.put(DBMan.DBTotales.CantidadActual, cantCuenta);
            db.update(DBMan.DBTotales.TABLE_NAME, contentValues, "_id = ?", new String[]{idCuentaTo+""});

        } else {
            Toast.makeText(context,"Motivo 2:  ",Toast.LENGTH_SHORT).show();
            Cursor cCuenta = (db.rawQuery("SELECT "+ DBMan.DBTotales.CantidadActual + " " +
                    "FROM " + DBMan.DBTotales.TABLE_NAME + " WHERE _id = ?", new String[]{""+idCuentaTo}));
            cCuenta.moveToFirst();
            Double cantCuenta = cCuenta.getDouble(cCuenta.getColumnIndex(DBMan.DBTotales.CantidadActual));
            Toast.makeText(context,"Cantidad cuenta: " +cantCuenta,Toast.LENGTH_LONG).show();
            Toast.makeText(context,"Cantidad 2:  " + cantCuenta + " - " + cantidad +" = " + (cantCuenta -cantidad),Toast.LENGTH_LONG).show();
            cantCuenta = cantCuenta - cantidad;
            contentValues = new ContentValues();
            contentValues.put(DBMan.DBTotales.CantidadActual, cantCuenta);
            db.update(DBMan.DBTotales.TABLE_NAME, contentValues, "_id = ?", new String[]{""+idCuentaTo});
            cCuenta = db.rawQuery("SELECT "+ DBMan.DBTotales.CantidadActual + " " +
                    "FROM " + DBMan.DBTotales.TABLE_NAME + " WHERE _id = ?", new String[]{""+idCuentaFrom});
            cCuenta.moveToFirst();
            cantCuenta = cCuenta.getDouble(cCuenta.getColumnIndex(DBMan.DBTotales.CantidadActual));
            if (cambio!=null) cantidad = cantidad * Double.parseDouble(cambio);
            Toast.makeText(context,"Cantidad 3:  " + cantCuenta + " + " + cantidad +" = " + (cantCuenta + cantidad),Toast.LENGTH_LONG).show();
            cantCuenta = cantCuenta + cantidad;
            contentValues = new ContentValues();
            contentValues.put(DBMan.DBTotales.CantidadActual, cantCuenta);
            db.update(DBMan.DBTotales.TABLE_NAME, contentValues, "_id = ?", new String[]{idCuentaFrom+""});
        }
        updateLast();
    }
    public static void deshacerMov(int id){
        Double cantidad;
        Cursor c = db.rawQuery("SELECT _id, " + DBMan.DBMovimientos.Cantidad +", " +
                DBMan.DBMovimientos.IdTotales + ", " + DBMan.DBMovimientos.Cambio + " FROM " +
                DBMan.DBMovimientos.TABLE_NAME + " WHERE _id = ?", new String[]{id+""});
        c.moveToFirst();
        String cambio = c.getString(c.getColumnIndex(DBMan.DBMovimientos.Cambio));
        cantidad = c.getDouble(c.getColumnIndex(DBMan.DBMovimientos.Cantidad));
        if(cambio != null) cantidad = cantidad * Double.parseDouble(cambio);
        int idCuenta = c.getInt(c.getColumnIndex(DBMan.DBMovimientos.IdTotales));
        Cursor cCuenta = (db.rawQuery("SELECT "+ DBMan.DBTotales.CantidadActual + " " +
                "FROM " + DBMan.DBTotales.TABLE_NAME + " WHERE _id = ?", new String[]{""+idCuenta}));
        cCuenta.moveToFirst();
        Double cantidadCuenta = cCuenta.getDouble(cCuenta.getColumnIndex(DBMan.DBTotales.CantidadActual));
        cantidad = cantidadCuenta - cantidad;
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBTotales.CantidadActual, cantidad);
        db.update(DBMan.DBTotales.TABLE_NAME, contentValues, "_id = ?", new String[]{idCuenta+""});
    }

    //GetFirst use date
    public static Calendar getFirstDate(){
        Cursor c = db.rawQuery("SELECT Fecha FROM AccountsMovimiento order by Fecha asc limit 1", new String[]{});
        c.moveToFirst();
        if(c.getCount() == 0){
            return Calendar.getInstance();
        }
        String fecha = c.getString(c.getColumnIndex(DBMan.DBMovimientos.Fecha));
        String year = fecha.substring(0,4);
        int yy = Integer.parseInt(year);
        //int mm = Integer.getInteger(fecha.substring()
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR,Integer.parseInt(fecha.substring(0,4)));
        calendar.set(Calendar.MONTH,Integer.parseInt(fecha.substring(5,7)) -1);
        return calendar;
    }

    public static double round(Double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        if(value.isNaN()){
            value = 0.0;
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    //Trips
    public static Cursor getTrips(){
        return db.rawQuery("SELECT t1._id, t1.Nombre, t1.Descripcion, t1.FechaCreacion, t1.FechaCierre, t1.FechaInicio, t1.FechaFin, CASE WHEN t2.Total is null then 0 else t2.Total END Total, t1.IdMoneda  FROM (\n" +
                "\tSELECT t._id, t.Nombre, t.Descripcion, t.FechaCreacion, t.FechaCierre, t.FechaInicio, t.FechaFin, t.IdMoneda  FROM AccountsTrips as t\n" +
                ") as t1 LEFT JOIN (\n" +
                "\tSELECT t._id, t.Nombre, t.Descripcion, t.FechaCreacion, t.FechaCierre, t.FechaInicio, t.FechaFin, sum(m.Cantidad) as Total, t.IdMoneda \n" +
                "                FROM AccountsTrips as t, AccountsMovimiento as m \n" +
                "                Where t._id = m.IdViaje \n" +
                "                GROUP by t._id \n" +
                "                order by FechaCreacion DESC\n" +
                ") as t2 on t1._id = t2._id", null);
    }
    public static Cursor getTrip(int _id){
        Cursor c = db.rawQuery("SELECT * FROM " + DBMan.DBViaje.TABLE_NAME + " WHERE _id == " + _id
                , null);
        c.moveToFirst();
        return c;
    }
    public static Cursor getMovesByTrips(int idTrip){
        return db.rawQuery("SELECT * FROM " + DBMan.DBMovimientos.TABLE_NAME + " WHERE " + DBMan.DBMovimientos.IdTrip +
                " == ?", new String[]{idTrip+""});
    }
    public static boolean createTrip(String nombre, String fechaInic, String fechaFin, int moneda, String descripcion){
        if(fechaFin != null && fechaFin.length() <= 0){
            fechaFin = null;
        }
        if(fechaInic != null && fechaInic.length() <= 0){
            fechaInic = null;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBViaje.Nombre, nombre);
        contentValues.put(DBMan.DBViaje.FechaInicio, fechaInic);
        contentValues.put(DBMan.DBViaje.FechaFin, fechaFin);
        contentValues.put(DBMan.DBViaje.IdMoneda, moneda);
        contentValues.put(DBMan.DBViaje.Descripcion, descripcion);
        long a = db.insert(DBMan.DBViaje.TABLE_NAME,null,contentValues);
        if(a >= 0){
            updateLast();
            return true;
        } else{
            return false;
        }
    }
    public static String getTripNameById(int IdTrip){
        Cursor c = db.rawQuery("SELECT " + DBMan.DBViaje.Nombre +
                " FROM " + DBMan.DBViaje.TABLE_NAME + " WHERE " +
                "_id = ?",new String[]{String.valueOf(IdTrip)});
        c.moveToFirst();
        return c.getString(c.getColumnIndex(DBMan.DBViaje.Nombre));
    }
    public static boolean updateMoveTrip(int idMove, int idTrip, Double cantidad){
        //TODO make it boolean
        //try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBMan.DBMovimientos.IdTrip, idTrip);
            db.update(DBMan.DBMovimientos.TABLE_NAME, contentValues, "_id = ?", new String[]{idMove+""});
            Cursor c = db.rawQuery("SELECT " + DBMan.DBMovimientos.IdMoneda + " FROM " + DBMan.DBMovimientos.TABLE_NAME +
                    " WHERE _id = " + idMove, null);
            c.moveToFirst();
            int monedaMove = c.getInt(c.getColumnIndex(DBMan.DBMovimientos.IdMoneda));
            c = db.rawQuery("SELECT " + DBMan.DBViaje.IdMoneda + " FROM " + DBMan.DBViaje.TABLE_NAME +
                    " WHERE _id = " + idTrip, null);
            c.moveToFirst();
            int monedaViaje = c.getInt(c.getColumnIndex(DBMan.DBMovimientos.IdMoneda));
            if(monedaMove == monedaViaje) {
                c = db.rawQuery("SELECT " + DBMan.DBViaje.CantTotal + " FROM " + DBMan.DBViaje.TABLE_NAME +
                        " WHERE _id == ?",new String[]{idTrip+""});
                c.moveToFirst();
                Double cantActual = c.getDouble(c.getColumnIndex(DBMan.DBViaje.CantTotal));
                contentValues = new ContentValues();
                contentValues.put(DBMan.DBViaje.CantTotal, cantActual + cantidad);
                db.update(DBMan.DBViaje.TABLE_NAME, contentValues, "_id = ?", new String[]{""+idTrip});
            }
            updateLast();
            return true;
        //} catch (Exception e){
        //    return false;
        //}
    }

    public static boolean updateTrip(int _id, String name, String descripcion, String fechaInic, String fechaFin, int moneda){

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBViaje.Nombre, name);
        contentValues.put(DBMan.DBViaje.Descripcion, descripcion);
        contentValues.put(DBMan.DBViaje.FechaInicio, fechaInic);
        contentValues.put(DBMan.DBViaje.FechaFin, fechaFin);
        contentValues.put(DBMan.DBViaje.IdMoneda, moneda);
        db.update(DBMan.DBViaje.TABLE_NAME, contentValues, "_id = ?", new String[]{""+_id});
        updateLast();
        return true;
    }
    public static void addMoveToTrip(int idMove, int idTrip){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBMovimientos.IdTrip, idTrip);
        db.update(DBMan.DBMovimientos.TABLE_NAME, contentValues, "_id = ?", new String[]{""+idMove});
        updateLast();
    }


    //Personas
    public static Cursor getPeopleAll(){
        return db.rawQuery("SELECT AccountsPersonas._id, AccountsPersonas.Nombre, AccountsPersonas.Active, COUNT(AccountsPersonas.Nombre) as Cuenta " +
                "FROM AccountsPersonas LEFT JOIN " +
                "AccountsPrestamos on AccountsPrestamos.IdPersona = AccountsPersonas._id and date('now','-1 month') <= date('now') " +
                " GROUP BY AccountsPersonas._id ORDER by Active DESC, Fecha DESC, Cuenta DESC ",null);
    }
    public static long insertPersona(String persona){
        Cursor c = db.rawQuery("Select _id FROM " + DBMan.DBPersona.TABLE_NAME +
                " WHERE " + DBMan.DBPersona.Nombre + " == \"" + persona + "\"", null);
        if(c.getCount() > 0){
            c.moveToFirst();
            return c.getInt(c.getColumnIndex("_id"));
        }
        updateLast();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBPersona.Nombre,persona);
        return (db.insert(DBMan.DBPersona.TABLE_NAME,null, contentValues));
    }
    public static void updateActivePeople(int act, int id){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBPersona.Activo, act);
        db.update(DBMan.DBPersona.TABLE_NAME, contentValues, "_id = ?", new String[]{""+id});
        updateLast();
    }
    public static void updateNamePeople(String nombre, int id){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBPersona.Nombre, nombre);
        db.update(DBMan.DBPersona.TABLE_NAME, contentValues, "_id = ?", new String[]{""+id});
        updateLast();
    }
    public static Cursor getPersonas(){
        return db.rawQuery("SELECT AccountsPersonas._id as _id, AccountsPersonas.Nombre as Nombre, COUNT(AccountsPersonas.Nombre) as Count " +
                "FROM AccountsPersonas LEFT JOIN " +
                "AccountsPrestamos on AccountsPrestamos.IdPersona= AccountsPersonas._id and date('now','-1 month') <= date('now') " +
                "WHERE Active == 1 GROUP BY AccountsPersonas._id union \n" +
                " select -1 as _id, \"Otro\" as Nombre, (select coalesce(MAX(_id),1) + 1 From AccountsPersonas) as Count\n" +
                " order by Count" ,null);
    }
    public static void deshacesPrestamo(int id){
        Double cantidad;
        Cursor c = db.rawQuery("SELECT _id, " + DBMan.DBPrestamo.Cantidad +", " +
                DBMan.DBPrestamo.IdTotales + ", " + DBMan.DBPrestamo.Cambio + " FROM " +
                DBMan.DBPrestamo.TABLE_NAME + " WHERE _id = ?", new String[]{id+""});
        c.moveToFirst();
        String cambio = c.getString(c.getColumnIndex(DBMan.DBPrestamo.Cambio));
        cantidad = c.getDouble(c.getColumnIndex(DBMan.DBPrestamo.Cantidad));
        if(cambio != null) cantidad = cantidad * Double.parseDouble(cambio);
        int idCuenta = c.getInt(c.getColumnIndex(DBMan.DBPrestamo.IdTotales));
        Cursor cCuenta = (db.rawQuery("SELECT "+ DBMan.DBTotales.CantidadActual + " " +
                "FROM " + DBMan.DBTotales.TABLE_NAME + " WHERE _id = ?", new String[]{""+idCuenta}));
        cCuenta.moveToFirst();
        Double cantidadCuenta = cCuenta.getDouble(cCuenta.getColumnIndex(DBMan.DBTotales.CantidadActual));
        cantidad = cantidadCuenta - cantidad;
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBTotales.CantidadActual, cantidad);
        db.update(DBMan.DBTotales.TABLE_NAME, contentValues, "_id = ?", new String[]{""+idCuenta});
        updateLast();
    }
    public static void updatePrestamo(int id, double cant, double cambio, int idCuenta, int idMoneda, int idPersona, String comment, String fecha){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBPrestamo.Cantidad, cant);
        contentValues.put(DBMan.DBPrestamo.Cambio, cambio);
        contentValues.put(DBMan.DBPrestamo.IdTotales, idCuenta);
        contentValues.put(DBMan.DBPrestamo.IdMoneda, idMoneda);
        contentValues.put(DBMan.DBPrestamo.IdPersona, idPersona);
        contentValues.put(DBMan.DBPrestamo.Comment, comment);
        contentValues.put(DBMan.DBPrestamo.Fecha, fecha);
        db.update(DBMan.DBPrestamo.TABLE_NAME, contentValues, "_id = ?", new String[]{"" + id});
        updateLast();
    }
    public static String getPersonaNombreById(int id){
        Cursor c = db.rawQuery("SELECT " + DBMan.DBPersona.Nombre + " FROM " + DBMan.DBPersona.TABLE_NAME +
                " WHERE _id == " + id, null);
        c.moveToFirst();
        return c.getString(c.getColumnIndex(DBMan.DBPersona.Nombre));
    }

    //Prestamos
    public static boolean createPrestamo(double cant, int cuenta, int moneda, int persona, String desc, double cambio, long idMove){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBPrestamo.IdTotales, cuenta);
        contentValues.put(DBMan.DBPrestamo.Cantidad, cant);
        contentValues.put(DBMan.DBPrestamo.IdPersona, persona);
        contentValues.put(DBMan.DBPrestamo.IdMoneda, moneda);
        contentValues.put(DBMan.DBPrestamo.Comment, desc);
        contentValues.put(DBMan.DBPrestamo.Cambio, cambio);
        contentValues.put(DBMan.DBPrestamo.IdMovimiento, idMove);
        long a = db.insert(DBMan.DBPrestamo.TABLE_NAME,null,contentValues);
        if(a >= 0){
            updateLast();
            return true;
        } else{
            return false;
        }
    }

    public static void updatePrestamoFromMove(int id, double cantidad, double cambio, int idMoneda){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBPrestamo.Cantidad, cantidad);
        contentValues.put(DBMan.DBPrestamo.Cambio, cambio);
        contentValues.put(DBMan.DBPrestamo.IdMoneda, idMoneda);
        db.update(DBMan.DBPrestamo.TABLE_NAME, contentValues, "_id = ?", new String[]{"" + id});
        updateLast();
    }
    public static Cursor getPrestamosPlus(boolean isCeros){
        String s = " ";
        if(isCeros){
            s = " and Cantidad <> 0 ";
        }
        return db.rawQuery("select * from (\n" +
                "\tselect p._id, p.Fecha, AccountsTotales.Cuenta, AccountsMoneda.Moneda, Comment, AccountsPersonas.Nombre, p.Cambio, p.IdMovimiento, p.Cerrada,\n" +
                "\t\t(p.Cantidad - coalesce(pd.Cantidad, 0)) as Cantidad from AccountsPrestamos as p\n" +
                "\tleft join(\n" +
                "\tselect _id, (SUM(Cantidad * Cambio)) as Cantidad, IdPrestamo from " + DBMan.DBPrestamoDetalle.TABLE_NAME +" group by IdPrestamo\n" +
                "\t) as pd on p._id = pd.IdPrestamo, AccountsPersonas, AccountsMoneda, AccountsTotales\n" +
                "\tWhere AccountsPersonas._id = p.IdPersona and AccountsMoneda._id = p.IdMoneda and AccountsTotales._id = p.IdTotales \n" +
                ") WHERE IdMovimiento = 0" + s + "order by Fecha desc", null);
    }
    public static Cursor getPrestamosMinus(boolean isCeros){
        String s = " ";
        if(isCeros){
            s = " and Cantidad <> 0 ";
        }
        return db.rawQuery("select * from (\n" +
                "\tselect p._id, p.Fecha, AccountsTotales.Cuenta, AccountsMoneda.Moneda, Comment, AccountsPersonas.Nombre, " +
                "p.Cambio, p.IdMovimiento, p.Cerrada,\n" +
                "\t\t(p.Cantidad - coalesce(pd.Cantidad, 0)) as Cantidad from AccountsPrestamos as p\n" +
                "\tleft join(\n" +
                "\tselect _id, (SUM(Cantidad * Cambio)) as Cantidad, IdPrestamo from AccountsPrestamosDetalle group by IdPrestamo\n" +
                "\t) as pd on p._id = pd.IdPrestamo, AccountsPersonas, AccountsMoneda, AccountsTotales\n" +
                "\tWhere AccountsPersonas._id = p.IdPersona and AccountsMoneda._id = p.IdMoneda and AccountsTotales._id = p.IdTotales \n" +
                ") WHERE IdMovimiento <> 0" + s + "order by Fecha desc", null);
    }
    public static Cursor getPrestamosByPeople(boolean checked){
        String s = " ";
        if(checked){
            s = " and Cantidad <> 0 ";
        }
        return db.rawQuery("\n" +
                "SELECT AccountsPersonas._id, AccountsPersonas.Nombre, AccountsMoneda.Moneda, SUM(Cantidad * b) as Cantidad, Fecha FROM(\n" +
                "   SELECT *, (1) as b FROM(\n" +
                "   ( " +
                "       select (Cantidad - coalesce(CantidadMenos, 0)) as Cantidad, table1.IdPersona, table1.IdMoneda, table1.Fecha  From(\n" +
                "           (select sum(Cantidad) as Cantidad, IdPersona, IdMoneda, Fecha from AccountsPrestamos where IdMovimiento = 0 group by IdPersona, IdMoneda) as table1 " +
                "           left join (\n" +
                "\t\t\tselect sum(pd.Cantidad * pd.Cambio) as CantidadMenos, p.IdPersona, p.IdMoneda\n" +
                "\t\t\tfrom AccountsPrestamos as p, AccountsPrestamosDetalle as pd \n" +
                "\t\t\twhere p._id = pd.IdPrestamo and p.IdMovimiento = 0\n" +
                "\t\t\tgroup by p.IdPersona, p.IdMoneda) as table2 on table1.IdPersona = table2.IdPersona and table1.IdMoneda = table2.IdMoneda)\n" +
                "\t\t) as table1\n" +
                "\t)\n" +
                "\tunion all\n" +
                "\tSELECT *, (1)/*(-1)*/ as b FROM(\n" +
                "\t\t(\n" +
                "\t\tselect (Cantidad - coalesce(CantidadMenos, 0)) as Cantidad, table1.IdPersona, table1.IdMoneda, table1.Fecha  From(\n" +
                "\t\t\t(select sum(Cantidad) as Cantidad, IdPersona, IdMoneda, Fecha from AccountsPrestamos where IdMovimiento <> 0 group by IdPersona, IdMoneda) as table1\n" +
                "\t\t\tleft join (\n" +
                "\t\t\tselect sum(pd.Cantidad * pd.Cambio) as CantidadMenos, p.IdPersona, p.IdMoneda\n" +
                "\t\t\tfrom AccountsPrestamos as p, AccountsPrestamosDetalle as pd \n" +
                "\t\t\twhere p._id = pd.IdPrestamo and p.IdMovimiento <> 0\n" +
                "\t\t\tgroup by p.IdPersona, p.IdMoneda) as table2 on table1.IdPersona = table2.IdPersona and table1.IdMoneda = table2.IdMoneda)\n" +
                "\t\t) as table2\n" +
                "\t)\n" +
                "\t), AccountsPersonas, AccountsMoneda \n" +
                "\twhere IdPersona = AccountsPersonas._id and IdMoneda = AccountsMoneda._id" + s + "group by IdPersona, IdMoneda order by Fecha desc", null);
    }
    public static String getNombrePrestamoById(int id){
        try {
            Cursor c = db.rawQuery("SELECT " + DBMan.DBPersona.Nombre + " FROM " + DBMan.DBPersona.TABLE_NAME +
                    " WHERE _id == " + id, null);
            c.moveToFirst();
            String nombre = c.getString(c.getColumnIndex(DBMan.DBPersona.Nombre));
            return nombre;
        } catch (Exception e){
            return "Not find";
        }
    }
    public static Cursor getCursorPrestamo(int id){
        Cursor c = db.rawQuery("SELECT * FROM AccountsPrestamos Where _id == ?", new String[]{id +""});
        c.moveToFirst();
        return c;
    }
    public static boolean insertPrestamoDetalle(double cantidad, int idTotales, int idMoneda, int idPrestamo, double cambio){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBPrestamoDetalle.IdTotales, idTotales);
        contentValues.put(DBMan.DBPrestamoDetalle.Cantidad, cantidad);
        contentValues.put(DBMan.DBPrestamoDetalle.Cambio, cambio);
        contentValues.put(DBMan.DBPrestamoDetalle.IdMoneda, idMoneda);
        contentValues.put(DBMan.DBPrestamoDetalle.IdPrestamo, idPrestamo);
        long a = db.insert(DBMan.DBPrestamoDetalle.TABLE_NAME,null,contentValues);
        if(a >= 0){
            updateLast();
            return true;
        } else{
            return false;
        }
    }
    public static void updatePrestamoDetalle(double cantidad, int idTotales, int idMoneda, int idPrestamo, double cambio, int _id){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBPrestamoDetalle.Cantidad, cantidad);
        contentValues.put(DBMan.DBPrestamoDetalle.IdTotales, idTotales);
        contentValues.put(DBMan.DBPrestamoDetalle.IdMoneda, idMoneda);
        contentValues.put(DBMan.DBPrestamoDetalle.IdPrestamo, idPrestamo);
        contentValues.put(DBMan.DBPrestamoDetalle.Cambio, cambio);
        db.update(DBMan.DBPrestamoDetalle.TABLE_NAME, contentValues, "_id = ?", new String[]{_id+""});
        updateLast();
    }
    public static void CerrarPrestamo(int id){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBPrestamo.Cerrada, 1);
        db.update(DBMan.DBPrestamo.TABLE_NAME, contentValues, "_id = ?", new String[]{id+""});
        updateLast();
    }
    public static double getSumPrestamoDetalle(int idPrestamo){
        Cursor c = db.rawQuery("select sum(Cantidad * Cambio) as Cantidad from AccountsPrestamosDetalle Where IdPrestamo = ? group by IdPrestamo", new String[]{idPrestamo+""});
        c.moveToFirst();
        return c.getDouble(c.getColumnIndex(DBMan.DBPrestamoDetalle.Cantidad));
    }
    public static Cursor getPrestamoDetalle(int idPrestamo){
        return db.rawQuery("SELECT * FROM " + DBMan.DBPrestamoDetalle.TABLE_NAME + " WHERE " + DBMan.DBPrestamoDetalle.IdPrestamo + " == " + idPrestamo, null);
    }

    //Config
    public static String getLastSync(){
        String [] columns = {DBMan.DBConfig.Value};
        String where = "_id = ?";
        String whereArgs[] = {DBMan.DBConfig.LastSync +""};
        Cursor c = db.query(DBMan.DBConfig.TABLE_NAME, columns, where, whereArgs,null, null, null);
        if(c.getCount() <= 0){
            return null;
        }
        c.moveToFirst();
        return c.getString(c.getColumnIndex(DBMan.DBConfig.Value));
    }

    public static Cursor getAllTables(){
        String table = "sqlite_master";
        String columns[] = {"name"};
        String where = "type = ? and name like ?";
        String whereArgs[] = {"table", "Accounts%"};
        return db.query(table, columns, where, whereArgs, null, null, null);
    }

    public static String getTableAsJsonString(String table) throws JSONException{

        Cursor c = db.query(table, new String[]{"*"}, null, null, null, null, null );
        return cursorToString(c);
    }

    public static void deleteTables(){
        Log.d("Accoun", "DeleteTables");
        Cursor c = getAllTables();
        while(c.moveToNext()){
            String name = c.getString(c.getColumnIndex("name"));
            Log.d("Accoun ", "delete: " + name);
            db.delete(name, null, null);
        }
    }

    public static long insertIntoTable(String table, String[] columns, String[] values){
        ContentValues contentValues = new ContentValues();
        for(int i = 0; i < columns.length; i++){
            contentValues.put(columns[i], values[i]);
        }long a = db.insert(table, null,contentValues);
        return a;
    }

    public static void updateLast(){
        Timestamp ts = new Timestamp((new Date()).getTime());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String s = simpleDateFormat.format(ts);
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBConfig.Value, s);
        Log.d("Accoun ", ts + " ---- " + s);
        db.update(DBMan.DBConfig.TABLE_NAME, contentValues, "_id = ? or _id = ?", new String[]{DBMan.DBConfig.LastSync+"", DBMan.DBConfig.LastUpdated+""});
    }

    public static void setOnlyWifi(boolean x){
        int b = 0;
        if(x){
            b = 1;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBConfig.Value, b);
        db.update(DBMan.DBConfig.TABLE_NAME,contentValues, "_id = ?", new String[]{""+ DBMan.DBConfig.Wifi});
        //updateLast();
    }

    public static boolean getOnlyWifi(){
        Cursor c = db.query(DBMan.DBConfig.TABLE_NAME, new String[]{DBMan.DBConfig.Value}, "_id = ?", new String[]{DBMan.DBConfig.Wifi+""}, null, null, null);
        c.moveToFirst();
        if(c.getCount() == 0){
            return false;
        }
        int x = c.getInt(c.getColumnIndex(DBMan.DBConfig.Value));
        Log.d("Accoun", "Wifi " + x);
        if(x == 1)
            return true;
        return false;
    }
    public static boolean isWifiAvailable(Context context) {
        boolean haveConnectedWifi = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
        }
        return haveConnectedWifi;
    }

    public static boolean haveNetworkConnection(Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    private static String encrypt(){
        //Cipher cipher = Cipher.getInstance("AES");
        return "";
    }
    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }



    public static boolean hasPermissions(Context context, String... allPermissionNeeded)
    {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && context != null && allPermissionNeeded != null)
            for (String permission : allPermissionNeeded)
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                    return false;
        return true;
    }

    private static String cursorToString(Cursor crs) throws JSONException{
        JSONArray arr = new JSONArray();
        crs.moveToFirst();
        while (!crs.isAfterLast()) {
            int nColumns = crs.getColumnCount();
            JSONObject row = new JSONObject();
            for (int i = 0 ; i < nColumns ; i++) {
                String colName = crs.getColumnName(i);
                if (colName != null) {
                    switch (crs.getType(i)) {
                        case Cursor.FIELD_TYPE_BLOB   : row.put(colName, crs.getBlob(i).toString()); break;
                        case Cursor.FIELD_TYPE_FLOAT  : row.put(colName, crs.getDouble(i))         ; break;
                        case Cursor.FIELD_TYPE_INTEGER: row.put(colName, crs.getLong(i))           ; break;
                        case Cursor.FIELD_TYPE_NULL   : row.put(colName, null)               ; break;
                        case Cursor.FIELD_TYPE_STRING : row.put(colName, crs.getString(i))         ; break;
                    }
                }
            }
            arr.put(row);
            if (!crs.moveToNext())
                break;
        }
        crs.close(); // close the cursor
        return arr.toString();
    }
}
/*
SELECT Moneda1.Moneda, Moneda2.Moneda, CambioMoneda.Tipo_de_cambio
FROM CambioMoneda
JOIN Moneda Moneda1 ON Moneda1._id = CambioMoneda.Moneda1
JOIN Moneda Moneda2 ON Moneda2._id = CambioMoneda.Moneda2
 */
