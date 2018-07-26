package com.lala;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
                "WHERE Activa == 1 and Totales.IdMoneda == Moneda._id GROUP BY Totales._id ORDER by Fecha DESC, Count DESC" ,null);
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

    //Movimientos
    public static Cursor getMovimientos() {
        return db.rawQuery("SELECT * FROM Movimiento WHERE strftime('%Y',Fecha) = strftime('%Y', date('now'))and " +
                "strftime('%m',Fecha) = strftime('%m',date('now')) and strftime('%d',Fecha) >= 1 and " +
                "Traspaso is null ORDER BY Fecha DESC, _id DESC", null);

        //return db.rawQuery("SELECT * FROM Movimiento WHERE date('now','-1 month') <= date('now') ORDER BY Fecha",null);
    }
    public static void newMove(Double cantidad, int cuenta, String comment,String motivo, String moneda, double cambio){


        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMan.DBMovimientos.Cantidad,cantidad);

        contentValues.put(DBMan.DBMovimientos.Comment, comment);
        contentValues.put(DBMan.DBMovimientos.IdMotivo,getMotiveId(motivo));
        contentValues.put("IdMoneda", getIdMoneda(moneda));
        contentValues.put("IdTotales", cuenta);
        if(cambio != -1) contentValues.put("Cambio", cambio);
        db.insert(DBMan.DBMovimientos.TABLE_NAME,null,contentValues);
    }
    public static Cursor getData(int id){
        return db.rawQuery("SELECT _id, Cantidad, (case strftime('%m', Fecha) when '01' then 'Jan'" +
                " when '02' then 'Feb' when '03' then 'Mar' when '04' then 'Apr' when '05' then 'May' " +
                "when '06' then 'Jun' when '07' then 'Jul' when '08' then 'Aug' when '09' then 'Sep' " +
                "when '10' then 'Oct' when '11' then 'Nov' when '12' then 'Dec' else '' " +
                "end ||'-'|| strftime('%d-%Y', Fecha))  as Fecha, Fecha as nFecha, " +
                "IdTotales, Comment, IdMotivo, IdMoneda, Cambio, Traspaso " +
                "FROM Movimiento WHERE _id = ?",new String[]{""+id});
    }
    public static void eliminarMov(int id){
        deshacerMov(id);
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
                DBMan.DBMovimientos.Comment+ " = "+ comment+" WHERE _id = " + id);
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
        return db.rawQuery("SELECT Motivo._id as _id, SUM(Gasto) as Gasto, Ingreso , Motivo.Motivo as Motivo, (COALESCE(Ingreso,0) - COALESCE(Gasto,0)) as count1 FROM(\n" +
                "                SELECT sum(Cantidad ) as Gasto, IdMotivo, COUNT(IdMotivo) as count1 FROM Movimiento WHERE  IdMoneda == ? and strftime('%Y',Fecha) ==  \"" + year + "\"  and \n" +
                "                strftime('%m',Fecha) == \"" + month + "\" and Cantidad < 0 GROUP BY IdMotivo\n" +
                "                union \n" +
                "SELECT SUM( CASE WHEN (SELECT Totales.idMoneda FROM Totales, Movimiento WHERE Totales._id == IdTotales and Cambio > 0) == ?\n" +
                "                 then Cantidad * Cambio end) as Gasto, IdMotivo, COUNT(IdMotivo) as count1 FROM Movimiento WHERE Cantidad < 0 and strftime('%Y',Fecha) == \"" + year + "\" and strftime('%m',Fecha) == \"" + month + "\" GROUP BY IdMotivo\n" +
                "                union \n" +
                "SELECT SUM (CASE WHEN idMotivo == 3 and (SELECT Totales.idMoneda FROM Totales, Movimiento WHERE Totales._id == IdTotales) == ? THEN\n" +
                "                 Cantidad * Cambio * -1 end) as Gasto, IdMotivo, COUNT(IdMotivo) as count1 FROM Movimiento WHERE strftime('%Y',Fecha) == \"" + year + "\" and strftime('%m',Fecha) == \"" + month + "\" GROUP BY IdMotivo) as table1 LEFT OUTER JOIN (\n" +
                "\n" +
                "SELECT SUM(Ingreso) as Ingreso, IdMotivo2 FROM (\n" +
                "                  SELECT sum(Cantidad ) as Ingreso, IdMotivo as IdMotivo2, COUNT(IdMotivo) as count1 FROM Movimiento WHERE Cantidad > 0 and IdMoneda == ? and strftime('%Y',Fecha) ==\"" + year + "\" and \n" +
                "                  strftime('%m',Fecha) == \"" + month + "\" Group BY IdMotivo2\n" +
                "                union\n" +
                "                SELECT SUM( CASE WHEN (SELECT Totales.idMoneda FROM Totales, Movimiento WHERE Totales._id == IdTotales and Cambio > 0) <> Movimiento.IdMoneda\n" +
                "                                then Cantidad * -1 end) as Ingreso, IdMotivo as IdMotivo2, COUNT(IdMotivo) as count1 FROM Movimiento WHERE Cantidad < 0 and strftime('%Y',Fecha) == \"" + year + "\" " +
                "and strftime('%m',Fecha) == \"" + month + "\" and IdMoneda == ? and Cambio IS NOT NULL Group BY IdMotivo2\n" +
                "union\n" +
                "                SELECT  SUM(Cantidad) as Ingreso, IdMotivo as IdMotivo2, COUNT(IdMotivo) as count1 From Totales, Movimiento WHERE IdMotivo2 == 3 and Traspaso == Totales._id and Totales.IdMoneda == ? " +
                "and strftime('%Y',Fecha) == \"" + year + "\"and strftime('%m',Fecha) == \"" + month + "\" Group BY IdMotivo ) as table3, Motivo WHERE table3.IdMotivo2 == Motivo._id GROUP BY IdMotivo2\n" +
                "\n" +
                ") as table2 on table1.IdMotivo = table2.IdMotivo2 ,  Motivo WHERE table1.IdMotivo == Motivo._id and (Gasto IS NOT NULL or Ingreso IS NOT NULL) GROUP BY Motivo ORDER BY count1 DESC",
                new String[]{Moneda+"", Moneda+"",Moneda+"",Moneda+"", Moneda+"",Moneda+""});
    }
    public static Cursor getSumByMotivesYear(int Moneda, String year){
        return db.rawQuery("SELECT Motivo._id as _id, SUM(Gasto) as Gasto, Ingreso , Motivo.Motivo as Motivo, (COALESCE(Ingreso,0) - COALESCE(Gasto,0)) as count1 FROM(\n" +
                        "                SELECT sum(Cantidad ) as Gasto, IdMotivo, COUNT(IdMotivo) as count1 FROM Movimiento WHERE  IdMoneda == ? and strftime('%Y',Fecha) ==  \"" + year + "\"  and \n" +
                        "                Cantidad < 0 GROUP BY IdMotivo\n" +
                        "                union \n" +
                        "SELECT SUM( CASE WHEN (SELECT Totales.idMoneda FROM Totales, Movimiento WHERE Totales._id == IdTotales and Cambio > 0) == ?\n" +
                        "                 then Cantidad * Cambio end) as Gasto, IdMotivo, COUNT(IdMotivo) as count1 FROM Movimiento WHERE Cantidad < 0 and strftime('%Y',Fecha) == \"" + year + "\" GROUP BY IdMotivo\n" +
                        "                union \n" +
                        "SELECT SUM (CASE WHEN idMotivo == 3 and (SELECT Totales.idMoneda FROM Totales, Movimiento WHERE Totales._id == IdTotales) == ? THEN\n" +
                        "                 Cantidad * Cambio * -1 end) as Gasto, IdMotivo, COUNT(IdMotivo) as count1 FROM Movimiento WHERE strftime('%Y',Fecha) == \"" + year + "\" GROUP BY IdMotivo) as table1 LEFT OUTER JOIN (\n" +
                        "\n" +
                        "SELECT SUM(Ingreso) as Ingreso, IdMotivo2 FROM (\n" +
                        "                  SELECT sum(Cantidad ) as Ingreso, IdMotivo as IdMotivo2, COUNT(IdMotivo) as count1 FROM Movimiento WHERE Cantidad > 0 and IdMoneda == ? and strftime('%Y',Fecha) ==\"" + year + "\"" +
                        "                  Group BY IdMotivo2\n" +
                        "                union\n" +
                        "                SELECT SUM( CASE WHEN (SELECT Totales.idMoneda FROM Totales, Movimiento WHERE Totales._id == IdTotales and Cambio > 0) <> Movimiento.IdMoneda\n" +
                        "                                then Cantidad * -1 end) as Ingreso, IdMotivo as IdMotivo2, COUNT(IdMotivo) as count1 FROM Movimiento WHERE Cantidad < 0 and strftime('%Y',Fecha) == \"" + year + "\" " +
                        "                  and IdMoneda == ? and Cambio IS NOT NULL Group BY IdMotivo2\n" +
                        "union\n" +
                        "                SELECT  SUM(Cantidad) as Ingreso, IdMotivo as IdMotivo2, COUNT(IdMotivo) as count1 From Totales, Movimiento WHERE IdMotivo2 == 3 and Traspaso == Totales._id and Totales.IdMoneda == ? " +
                        "and strftime('%Y',Fecha) == \"" + year + "\" Group BY IdMotivo ) as table3, Motivo WHERE table3.IdMotivo2 == Motivo._id GROUP BY IdMotivo2\n" +
                        "\n" +
                        ") as table2 on table1.IdMotivo = table2.IdMotivo2 ,  Motivo WHERE table1.IdMotivo == Motivo._id and (Gasto IS NOT NULL or Ingreso IS NOT NULL) GROUP BY Motivo ORDER BY count1 DESC",
                new String[]{Moneda+"", Moneda+"",Moneda+"",Moneda+"", Moneda+"",Moneda+""});
    }
    public static Cursor getSumByMotive(int id, String month, String year){
        if(month.equals("")){
            return db.rawQuery("SELECT Movimiento._id, cuenta, Cantidad, Comment, Fecha FROM Movimiento, Totales \n" +
                    "WHERE idTotales = Totales._id and strftime('%Y',Fecha) == \""+ year+ "\" " +
                    "and Movimiento.idMotivo == " + id,null);
        }
        return db.rawQuery("SELECT Movimiento._id, cuenta, Cantidad, Comment, Fecha FROM Movimiento, Totales \n" +
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
                "WHERE Motivo._id > 2 GROUP BY Motivo._id ORDER by Active DESC, Fecha DESC, Cuenta DESC ",null);
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
            cantCuenta = cantCuenta - cantidad;
            db.execSQL("UPDATE " + DBMan.DBTotales.TABLE_NAME +" SET "+ DBMan.DBTotales.CantidadActual +
                    " = " + cantCuenta +" WHERE _id = " + idCuentaFrom);
            cCuenta = db.rawQuery("SELECT "+ DBMan.DBTotales.CantidadActual + " " +
                    "FROM " + DBMan.DBTotales.TABLE_NAME + " WHERE _id = ?", new String[]{""+idCuentaTo});
            cCuenta.moveToFirst();
            cantCuenta = cCuenta.getDouble(cCuenta.getColumnIndex(DBMan.DBTotales.CantidadActual));
            if(cambio != null) cantidad = cantidad * Double.parseDouble(cambio);
            cantCuenta = cantCuenta + cantidad;
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

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
/*
SELECT Moneda1.Moneda, Moneda2.Moneda, CambioMoneda.Tipo_de_cambio
FROM CambioMoneda
JOIN Moneda Moneda1 ON Moneda1._id = CambioMoneda.Moneda1
JOIN Moneda Moneda2 ON Moneda2._id = CambioMoneda.Moneda2
 */
