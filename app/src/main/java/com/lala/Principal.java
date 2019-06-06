package com.lala;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;

/**
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
        return db.rawQuery("SELECT * FROM Movimiento WHERE Fecha BETWEEN date('now', '-1 month') and date('now') " +
                "and (IdTotales = ? or Traspaso = ?) ORDER BY Fecha DESC, _id DESC", new String[]{""+id, ""+id});
    }
    public static Cursor getTotalMoves(int id, String year){
        return db.rawQuery("SELECT * FROM Movimiento WHERE strftime('%Y',Fecha) == ? " +
                "and (IdTotales = ? or Traspaso = ?) ORDER BY Fecha DESC, _id DESC", new String[]{year,""+id, ""+id});
    }

    public static Cursor getTotalMoves(int id, String month, String year){
        return db.rawQuery("SELECT * FROM Movimiento WHERE strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? " +
                "and (IdTotales = ? or Traspaso = ?) ORDER BY Fecha DESC, _id DESC", new String[]{year, month,""+id, ""+id});
    }
    public static Cursor getTotalCredit(){
        return db.rawQuery("SELECT * FROM Totales WHERE Activa == 1 and Tipo = 2",null);
    }
    public static Cursor getTotalWallet(){
        return db.rawQuery("SELECT * FROM Totales WHERE Tipo == 1 and Activa == 1",null);
    }
    public static Cursor getTotalCredit(int id){
        return db.rawQuery("SELECT * FROM Totales WHERE (Activa == 1 or _id == ?) and Tipo = 2",new String[]{id+""});
    }
    public static Cursor getTotalWallet(int id){
        return db.rawQuery("SELECT * FROM Totales WHERE Tipo == 1 and (Activa == 1 or _id == ?)",new String[]{id+""});
    }
    public static Cursor getTotal(int id){
        return db.rawQuery("SELECT * FROM Totales WHERE _id = ?",new String[]{""+id});
    }
    public static Cursor getTotales(){
        return db.rawQuery("SELECT Totales._id, Moneda.Moneda, Totales.Cuenta, COUNT(Totales.Cuenta) as Count , Totales.CurrentCantidad FROM Totales, Moneda LEFT JOIN " +
                "Movimiento on Movimiento.IdTotales= Totales._id and date('now','-1 month') <= date('now') " +
                "WHERE Activa == 1 and Totales.IdMoneda == Moneda._id and Totales._id > 20 GROUP BY Totales._id ORDER by Count DESC" ,null);
    }
    public static Cursor getTotalesWithPrestamo(){
        return db.rawQuery("SELECT Totales._id, Moneda.Moneda, Totales.Cuenta, COUNT(Totales.Cuenta) as Count , Totales.CurrentCantidad FROM Totales, Moneda LEFT JOIN " +
                "Movimiento on Movimiento.IdTotales= Totales._id and date('now','-1 month') <= date('now') " +
                "WHERE Activa == 1 and Totales.IdMoneda == Moneda._id and Totales._id > 20 GROUP BY Totales._id " +
                "union\n" +
                "SELECT Totales._id, Moneda.Moneda, Totales.Cuenta, (-1) as Count , Totales.CurrentCantidad FROM Totales, Moneda\n" +
                "where Totales.IdMoneda == Moneda._id and Totales._id == 1\n" +
                "ORDER by Count DESC",null);
    }
    public static Cursor getTotales(int id){
        return db.rawQuery("SELECT Totales._id, Moneda.Moneda, Totales.Cuenta, COUNT(Totales.Cuenta) as Count , Totales.CurrentCantidad FROM Totales, Moneda LEFT JOIN " +
                "Movimiento on Movimiento.IdTotales= Totales._id and date('now','-1 month') <= date('now') " +
                "WHERE (Activa == 1 or Totales._id == ?) and Totales.IdMoneda == Moneda._id GROUP BY Totales._id ORDER by Fecha DESC, Count DESC" ,new String[]{id+""});
    }
    public static void insertTotales(String cuenta, Double cantidad, int moneda){
        db.execSQL("INSERT INTO Totales(Cuenta,CantidadInicial,CurrentCantidad" +
                ",IdMoneda) VALUES ('"+cuenta+"','"+cantidad+"','"+cantidad+"','" +moneda+"')");
    }
    public static int getMonedaId(int id){
        Cursor c = db.rawQuery("SELECT IdMoneda FROM Totales " +
                "WHERE _id = ?", new String[]{""+id});
        c.moveToFirst();
        return c.getInt(c.getColumnIndex(DBMan.DBTotales.Moneda));
    }
    public static String getMonedaTotales(int cuenta){
        Cursor c = db.rawQuery("SELECT Moneda.Moneda as Moneda " +
                "FROM Moneda, Totales " +
                "WHERE Moneda._id = Totales.IdMoneda " +
                "and Totales._id = ?",new String[]{String.valueOf(cuenta)});
        c.moveToFirst();
        return c.getString(c.getColumnIndex("Moneda"));
    }
    public static int getIdMonedaTotales(int cuenta){
        Cursor c = db.rawQuery("SELECT IdMoneda " +
                "FROM Totales " +
                "WHERE Totales._id = ?",new String[]{String.valueOf(cuenta)});
        c.moveToFirst();
        return c.getInt(c.getColumnIndex("IdMoneda"));
    }
    public static String getCuentaTotales(int cuenta){
        Cursor c = db.rawQuery("SELECT Cuenta " +
                "FROM Totales WHERE " +
                "Totales._id = ?",new String[]{String.valueOf(cuenta)});
        c.moveToFirst();
        return c.getString(c.getColumnIndex("Cuenta"));
    }
    public static void newMoveCuenta(double cantidad, int id){
        Cursor c = db.rawQuery("SELECT CurrentCantidad FROM Totales WHERE _id = ?",new String[]{""+id});
        c.moveToFirst();
        Double priorCant = c.getDouble(c.getColumnIndex(DBMan.DBTotales.CantidadActual));
        db.execSQL("UPDATE Totales SET CurrentCantidad = " +(priorCant + cantidad)+ " WHERE _id = " + id);
    }
    public static void updateTotalesFromPrestamo(double cant, int idCuenta){
        Cursor c = db.rawQuery("SELECT " + DBMan.DBTotales.CantidadActual + " FROM " + DBMan.DBTotales.TABLE_NAME +
                " WHERE _id == " + idCuenta, null);
        c.moveToFirst();
        double can = c.getDouble(c.getColumnIndex(DBMan.DBTotales.CantidadActual));
        db.execSQL("UPDATE " + DBMan.DBTotales.TABLE_NAME + " SET " + DBMan.DBTotales.CantidadActual + " = " + (can + cant) +
                " WHERE _id = " +idCuenta);
    }
    //Movimientos
    public static Cursor getMovimientos() {
        return db.rawQuery("SELECT * FROM Movimiento WHERE strftime('%Y',Fecha) = strftime('%Y', date('now'))and " +
                "strftime('%m',Fecha) = strftime('%m',date('now')) and strftime('%d',Fecha) >= 1 and " +
                "Traspaso is null ORDER BY Fecha DESC, _id DESC", null);

        //return db.rawQuery("SELECT * FROM Movimiento WHERE date('now','-1 month') <= date('now') ORDER BY Fecha",null);
    }
    public static long newMove(Double cantidad, int cuenta, String comment,String motivo, String moneda, double cambio){


        ContentValues contentValues = new ContentValues();
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
                " FROM Movimiento WHERE _id = ?",new String[]{""+id});
    }
    public static void eliminarMov(int id) {
        deshacerMov(id);
        db.execSQL("DELETE FROM " + DBMan.DBMovimientos.TABLE_NAME + " WHERE _id = " + id);
    }
    public static void eliminarTras(int id) {
        deshacerTras(id);
        db.execSQL("DELETE FROM " + DBMan.DBMovimientos.TABLE_NAME + " WHERE _id = " + id);
    }
    public static void actualizarMovimiento(int id, Double cantidad, int cuenta, String comment,int motivo, int moneda, double cambio, String date){
        deshacerMov(id);
        String sCambio = null;
        if(cambio != -1.0) sCambio = cambio + "";
        db.execSQL("UPDATE " + DBMan.DBMovimientos.TABLE_NAME + " SET " +
                DBMan.DBMovimientos.Cantidad + " = " + cantidad +", " + DBMan.DBMovimientos.IdTotales + "="+cuenta+
                ", " + DBMan.DBMovimientos.IdMotivo + " = " + motivo + ", " + DBMan.DBMovimientos.IdMoneda + " = " + moneda+ ", " +
                DBMan.DBMovimientos.Cambio + " = " + sCambio+ ", " + DBMan.DBMovimientos.Fecha + " = date('" + date + "'), " +
                DBMan.DBMovimientos.Comment+ " = '"+ comment+"' WHERE _id = " + id);
        Cursor c = db.rawQuery("SELECT " + DBMan.DBTotales.CantidadActual + " FROM " + DBMan.DBTotales.TABLE_NAME + " WHERE _id = ?", new String[]{cuenta+""});
        c.moveToFirst();
        if(cambio == -1.0) cantidad = cantidad + c.getDouble(c.getColumnIndex(DBMan.DBTotales.CantidadActual));
        else cantidad = (cantidad*cambio) + c.getDouble(c.getColumnIndex(DBMan.DBTotales.CantidadActual));
        db.execSQL("UPDATE " + DBMan.DBTotales.TABLE_NAME + " SET " + DBMan.DBTotales.CantidadActual +" = " + cantidad + " WHERE _id = " + cuenta);
    }

    public static Double getIngresoTotal(int Moneda){
        Cursor c = db.rawQuery("SELECT SUM(Ingreso) as Ingreso FROM (\n" +
                "  SELECT sum(Cantidad ) as Ingreso FROM Movimiento WHERE Cantidad > 0 and IdMoneda == ? and strftime('%Y',Fecha) == strftime('%Y', date('now'))and \n" +
                "  strftime('%m',Fecha) == strftime('%m',date('now')) and strftime('%d',Fecha) >= 1 and strftime('%d',Fecha) <= strftime('%d',date('now'))\n" +
                "union\n" +
                "\n" +
                "SELECT SUM( CASE WHEN (SELECT Totales.idMoneda FROM Totales, Movimiento WHERE Totales._id == IdTotales and Cambio > 0) <> Movimiento.IdMoneda\n" +
                "                then Cantidad * -1 end) as Ingreso FROM Movimiento WHERE Cantidad < 0 and strftime('%Y',Fecha) == strftime('%Y', date('now')) and strftime('%m',Fecha) == strftime('%m',date('now')) and strftime('%d',Fecha) >= 1 and strftime('%d',Fecha) <= strftime('%d',date('now')) and IdMoneda == ? and Cambio IS NOT NULL\n" +
                "union\n" +
                "SELECT  SUM(Cantidad) as Ingreso From Totales, Movimiento WHERE IdMotivo == 3 and Traspaso == Totales._id and Totales.IdMoneda == ? and strftime('%Y',Fecha) == strftime('%Y', date('now')) and strftime('%m',Fecha) == strftime('%m',date('now')) and strftime('%d',Fecha) >= 1 and strftime('%d',Fecha) <= strftime('%d',date('now'))\n" +
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
                "  SELECT sum(Cantidad ) as Ingreso FROM Movimiento WHERE Cantidad > 0 and IdMoneda == ? and strftime('%Y',Fecha) == \"" + year +"\" and \n" +
                "  strftime('%m',Fecha) == \"" + month + "\" \n" +
                "union\n" +
                "\n" +
                "SELECT SUM( CASE WHEN (SELECT Totales.idMoneda FROM Totales, Movimiento WHERE Totales._id == IdTotales and Cambio > 0) <> Movimiento.IdMoneda\n" +
                "                then Cantidad * -1 end) as Ingreso FROM Movimiento WHERE Cantidad < 0 and strftime('%Y',Fecha) == \"" + year + "\" and strftime('%m',Fecha) == \"" + month + "\" and IdMoneda == ? and Cambio IS NOT NULL\n" +
                "union\n" +
                "SELECT  SUM(Cantidad) as Ingreso From Totales, Movimiento WHERE IdMotivo == 3 and Traspaso == Totales._id and Totales.IdMoneda == ? and strftime('%Y',Fecha) == \"" + year +"\" and strftime('%m',Fecha) == \"" + month + "\"\n" +
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
                "  SELECT sum(Cantidad ) as Ingreso FROM Movimiento WHERE Cantidad > 0 and IdMoneda == ? and strftime('%Y',Fecha) == \"" + year +"\" \n" +
                "union\n" +
                "\n" +
                "SELECT SUM( CASE WHEN (SELECT Totales.idMoneda FROM Totales, Movimiento WHERE Totales._id == IdTotales and Cambio > 0) <> Movimiento.IdMoneda\n" +
                "                then Cantidad * -1 end) as Ingreso FROM Movimiento WHERE Cantidad < 0 and strftime('%Y',Fecha) == \"" + year + "\"  and IdMoneda == ? and Cambio IS NOT NULL\n" +
                "union\n" +
                "SELECT  SUM(Cantidad) as Ingreso From Totales, Movimiento WHERE IdMotivo == 3 and Traspaso == Totales._id and Totales.IdMoneda == ? and strftime('%Y',Fecha) == \"" + year +"\"\n" +
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
                "SELECT sum(Cantidad ) as Gasto FROM Movimiento WHERE Cantidad < 0 and IdMoneda == ? and strftime('%Y',Fecha) == strftime('%Y', date('now'))and \n" +
                "strftime('%m',Fecha) == strftime('%m',date('now')) and strftime('%d',Fecha) >= 1 and strftime('%d',Fecha) <= strftime('%d',date('now'))\n" +
                "union\n" + "SELECT SUM( CASE WHEN (SELECT Totales.idMoneda FROM Totales, Movimiento WHERE Totales._id == IdTotales and Cambio > 0) == ?\n" +
                " then Cantidad * Cambio end) FROM Movimiento WHERE Cantidad < 0 and strftime('%Y',Fecha) == strftime('%Y', date('now')) and strftime('%m',Fecha)" +
                " == strftime('%m',date('now')) and strftime('%d',Fecha) >= 1 and strftime('%d',Fecha) <= strftime('%d',date('now'))\n" +
                "union\n" + "SELECT SUM (CASE WHEN idMotivo == 3 and (SELECT Totales.idMoneda FROM Totales, Movimiento WHERE Totales._id == IdTotales) == ? THEN" +
                " Cantidad * Cambio * -1 end) FROM Movimiento WHERE strftime('%Y',Fecha) == strftime('%Y', date('now')) and strftime('%m',Fecha) == strftime('%m',date('now'))" +
                " and strftime('%d',Fecha) >= 1 and strftime('%d',Fecha) <= strftime('%d',date('now'))\n" + ")",new String[]{Moneda+"",Moneda+"",Moneda+""});
        c.moveToFirst();
        Double gasto;
        gasto = c.getDouble(c.getColumnIndex("Gasto"));

        return gasto;
    }
    public static Double getGastoTotalMonthly(int Moneda, String month, String year){
        Cursor c = db.rawQuery("SELECT SUM(Gasto) as Gasto FROM(\n" +
                "SELECT sum(Cantidad ) as Gasto FROM Movimiento WHERE Cantidad < 0 and IdMoneda == ? and strftime('%Y',Fecha) == \"" +year +"\" and \n" +
                "strftime('%m',Fecha) == \"" + month + "\"\n" +
                "union\n" + "SELECT SUM( CASE WHEN (SELECT Totales.idMoneda FROM Totales, Movimiento WHERE Totales._id == IdTotales and Cambio > 0) == ?\n" +
                " then Cantidad * Cambio end) FROM Movimiento WHERE Cantidad < 0 and strftime('%Y',Fecha) == \"" +year +"\" and strftime('%m',Fecha)" +
                " == \"" + month +"\"\n" +
                "union\n" + "SELECT SUM (CASE WHEN idMotivo == 3 and (SELECT Totales.idMoneda FROM Totales, Movimiento WHERE Totales._id == IdTotales) == ? THEN" +
                " Cantidad * Cambio * -1 end) FROM Movimiento WHERE strftime('%Y',Fecha) == \"" +year +"\" and strftime('%m',Fecha) == \"" + month +"\"" +
                " \n" + ")",new String[]{Moneda+"",Moneda+"",Moneda+""});
        c.moveToFirst();
        Double gasto;
        gasto = c.getDouble(c.getColumnIndex("Gasto"));

        return gasto;
    }
    public static Double getGastoTotalYearly(int Moneda, String year){
        Cursor c = db.rawQuery("SELECT SUM(Gasto) as Gasto FROM(\n" +
                "SELECT sum(Cantidad ) as Gasto FROM Movimiento WHERE Cantidad < 0 and IdMoneda == ? and strftime('%Y',Fecha) == \"" +year +"\"\n" +
                "union\n" + "SELECT SUM( CASE WHEN (SELECT Totales.idMoneda FROM Totales, Movimiento WHERE Totales._id == IdTotales and Cambio > 0) == ?\n" +
                " then Cantidad * Cambio end) FROM Movimiento WHERE Cantidad < 0 and strftime('%Y',Fecha) == \"" +year +"\"\n" +
                "union\n" + "SELECT SUM (CASE WHEN idMotivo == 3 and (SELECT Totales.idMoneda FROM Totales, Movimiento WHERE Totales._id == IdTotales) == ? THEN" +
                " Cantidad * Cambio * -1 end) FROM Movimiento WHERE strftime('%Y',Fecha) == \"" +year +"\")",new String[]{Moneda+"",Moneda+"",Moneda+""});
        c.moveToFirst();
        Double gasto;
        gasto = c.getDouble(c.getColumnIndex("Gasto"));

        return gasto;
    }

    public static Cursor getSumByMotivesMonth(int Moneda, String month, String year){
        return db.rawQuery("SELECT \n" +
                        "    Motivo._id as _id, SUM(Gasto) as Gasto, Ingreso , Motivo.Motivo as Motivo, (COALESCE(Ingreso,0) - COALESCE(SUM(Gasto),0)) as count1, (0) as isViaje\n" +
                        "    FROM(\n" +
                        "        SELECT \n" +
                        "            sum(Cantidad ) as Gasto, IdMotivo\n" +
                        "            FROM Movimiento \n" +
                        "            WHERE  IdMoneda == ? and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? and Cantidad < 0 GROUP BY IdMotivo\n" +
                        "        union\n" +
                        "        SELECT \n" +
                        "            SUM( CASE WHEN (\n" +
                        "                SELECT \n" +
                        "                    Totales.idMoneda\n" +
                        "                    FROM Totales, Movimiento \n" +
                        "                    WHERE Totales._id == IdTotales and Cambio > 0) == ? \n" +
                        "                then Cantidad * Cambio end) as Gasto,\n" +
                        "                IdMotivo\n" +
                        "            FROM Movimiento \n" +
                        "            WHERE Cantidad < 0 and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) ==  ?  GROUP BY IdMotivo\n" +
                        "        union\n" +
                        "        SELECT \n" +
                        "            SUM (CASE WHEN idMotivo == 3 and (\n" +
                        "                SELECT \n" +
                        "                    Totales.idMoneda\n" +
                        "                    FROM Totales, Movimiento \n" +
                        "                    WHERE Totales._id == IdTotales) == ? THEN\n" +
                        "                    Cantidad * Cambio * -1 end) as Gasto, \n" +
                        "                IdMotivo\n" +
                        "                FROM Movimiento \n" +
                        "                WHERE strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? GROUP BY IdMotivo) as table1 \n" +
                        "LEFT OUTER JOIN (\n" +
                        "SELECT \n" +
                        "    SUM(Ingreso) as Ingreso, IdMotivo2 \n" +
                        "    FROM (\n" +
                        "        SELECT \n" +
                        "            sum(Cantidad ) as Ingreso, IdMotivo as IdMotivo2\n" +
                        "            FROM Movimiento WHERE Cantidad > 0 and IdMoneda == ? and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? Group BY IdMotivo2\n" +
                        "        union\n" +
                        "        SELECT \n" +
                        "        SUM( CASE WHEN (\n" +
                        "            SELECT \n" +
                        "                Totales.idMoneda \n" +
                        "                FROM Totales, Movimiento \n" +
                        "                WHERE Totales._id == IdTotales and Cambio > 0) <> Movimiento.IdMoneda\n" +
                        "            then Cantidad * -1 end) as Ingreso, IdMotivo as IdMotivo2\n" +
                        "            FROM Movimiento \n" +
                        "            WHERE Cantidad < 0 and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? and IdMoneda == ? and Cambio IS NOT NULL Group BY IdMotivo2\n" +
                        "        union\n" +
                        "        SELECT  \n" +
                        "            SUM(Cantidad) as Ingreso, IdMotivo as IdMotivo2\n" +
                        "            From Totales, Movimiento \n" +
                        "            WHERE IdMotivo2 == 3 and Traspaso == Totales._id and Totales.IdMoneda == ? and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? Group BY IdMotivo ) as table3, Motivo \n" +
                        "    WHERE table3.IdMotivo2 == Motivo._id GROUP BY IdMotivo2\n" +
                        ") as table2 on table1.IdMotivo = table2.IdMotivo2 ,  Motivo WHERE table1.IdMotivo == Motivo._id and (Gasto IS NOT NULL or Ingreso IS NOT NULL) GROUP BY Motivo \n" +
                        "                        union\n" +
                        "                        SELECT \n" +
                        "                            Trips._id as _id, SUM(Gasto) as Gasto, Ingreso , Trips.Nombre as Motivo, (COALESCE(Ingreso,0) - COALESCE(SUM(Gasto),0)) as count1, (1) as isViaje\n" +
                        "                            FROM(\n" +
                        "                                SELECT \n" +
                        "                                    sum(Cantidad ) as Gasto, IdViaje\n" +
                        "                                    FROM Movimiento \n" +
                        "                                    WHERE  IdMoneda == ? and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) ==  ? and Cantidad < 0 GROUP BY IdViaje\n" +
                        "                                union\n" +
                        "                                SELECT \n" +
                        "                                    SUM( CASE WHEN (\n" +
                        "                                        SELECT \n" +
                        "                                            Totales.IdMoneda \n" +
                        "                                            FROM Totales, Movimiento \n" +
                        "                                            WHERE Totales._id == IdTotales and Cambio > 0) == ? \n" +
                        "                                    then Cantidad * Cambio end) as Gasto, IdViaje\n" +
                        "                                    FROM Movimiento \n" +
                        "                                    WHERE Cantidad < 0 and strftime('%Y',Fecha) ==  ? and strftime('%m',Fecha) ==  ? GROUP BY IdViaje\n" +
                        "                                ) as table1 \n" +
                        "                        LEFT OUTER JOIN (\n" +
                        "                        SELECT \n" +
                        "                            SUM(Ingreso) as Ingreso, IdViaje\n" +
                        "                            FROM (\n" +
                        "                                SELECT \n" +
                        "                                    sum(Cantidad ) as Ingreso, IdViaje\n" +
                        "                                    FROM Movimiento \n" +
                        "                                    WHERE Cantidad > 0 and IdMoneda == ? and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) ==  ?\n" +
                        "                                    Group BY IdViaje\n" +
                        "                                union\n" +
                        "                                SELECT SUM( CASE WHEN (\n" +
                        "                                    SELECT Totales.idMoneda \n" +
                        "                                    FROM Totales, Movimiento \n" +
                        "                                    WHERE Totales._id == IdTotales and Cambio > 0) <> Movimiento.IdMoneda\n" +
                        "                                then Cantidad * -1 end) as Ingreso, IdViaje\n" +
                        "                                FROM Movimiento \n" +
                        "                                WHERE Cantidad < 0 and IdMoneda == ? and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) ==  ? and Cambio IS NOT NULL Group BY IdViaje\n" +
                        "                                ) as table3, Trips\n" +
                        "                            WHERE table3.IdViaje == Trips._id GROUP BY IdViaje\n" +
                        "                        ) as table2 on table1.IdViaje = table2.IdViaje ,  Trips\n" +
                        "                        WHERE table1.IdViaje == Trips._id and (Gasto IS NOT NULL or Ingreso IS NOT NULL) GROUP BY Motivo ORDER BY count1 DESC",
                new String[]{Moneda+"", year, month, Moneda+"", year, month, Moneda+"", year, month, Moneda+"", year, month, Moneda+"", year, month, Moneda+"", year, month, Moneda+"", year, month, Moneda+"", year, month, Moneda+"", year, month});
    }
    public static Cursor getSumByMotivesYear(int Moneda, String year){
        return db.rawQuery("SELECT \n" +
                        "    Motivo._id as _id, SUM(Gasto) as Gasto, Ingreso , Motivo.Motivo as Motivo, (COALESCE(Ingreso,0) - COALESCE(SUM(Gasto),0)) as count1, (0) as isViaje\n" +
                        "    FROM(\n" +
                        "        SELECT \n" +
                        "            sum(Cantidad ) as Gasto, IdMotivo\n" +
                        "            FROM Movimiento \n" +
                        "            WHERE  IdMoneda == ? and strftime('%Y',Fecha) == ? and Cantidad < 0 GROUP BY IdMotivo\n" +
                        "        union\n" +
                        "        SELECT \n" +
                        "            SUM( CASE WHEN (\n" +
                        "                SELECT \n" +
                        "                    Totales.idMoneda\n" +
                        "                    FROM Totales, Movimiento \n" +
                        "                    WHERE Totales._id == IdTotales and Cambio > 0) == ? \n" +
                        "                then Cantidad * Cambio end) as Gasto,\n" +
                        "                IdMotivo\n" +
                        "            FROM Movimiento \n" +
                        "            WHERE Cantidad < 0 and strftime('%Y',Fecha) == ?  GROUP BY IdMotivo\n" +
                        "        union\n" +
                        "        SELECT \n" +
                        "            SUM (CASE WHEN idMotivo == 3 and (\n" +
                        "                SELECT \n" +
                        "                    Totales.idMoneda\n" +
                        "                    FROM Totales, Movimiento \n" +
                        "                    WHERE Totales._id == IdTotales) == ? THEN\n" +
                        "                    Cantidad * Cambio * -1 end) as Gasto, \n" +
                        "                IdMotivo\n" +
                        "                FROM Movimiento \n" +
                        "                WHERE strftime('%Y',Fecha) == ? GROUP BY IdMotivo) as table1 \n" +
                        "LEFT OUTER JOIN (\n" +
                        "SELECT \n" +
                        "    SUM(Ingreso) as Ingreso, IdMotivo2 \n" +
                        "    FROM (\n" +
                        "        SELECT \n" +
                        "            sum(Cantidad ) as Ingreso, IdMotivo as IdMotivo2\n" +
                        "            FROM Movimiento WHERE Cantidad > 0 and IdMoneda == ? and strftime('%Y',Fecha) == ? Group BY IdMotivo2\n" +
                        "        union\n" +
                        "        SELECT \n" +
                        "        SUM( CASE WHEN (\n" +
                        "            SELECT \n" +
                        "                Totales.idMoneda \n" +
                        "                FROM Totales, Movimiento \n" +
                        "                WHERE Totales._id == IdTotales and Cambio > 0) <> Movimiento.IdMoneda\n" +
                        "            then Cantidad * -1 end) as Ingreso, IdMotivo as IdMotivo2\n" +
                        "            FROM Movimiento \n" +
                        "            WHERE Cantidad < 0 and strftime('%Y',Fecha) == ? and IdMoneda == ? and Cambio IS NOT NULL Group BY IdMotivo2\n" +
                        "        union\n" +
                        "        SELECT  \n" +
                        "            SUM(Cantidad) as Ingreso, IdMotivo as IdMotivo2\n" +
                        "            From Totales, Movimiento \n" +
                        "            WHERE IdMotivo2 == 3 and Traspaso == Totales._id and Totales.IdMoneda == ? and strftime('%Y',Fecha) == ? Group BY IdMotivo ) as table3, Motivo \n" +
                        "    WHERE table3.IdMotivo2 == Motivo._id GROUP BY IdMotivo2\n" +
                        ") as table2 on table1.IdMotivo = table2.IdMotivo2 ,  Motivo WHERE table1.IdMotivo == Motivo._id and (Gasto IS NOT NULL or Ingreso IS NOT NULL) GROUP BY Motivo \n" +
                        "                        union\n" +
                        "                        SELECT \n" +
                        "                            Trips._id as _id, SUM(Gasto) as Gasto, Ingreso , Trips.Nombre as Motivo, (COALESCE(Ingreso,0) - COALESCE(SUM(Gasto),0)) as count1, (1) as isViaje\n" +
                        "                            FROM(\n" +
                        "                                SELECT \n" +
                        "                                    sum(Cantidad ) as Gasto, IdViaje\n" +
                        "                                    FROM Movimiento \n" +
                        "                                    WHERE  IdMoneda == ? and strftime('%Y',Fecha) == ? and Cantidad < 0 GROUP BY IdViaje\n" +
                        "                                union\n" +
                        "                                SELECT \n" +
                        "                                    SUM( CASE WHEN (\n" +
                        "                                        SELECT \n" +
                        "                                            Totales.IdMoneda \n" +
                        "                                            FROM Totales, Movimiento \n" +
                        "                                            WHERE Totales._id == IdTotales and Cambio > 0) == ? \n" +
                        "                                    then Cantidad * Cambio end) as Gasto, IdViaje\n" +
                        "                                    FROM Movimiento \n" +
                        "                                    WHERE Cantidad < 0 and strftime('%Y',Fecha) ==  ? GROUP BY IdViaje\n" +
                        "                                ) as table1 \n" +
                        "                        LEFT OUTER JOIN (\n" +
                        "                        SELECT \n" +
                        "                            SUM(Ingreso) as Ingreso, IdViaje\n" +
                        "                            FROM (\n" +
                        "                                SELECT \n" +
                        "                                    sum(Cantidad ) as Ingreso, IdViaje\n" +
                        "                                    FROM Movimiento \n" +
                        "                                    WHERE Cantidad > 0 and IdMoneda == ? and strftime('%Y',Fecha) == ?\n" +
                        "                                    Group BY IdViaje\n" +
                        "                                union\n" +
                        "                                SELECT SUM( CASE WHEN (\n" +
                        "                                    SELECT Totales.idMoneda \n" +
                        "                                    FROM Totales, Movimiento \n" +
                        "                                    WHERE Totales._id == IdTotales and Cambio > 0) <> Movimiento.IdMoneda\n" +
                        "                                then Cantidad * -1 end) as Ingreso, IdViaje\n" +
                        "                                FROM Movimiento \n" +
                        "                                WHERE Cantidad < 0 and IdMoneda == ? and strftime('%Y',Fecha) == ? and Cambio IS NOT NULL Group BY IdViaje\n" +
                        "                                ) as table3, Trips\n" +
                        "                            WHERE table3.IdViaje == Trips._id GROUP BY IdViaje\n" +
                        "                        ) as table2 on table1.IdViaje = table2.IdViaje ,  Trips\n" +
                        "                        WHERE table1.IdViaje == Trips._id and (Gasto IS NOT NULL or Ingreso IS NOT NULL) GROUP BY Motivo ORDER BY count1 DESC\n",
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
        db.execSQL("UPDATE " + DBMan.DBMovimientos.TABLE_NAME + " SET " +
                DBMan.DBMovimientos.Cantidad + " = " + cantidad + ", " + DBMan.DBMovimientos.Cambio + " = " + cambio +
                ", " + DBMan.DBMovimientos.IdMoneda + " = " + idMoneda + " WHERE _id = " + idMove);
    }
    public static Cursor getSumByMotive(int id, String month, String year){
        if(month == null){
            return db.rawQuery("SELECT Movimiento._id, cuenta, Cantidad, Comment, Fecha, Movimiento.IdMoneda FROM Movimiento, Totales \n" +
                    "WHERE idTotales = Totales._id and strftime('%Y',Fecha) == \""+ year+ "\" " +
                    "and Movimiento.idMotivo == " + id,null);
        }
        return db.rawQuery("SELECT Movimiento._id, cuenta, Cantidad, Comment, Fecha, Movimiento.IdMoneda FROM Movimiento, Totales \n" +
                "WHERE idTotales = Totales._id and strftime('%Y',Fecha) == \""+ year+ "\" and " +
                "strftime('%m',Fecha) == \"" + month +"\" and Movimiento.idMotivo == " + id ,null);
    }
    public static Cursor getTotalesCuentasByMonth(String month, String year){
        return db.rawQuery("select Totales._id, Totales.Cuenta, COALESCE(Gasto,0) as Gasto, COALESCE(Ingreso,0) as Ingreso\n" +
                "from(\n" +
                "\tSELECT t._id as idTotales, sum(Gasto) as Gasto \n" +
                "\tfrom(\n" +
                "\t\tSelect _id \n" +
                "\t\tfrom(\n" +
                "\t\t\tselect totales.*, Movimiento.* \n" +
                "\t\t\tfrom Totales\n" +
                "\t\t\tLEFT JOIN Movimiento\n" +
                "\t\t\t\ton Totales._id = Movimiento.IdTotales\n" +
                "\t\t\t\tWHERE ((strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ?))\n" +
                "\t\t\tGroup by idTotales\n" +
                "\t\tUnion all\n" +
                "\t\t\tSelect Totales.* , Movimiento.*\n" +
                "\t\t\tfrom Movimiento\n" +
                "\t\t\tLEFT JOIN Totales\n" +
                "\t\t\t\tOn Totales._id = Movimiento.IdTotales Where Totales.Activa\n" +
                "\t\t\tGroup by idTotales\n" +
                "\t\t) group by _id\n" +
                "\t) as t \n" +
                "\tLeft outer join (\n" +
                "\t\tSELECT (sum(CASE WHEN Cambio is not null then cantidad*Cambio else cantidad end )) as Gasto, idTotales \n" +
                "\t\tfrom Movimiento \n" +
                "\t\t\tWHERE Cantidad < 0 and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? \n" +
                "\t\tGroup by idTotales\n" +
                "\t\tunion\n" +
                "\t\tselect (-1*sum(CASE WHEN Cambio is not null and IdMotivo == 2 then cantidad*Cambio else cantidad end )) as Gasto, idTotales \n" +
                "\t\tfrom Movimiento \n" +
                "\t\t\tWHERE Traspaso is not null and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? \n" +
                "\t\tGroup by idTotales\n" +
                "\t) as table3 on t._id == table3.idTotales Group by t._id\n" +
                ") as table1\n" +
                "\tLEFT OUTER JOIN (\n" +
                "\tselect idTotales, sum(Ingreso) as Ingreso \n" +
                "\t\tfrom(\n" +
                "\t\t\tselect idTotales, sum(CASE WHEN Cambio is not null then cantidad*Cambio else cantidad end ) as Ingreso \n" +
                "\t\t\tfrom Movimiento \n" +
                "\t\t\t\tWHERE Cantidad > 0 and Traspaso is null and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? \n" +
                "\t\t\tGroup by idTotales\n" +
                "\t\tunion \n" +
                "\t\tselect  Traspaso as idTotales, sum(CASE WHEN Cambio is not null and IdMotivo <> 2 then cantidad*Cambio else cantidad end ) as Ingreso \n" +
                "\t\tfrom Movimiento \n" +
                "\t\t\tWHERE Traspaso is not null and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? \n" +
                "\t\tgroup by Traspaso\n" +
                "\t) Group by idTotales\n" +
                ") as table2 on table1.idTotales == table2.idTotales, Movimiento, Totales \n" +
                "\tWHERE (Totales._id == table1.idTotales or Totales._id == table2.idTotales) \n" +
                "group by Totales._id\n" +
                "\n",new String[]{year, month,year, month,year, month,year, month,year, month});
    }
    public static Cursor getTotalesCuentasByYear(String year){
        return db.rawQuery("select Totales._id, Totales.Cuenta, COALESCE(Gasto,0) as Gasto, COALESCE(Ingreso,0) as Ingreso\n" +
                "from(\n" +
                "\tSELECT t._id as idTotales, sum(Gasto) as Gasto \n" +
                "\tfrom(\n" +
                "\t\tSelect _id \n" +
                "\t\tfrom(\n" +
                "\t\t\tselect totales.*, Movimiento.* \n" +
                "\t\t\tfrom Totales\n" +
                "\t\t\tLEFT JOIN Movimiento\n" +
                "\t\t\t\ton Totales._id = Movimiento.IdTotales\n" +
                "\t\t\t\tWHERE strftime('%Y',Fecha) == ?\n" +
                "\t\t\tGroup by idTotales\n" +
                "\t\tUnion all\n" +
                "\t\t\tSelect Totales.* , Movimiento.*\n" +
                "\t\t\tfrom Movimiento\n" +
                "\t\t\tLEFT JOIN Totales\n" +
                "\t\t\t\tOn Totales._id = Movimiento.IdTotales Where Totales.Activa\n" +
                "\t\t\tGroup by idTotales\n" +
                "\t\t) group by _id\n" +
                "\t) as t \n" +
                "\tLeft outer join (\n" +
                "\t\tSELECT (sum(CASE WHEN Cambio is not null then cantidad*Cambio else cantidad end )) as Gasto, idTotales \n" +
                "\t\tfrom Movimiento \n" +
                "\t\t\tWHERE Cantidad < 0 and strftime('%Y',Fecha) == ? \n" +
                "\t\tGroup by idTotales\n" +
                "\t\tunion\n" +
                "\t\tselect (-1*sum(CASE WHEN Cambio is not null and IdMotivo == 2 then cantidad*Cambio else cantidad end )) as Gasto, idTotales \n" +
                "\t\tfrom Movimiento \n" +
                "\t\t\tWHERE Traspaso is not null and strftime('%Y',Fecha) == ? \n" +
                "\t\tGroup by idTotales\n" +
                "\t) as table3 on t._id == table3.idTotales Group by t._id\n" +
                ") as table1\n" +
                "\tLEFT OUTER JOIN (\n" +
                "\tselect idTotales, sum(Ingreso) as Ingreso \n" +
                "\t\tfrom(\n" +
                "\t\t\tselect idTotales, sum(CASE WHEN Cambio is not null then cantidad*Cambio else cantidad end ) as Ingreso \n" +
                "\t\t\tfrom Movimiento \n" +
                "\t\t\t\tWHERE Cantidad > 0 and Traspaso is null and strftime('%Y',Fecha) == ? \n" +
                "\t\t\tGroup by idTotales\n" +
                "\t\tunion \n" +
                "\t\tselect  Traspaso as idTotales, sum(CASE WHEN Cambio is not null and IdMotivo <> 2 then cantidad*Cambio else cantidad end ) as Ingreso \n" +
                "\t\tfrom Movimiento \n" +
                "\t\t\tWHERE Traspaso is not null and strftime('%Y',Fecha) == ? \n" +
                "\t\tgroup by Traspaso\n" +
                "\t) Group by idTotales\n" +
                ") as table2 on table1.idTotales == table2.idTotales, Movimiento, Totales \n" +
                "\tWHERE (Totales._id == table1.idTotales or Totales._id == table2.idTotales) \n" +
                "group by Totales._id\n" +
                "\n",new String[]{year, year, year, year, year});
    }
    //Motivos
    public static void insertMotive(String mot){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBMotivo.Motivo,mot);
        db.insert(DBMan.DBMotivo.TABLE_NAME,null, contentValues);
    }
    public static Cursor getMotive(){
        return db.rawQuery("SELECT Motivo._id, Motivo.Motivo, COUNT(Motivo.Motivo) as Cuenta FROM Motivo LEFT JOIN " +
                "Movimiento on Movimiento.IdMotivo = Motivo._id and date('now','-1 month') <= date('now') " +
                " WHERE Active == 1 GROUP BY Motivo._id ORDER by Fecha DESC, Cuenta DESC ",null);
    }
    public static Cursor getMotiveAll(){
        return db.rawQuery("SELECT Motivo._id, Motivo.Motivo, Motivo.Active, COUNT(Motivo.Motivo) as Cuenta FROM Motivo LEFT JOIN " +
                "Movimiento on Movimiento.IdMotivo = Motivo._id and date('now','-1 month') <= date('now') " +
                "WHERE Motivo._id > 15 GROUP BY Motivo._id ORDER by Active DESC, Fecha DESC, Cuenta DESC ",null);
    }
    public static Cursor getMotive(int id){
        return db.rawQuery("SELECT Motivo._id, Motivo.Motivo, COUNT(Motivo.Motivo) as Cuenta FROM Motivo LEFT JOIN " +
                "Movimiento on Movimiento.IdMotivo = Motivo._id and date('now','-1 month') <= date('now') " +
                "WHERE Active == 1 or Motivo._id == ? GROUP BY Motivo._id ORDER by Fecha DESC, Cuenta DESC ",new String[]{""+id});
    }
    public static int getMotiveId(String motivo){
        Cursor c = db.rawQuery("SELECT _id FROM Motivo WHERE Motivo = ?",new String[]{motivo});
        c.moveToFirst();
        return c.getInt(c.getColumnIndex("_id"));
    }
    public static String getMotiveId(int id){
        Cursor c = db.rawQuery("SELECT Motivo FROM Motivo WHERE _id = ?",new String[]{""+id});
        c.moveToFirst();
        return c.getString(c.getColumnIndex("Motivo"));

    }
    public static void updateActiveMotive(int act, int id){
        db.execSQL("UPDATE " + DBMan.DBMotivo.TABLE_NAME + " SET " +
                "Active = " + act + " WHERE _id = " + id);
    }
    public static void updateNameMotive(String motivo, int id){
        db.execSQL("UPDATE " + DBMan.DBMotivo.TABLE_NAME + " SET " +
                DBMan.DBMotivo.Motivo + " = '" + motivo + "' WHERE _id = " + id);
    }
    //Moneda
    public static Cursor getMoneda(){
        return db.rawQuery("SELECT Moneda._id, Moneda.Moneda, COUNT(Moneda.Moneda) as Cuenta FROM Moneda LEFT JOIN" +
                " Movimiento on Movimiento.IdMoneda = Moneda._id and date('now','-1 month') <= date('now') WHERE Active == 1" +
                " GROUP BY Moneda._id ORDER by Fecha DESC, Cuenta DESC ",null);
    }
    public static String getTipodeCambio(String MonedaFrom, String MonedaTo){
        int id1 = getIdMoneda(MonedaFrom);
        int id2 = getIdMoneda(MonedaTo);
        MonedaFrom = String.valueOf(id1);
        MonedaTo = String.valueOf(id2);
        Cursor c = db.rawQuery("SELECT CambioMoneda.Tipo_de_cambio " +
                "FROM CambioMoneda " +
                "JOIN Moneda Moneda1 ON Moneda1._id = CambioMoneda.IdMoneda1 " +
                "JOIN Moneda Moneda2 ON Moneda2._id = CambioMoneda.IdMoneda2 " +
                "WHERE CambioMoneda.IdMoneda1 = ? and CambioMoneda.IdMoneda2 = ?", new String[]{MonedaFrom,MonedaTo});
        c.moveToFirst();
        return c.getString(c.getColumnIndex("Tipo_de_cambio"));
    }
    public static String getTipodeCambio(int MonedaFrom, int MonedaTo){
        Cursor c = db.rawQuery("SELECT CambioMoneda.Tipo_de_cambio " +
                "FROM CambioMoneda " +
                "JOIN Moneda Moneda1 ON Moneda1._id = CambioMoneda.IdMoneda1 " +
                "JOIN Moneda Moneda2 ON Moneda2._id = CambioMoneda.IdMoneda2 " +
                "WHERE CambioMoneda.IdMoneda1 = ? and CambioMoneda.IdMoneda2 = ?", new String[]{MonedaFrom+"",MonedaTo+""});
        c.moveToFirst();
        return c.getString(c.getColumnIndex("Tipo_de_cambio"));
    }
    public static int getIdMoneda(String moneda){
        Cursor c = db.rawQuery("SELECT _id " +
                "FROM Moneda " +
                "WHERE Moneda = ?", new String[]{moneda});
        c.moveToFirst();
        return c.getInt(c.getColumnIndex("_id"));
    }
    public static String getIdMoneda(int moneda){
        Cursor c = db.rawQuery("SELECT Moneda " +
                "FROM Moneda " +
                "WHERE _id = ?", new String[]{""+moneda});
        c.moveToFirst();
        return c.getString(c.getColumnIndex("Moneda"));
    }
    public static void actualizarTipoDeCambio(String moneda1,String moneda2, double cambio){
        int id1 = getIdMoneda(moneda1);
        int id2 = getIdMoneda(moneda2);
        moneda1 = String.valueOf(id1);
        moneda2 = String.valueOf(id2);
        Cursor c = db.rawQuery("SELECT CambioMoneda._id " +
                "FROM CambioMoneda " +
                "JOIN Moneda Moneda1 ON Moneda1._id = CambioMoneda.IdMoneda1 " +
                "JOIN Moneda Moneda2 ON Moneda2._id = CambioMoneda.IdMoneda2 " +
                "WHERE CambioMoneda.IdMoneda1 = ? and CambioMoneda.IdMoneda2 = ?", new String[]{moneda1,moneda2});
        c.moveToFirst();
        db.execSQL("UPDATE CambioMoneda SET Tipo_de_cambio = " + cambio +" WHERE _id = " + c.getInt(c.getColumnIndex("_id")));
        c = db.rawQuery("SELECT CambioMoneda._id " +
                "FROM CambioMoneda " +
                "JOIN Moneda Moneda1 ON Moneda1._id = CambioMoneda.IdMoneda1 " +
                "JOIN Moneda Moneda2 ON Moneda2._id = CambioMoneda.IdMoneda2 " +
                "WHERE CambioMoneda.IdMoneda1 = ? and CambioMoneda.IdMoneda2 = ?", new String[]{moneda2,moneda1});
        c.moveToFirst();
        db.execSQL("UPDATE CambioMoneda SET Tipo_de_cambio = " + (1/cambio) +" WHERE _id = " + c.getInt(c.getColumnIndex("_id")));
    }

    public static void actualizarTipoDeCambio(int moneda1,int moneda2, double cambio){
        Cursor c = db.rawQuery("SELECT CambioMoneda._id " +
                "FROM CambioMoneda " +
                "JOIN Moneda Moneda1 ON Moneda1._id = CambioMoneda.IdMoneda1 " +
                "JOIN Moneda Moneda2 ON Moneda2._id = CambioMoneda.IdMoneda2 " +
                "WHERE CambioMoneda.IdMoneda1 = ? and CambioMoneda.IdMoneda2 = ?", new String[]{moneda1+"",moneda2+""});
        c.moveToFirst();
        db.execSQL("UPDATE CambioMoneda SET Tipo_de_cambio = " + cambio +" WHERE _id = " + c.getInt(c.getColumnIndex("_id")));
    }

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
    }

    public static void guardarMoneda(String moneda){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBMoneda.Moneda,moneda);
        db.insert(DBMan.DBMoneda.TABLE_NAME,null,contentValues);
        Cursor c = db.rawQuery("SELECT * FROM Moneda",null);
        int N = c.getCount();
        c.moveToLast();
        int idMonedaNueva = c.getInt(c.getColumnIndex("_id"));
        c.moveToPrevious();
        for(int i = 0; i < N-1; i++){
            newTipoCambio(idMonedaNueva,c.getInt(c.getColumnIndex("_id")));
            c.moveToPrevious();
        }
    }
    public static void actualizarTraspaso(int id,Double cantidad,int idFrom,int idTo,
                                          String comment,int motivo,Double cambio, String date){
        deshacerTras(id);
        String sCambio = null;

        if(Principal.getMonedaId(idFrom) != Principal.getMonedaId(idTo)) sCambio = cambio + "";
        Toast.makeText(context,idFrom + " = " + idTo,Toast.LENGTH_SHORT).show();
        db.execSQL("UPDATE " + DBMan.DBMovimientos.TABLE_NAME + " SET " +
                DBMan.DBMovimientos.Cantidad + " = " + cantidad +", " + DBMan.DBMovimientos.IdTotales + "="+idFrom +
                ", " + DBMan.DBMovimientos.IdMotivo + " = " + motivo + ", " + DBMan.DBMovimientos.Traspaso + "=" + idTo + ", " +
                DBMan.DBMovimientos.Cambio + " = " + sCambio + ", " + DBMan.DBMovimientos.Fecha + " = date('" + date + "'), " +
                DBMan.DBMovimientos.Comment + " = " + comment +"  WHERE _id = " + id);
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
        Toast.makeText(context,"Cantidad 1:  " + cantidad,Toast.LENGTH_SHORT).show();
        //if(cambio != null) cantidad = cantidad * Double.parseDouble(cambio);
        if(motivo == 1){
            Toast.makeText(context,"Motivo 1:  ",Toast.LENGTH_SHORT).show();
            Cursor cCuenta = (db.rawQuery("SELECT "+ DBMan.DBTotales.CantidadActual + " " +
                    "FROM " + DBMan.DBTotales.TABLE_NAME + " WHERE _id = ?", new String[]{""+idCuentaFrom}));
            cCuenta.moveToFirst();
            Double cantCuenta = cCuenta.getDouble(cCuenta.getColumnIndex(DBMan.DBTotales.CantidadActual));
            cantCuenta = cantCuenta + cantidad;
            db.execSQL("UPDATE " + DBMan.DBTotales.TABLE_NAME +" SET "+ DBMan.DBTotales.CantidadActual +
                    " = " + cantCuenta +" WHERE _id = " + idCuentaFrom);
            cCuenta = db.rawQuery("SELECT "+ DBMan.DBTotales.CantidadActual + " " +
                    "FROM " + DBMan.DBTotales.TABLE_NAME + " WHERE _id = ?", new String[]{""+idCuentaTo});
            cCuenta.moveToFirst();
            cantCuenta = cCuenta.getDouble(cCuenta.getColumnIndex(DBMan.DBTotales.CantidadActual));
            if(cambio != null) cantidad = cantidad * Double.parseDouble(cambio);
            cantCuenta = cantCuenta - cantidad;
            db.execSQL("UPDATE " + DBMan.DBTotales.TABLE_NAME +" SET "+ DBMan.DBTotales.CantidadActual +
                    " = " + cantCuenta +" WHERE _id = " + idCuentaTo);

        } else {
            Toast.makeText(context,"Motivo 2:  ",Toast.LENGTH_SHORT).show();
            Cursor cCuenta = (db.rawQuery("SELECT "+ DBMan.DBTotales.CantidadActual + " " +
                    "FROM " + DBMan.DBTotales.TABLE_NAME + " WHERE _id = ?", new String[]{""+idCuentaTo}));
            cCuenta.moveToFirst();
            Double cantCuenta = cCuenta.getDouble(cCuenta.getColumnIndex(DBMan.DBTotales.CantidadActual));
            Toast.makeText(context,"Cantidad cuenta: " +cantCuenta,Toast.LENGTH_LONG).show();
            Toast.makeText(context,"Cantidad 2:  " + cantCuenta + " - " + cantidad +" = " + (cantCuenta -cantidad),Toast.LENGTH_LONG).show();
            cantCuenta = cantCuenta - cantidad;
            db.execSQL("UPDATE " + DBMan.DBTotales.TABLE_NAME +" SET "+ DBMan.DBTotales.CantidadActual +
                    " = " + cantCuenta +" WHERE _id = " + idCuentaTo);
            cCuenta = db.rawQuery("SELECT "+ DBMan.DBTotales.CantidadActual + " " +
                    "FROM " + DBMan.DBTotales.TABLE_NAME + " WHERE _id = ?", new String[]{""+idCuentaFrom});
            cCuenta.moveToFirst();
            cantCuenta = cCuenta.getDouble(cCuenta.getColumnIndex(DBMan.DBTotales.CantidadActual));
            if (cambio!=null) cantidad = cantidad * Double.parseDouble(cambio);
            Toast.makeText(context,"Cantidad 3:  " + cantCuenta + " + " + cantidad +" = " + (cantCuenta + cantidad),Toast.LENGTH_LONG).show();
            cantCuenta = cantCuenta + cantidad;
            db.execSQL("UPDATE " + DBMan.DBTotales.TABLE_NAME +" SET "+ DBMan.DBTotales.CantidadActual +
                    " = " + cantCuenta +" WHERE _id = " + idCuentaFrom);
        }
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
        db.execSQL("UPDATE " + DBMan.DBTotales.TABLE_NAME +" SET "+ DBMan.DBTotales.CantidadActual +
                " = " + cantidad +" WHERE _id = " + idCuenta);
    }

    //GetFirst use date
    public static Calendar getFirstDate(){
        Cursor c = db.rawQuery("SELECT Fecha FROM Movimiento WHERE _id = 1", new String[]{});
        c.moveToFirst();
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
        return db.rawQuery("SELECT * FROM " + DBMan.DBViaje.TABLE_NAME + " order by " + DBMan.DBViaje.FechaCreacion +
                " desc", null);
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
            db.execSQL("UPDATE " + DBMan.DBMovimientos.TABLE_NAME + " SET " + DBMan.DBMovimientos.IdTrip +
                    " = ? " + " WHERE _id == ?", new String[]{idTrip+"", idMove+""});
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
                db.execSQL("UPDATE " + DBMan.DBViaje.TABLE_NAME + " SET " + DBMan.DBViaje.CantTotal +
                        " = " + (cantActual + cantidad) + " WHERE _id == " + idTrip);
            }
            return true;
        //} catch (Exception e){
        //    return false;
        //}
    }

    public static boolean updateTrip(int _id, String name, String descripcion, String fechaInic, String fechaFin, int moneda){
        db.execSQL("UPDATE " + DBMan.DBViaje.TABLE_NAME + " SET " + DBMan.DBViaje.Nombre + " = \"" + name +
                "\", " + DBMan.DBViaje.Descripcion + " = \"" + descripcion + "\", " + DBMan.DBViaje.FechaInicio + " = \"" + fechaInic +
                "\", " + DBMan.DBViaje.FechaFin + " = \"" + fechaFin + "\", " + DBMan.DBViaje.IdMoneda + " = \"" + moneda +
                "\" WHERE _id = " + _id);
        return true;
    }
    public static void addMoveToTrip(int idMove, int idTrip){
        db.execSQL("UPDATE " + DBMan.DBMovimientos.TABLE_NAME + " SET " + DBMan.DBMovimientos.IdTrip +
                " = " +idTrip + " WHERE _id == " + idMove);
    }


    //Personas
    public static Cursor getPeopleAll(){
        return db.rawQuery("SELECT Personas._id, Personas.Nombre, Personas.Active, COUNT(Personas.Nombre) as Cuenta FROM Personas LEFT JOIN " +
                "Prestamos on Prestamos.IdPersona = Personas._id and date('now','-1 month') <= date('now') " +
                " GROUP BY Personas._id ORDER by Active DESC, Fecha DESC, Cuenta DESC ",null);
    }
    public static long insertPersona(String persona){
        Cursor c = db.rawQuery("Select _id FROM " + DBMan.DBPersona.TABLE_NAME +
                " WHERE " + DBMan.DBPersona.Nombre + " == \"" + persona + "\"", null);
        if(c.getCount() > 0){
            c.moveToFirst();
            return c.getInt(c.getColumnIndex("_id"));
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBPersona.Nombre,persona);
        return (db.insert(DBMan.DBPersona.TABLE_NAME,null, contentValues));
    }
    public static void updateActivePeople(int act, int id){
        db.execSQL("UPDATE " + DBMan.DBPersona.TABLE_NAME + " SET " +
                "Active = " + act + " WHERE _id = " + id);
    }
    public static void updateNamePeople(String nombre, int id){
        db.execSQL("UPDATE " + DBMan.DBPersona.TABLE_NAME + " SET " +
                DBMan.DBPersona.Nombre + " = '" + nombre + "' WHERE _id = " + id);
    }
    public static Cursor getPersonas(){
        return db.rawQuery("SELECT Personas._id, Personas.Nombre, COUNT(Personas.Nombre) as Count FROM Personas LEFT JOIN " +
                "Prestamos on Prestamos.IdPersona= Personas._id and date('now','-1 month') <= date('now') " +
                "WHERE Active == 1 GROUP BY Personas._id union \n" +
                " select -1 as _id, \"Otro\" as Nombre, (select coalesce(MAX(_id),1) + 1 From Personas) as Count\n" +
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
        db.execSQL("UPDATE " + DBMan.DBTotales.TABLE_NAME +" SET "+ DBMan.DBTotales.CantidadActual +
                " = " + cantidad +" WHERE _id = " + idCuenta);
    }
    public static void updatePrestamo(int id, double cant, double cambio, int idCuenta, int idMoneda, int idPersona, String comment, String fecha){
        db.execSQL("UPDATE " + DBMan.DBPrestamo.TABLE_NAME + " SET " + DBMan.DBPrestamo.Cantidad +
                " = " + cant + ", " + DBMan.DBPrestamo.Cambio + " = " + cambio + ", " +
                DBMan.DBPrestamo.IdTotales + " = " + idCuenta + ", " + DBMan.DBPrestamo.IdMoneda + " = " + idMoneda +
                ", " + DBMan.DBPrestamo.IdPersona + " = " + idPersona + ", " + DBMan.DBPrestamo.Comment + " = \"" + comment + "\"" +
                ", " + DBMan.DBPrestamo.Fecha + " = \"" + fecha + "\"" + " WHERE _id == " + id);
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
            return true;
        } else{
            return false;
        }
    }

    public static void updatePrestamoFromMove(int id, double cantidad, double cambio, int idMoneda){
        db.execSQL("UPDATE " + DBMan.DBPrestamo.TABLE_NAME + " SET "
                + DBMan.DBPrestamo.Cantidad + " = " + cantidad + ", " + DBMan.DBPrestamo.Cambio +
                " = " + cambio + ", " + DBMan.DBPrestamo.IdMoneda + " = " + idMoneda + " WHERE _id == " + id);
    }
    public static Cursor getPrestamosPlus(boolean isCeros){
        String s = " ";
        if(isCeros){
            s = " and Cantidad <> 0 ";
        }
        return db.rawQuery("select * from (\n" +
                "\tselect p._id, p.Fecha, Totales.Cuenta, Moneda.Moneda, Comment, Personas.Nombre, p.Cambio, p.IdMovimiento, p.Cerrada,\n" +
                "\t\t(p.Cantidad - coalesce(pd.Cantidad, 0)) as Cantidad from Prestamos as p\n" +
                "\tleft join(\n" +
                "\tselect _id, (SUM(Cantidad * Cambio)) as Cantidad, IdPrestamo from PrestamosDetalle group by IdPrestamo\n" +
                "\t) as pd on p._id = pd.IdPrestamo, Personas, Moneda, Totales\n" +
                "\tWhere Personas._id = p.IdPersona and Moneda._id = p.IdMoneda and Totales._id = p.IdTotales \n" +
                ") WHERE IdMovimiento = 0" + s + "order by Fecha desc", null);
    }
    public static Cursor getPrestamosMinus(boolean isCeros){
        String s = " ";
        if(isCeros){
            s = " and Cantidad <> 0 ";
        }
        return db.rawQuery("select * from (\n" +
                "\tselect p._id, p.Fecha, Totales.Cuenta, Moneda.Moneda, Comment, Personas.Nombre, p.Cambio, p.IdMovimiento, p.Cerrada,\n" +
                "\t\t(p.Cantidad - coalesce(pd.Cantidad, 0)) as Cantidad from Prestamos as p\n" +
                "\tleft join(\n" +
                "\tselect _id, (SUM(Cantidad * Cambio)) as Cantidad, IdPrestamo from PrestamosDetalle group by IdPrestamo\n" +
                "\t) as pd on p._id = pd.IdPrestamo, Personas, Moneda, Totales\n" +
                "\tWhere Personas._id = p.IdPersona and Moneda._id = p.IdMoneda and Totales._id = p.IdTotales \n" +
                ") WHERE IdMovimiento <> 0" + s + "order by Fecha desc", null);
    }
    public static Cursor getPrestamosByPeople(boolean checked){
        String s = " ";
        if(checked){
            s = " and Cantidad <> 0 ";
        }
        return db.rawQuery("\n" +
                "SELECT Personas._id, Personas.Nombre, Moneda.Moneda, SUM(Cantidad * b) as Cantidad, Fecha FROM(\n" +
                "\tSELECT *, (1) as b FROM(\n" +
                "\t\t(\n" +
                "\t\tselect (Cantidad - coalesce(CantidadMenos, 0)) as Cantidad, table1.IdPersona, table1.IdMoneda, table1.Fecha  From(\n" +
                "\t\t\t(select sum(Cantidad) as Cantidad, IdPersona, IdMoneda, Fecha from Prestamos where IdMovimiento = 0 group by IdPersona, IdMoneda) as table1\n" +
                "\t\t\tleft join (\n" +
                "\t\t\tselect sum(pd.Cantidad * pd.Cambio) as CantidadMenos, p.IdPersona, p.IdMoneda\n" +
                "\t\t\tfrom Prestamos as p, PrestamosDetalle as pd \n" +
                "\t\t\twhere p._id = pd.IdPrestamo and p.IdMovimiento = 0\n" +
                "\t\t\tgroup by p.IdPersona, p.IdMoneda) as table2 on table1.IdPersona = table2.IdPersona and table1.IdMoneda = table2.IdMoneda)\n" +
                "\t\t) as table1\n" +
                "\t)\n" +
                "\tunion all\n" +
                "\tSELECT *, (-1) as b FROM(\n" +
                "\t\t(\n" +
                "\t\tselect (Cantidad - coalesce(CantidadMenos, 0)) as Cantidad, table1.IdPersona, table1.IdMoneda, table1.Fecha  From(\n" +
                "\t\t\t(select sum(Cantidad) as Cantidad, IdPersona, IdMoneda, Fecha from Prestamos where IdMovimiento <> 0 group by IdPersona, IdMoneda) as table1\n" +
                "\t\t\tleft join (\n" +
                "\t\t\tselect sum(pd.Cantidad * pd.Cambio) as CantidadMenos, p.IdPersona, p.IdMoneda\n" +
                "\t\t\tfrom Prestamos as p, PrestamosDetalle as pd \n" +
                "\t\t\twhere p._id = pd.IdPrestamo and p.IdMovimiento <> 0\n" +
                "\t\t\tgroup by p.IdPersona, p.IdMoneda) as table2 on table1.IdPersona = table2.IdPersona and table1.IdMoneda = table2.IdMoneda)\n" +
                "\t\t) as table2\n" +
                "\t)\n" +
                "\t), Personas, Moneda \n" +
                "\twhere IdPersona = Personas._id and IdMoneda = Moneda._id" + s + "group by IdPersona, IdMoneda order by Fecha desc", null);
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
        Cursor c = db.rawQuery("SELECT * FROM Prestamos Where _id == ?", new String[]{id +""});
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
            return true;
        } else{
            return false;
        }
    }
    public static void updatePrestamoDetalle(double cantidad, int idTotales, int idMoneda, int idPrestamo, double cambio, int _id){
        db.execSQL("UPDATE " + DBMan.DBPrestamoDetalle.TABLE_NAME + " SET " + DBMan.DBPrestamoDetalle.Cantidad + " = ?, " +
                DBMan.DBPrestamoDetalle.IdTotales + " = ?, " + DBMan.DBPrestamoDetalle.IdMoneda + " = ?, " + DBMan.DBPrestamoDetalle.IdPrestamo + " = ?, " +
                DBMan.DBPrestamoDetalle.Cambio + " = ? WHERE _id = ?", new String[]{cantidad+"", idTotales+"", idMoneda+"", idPrestamo+"", cambio+"", _id+""});
    }
    public static void CerrarPrestamo(int id){
        db.execSQL("UPDATE " + DBMan.DBPrestamo.TABLE_NAME + " SET " + DBMan.DBPrestamo.Cerrada + " = 1 WHERE _id = " + id);
    }
    public static double getSumPrestamoDetalle(int idPrestamo){
        Cursor c = db.rawQuery("select sum(Cantidad * Cambio) as Cantidad from PrestamosDetalle Where IdPrestamo = ? group by IdPrestamo", new String[]{idPrestamo+""});
        c.moveToFirst();
        return c.getDouble(c.getColumnIndex(DBMan.DBPrestamoDetalle.Cantidad));
    }
    public static Cursor getPrestamoDetalle(int idPrestamo){
        return db.rawQuery("SELECT * FROM " + DBMan.DBPrestamoDetalle.TABLE_NAME + " WHERE " + DBMan.DBPrestamoDetalle.IdPrestamo + " == " + idPrestamo, null);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
}
/*
SELECT Moneda1.Moneda, Moneda2.Moneda, CambioMoneda.Tipo_de_cambio
FROM CambioMoneda
JOIN Moneda Moneda1 ON Moneda1._id = CambioMoneda.Moneda1
JOIN Moneda Moneda2 ON Moneda2._id = CambioMoneda.Moneda2
 */
