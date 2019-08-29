package com.lala;

/**
 * Created by Juan on 07/02/2016.
 */
public class DBMan {

    public static final String Id = "_id";

    public static class DBTotales{
        public static final String TABLE_NAME = "AccountsTotales";
        public static final String Cuenta = "Cuenta";
        public static final String CantidadActual = "CurrentCantidad";
        public static final String CantidadInicial = "CantidadInicial";
        public static final String Moneda = "IdMoneda";
        public static final String Activa = "Activa";
        public static final String Tipo = "Tipo";
    }
    public static class DBMovimientos{
        public static final String TABLE_NAME = "AccountsMovimiento";
        public static final String Cantidad = "Cantidad";
        public static final String Fecha = "Fecha";
        public static final String IdTotales = "IdTotales";
        public static final String Comment = "comment";
        public static final String IdMotivo = "IdMotivo";
        public static final String IdMoneda = "IdMoneda";
        public static final String Cambio = "Cambio";
        public static final String Traspaso = "Traspaso";
        public static final String IdTrip = "IdViaje";
    }
    public static class DBMotivo{
        public static final String TABLE_NAME = "AccountsMotivo";
        public static final String Motivo = "Motivo";
        public static final String Activo = "Active";
    }
    public static class DBMoneda{
        public static final String TABLE_NAME = "AccountsMoneda";
        public static final String Moneda = "Moneda";
        public static final String Activo = "Active";
    }
    public static class DBCambioMoneda{
        public static final String TABLE_NAME = "AccountsCambioMoneda";
        public static final String Moneda1    = "IdMoneda1";
        public static final String Moneda2    = "IdMoneda2";
        public static final String Cambio     = "Tipo_de_cambio";
    }
    public static class DBViaje{
        public static final String TABLE_NAME = "AccountsTrips";
        public static final String Nombre = "Nombre";
        public static final String Descripcion = "Descripcion";
        public static final String FechaCreacion = "FechaCreacion";
        public static final String FechaCierre = "FechaCierre";
        public static final String FechaInicio = "FechaInicio";
        public static final String FechaFin = "FechaFin";
        public static final String CantTotal = "Total";
        public static final String IdMoneda = "IdMoneda";
    }
    public static class DBPrestamo{
        public static final String TABLE_NAME = "AccountsPrestamos";
        public static final String Cantidad = "Cantidad";
        public static final String Fecha = "Fecha";
        public static final String IdTotales = "IdTotales";
        public static final String IdMoneda = "IdMoneda";
        public static final String Comment = "Comment";
        public static final String IdPersona = "IdPersona";
        public static final String Cambio = "Cambio";
        public static final String IdMovimiento = "IdMovimiento";
        public static final String Cerrada = "Cerrada";
    }
    public static final class DBTiposCuentas{
        public static final String TABLE_NAME = "AccountsTiposCuentas";
        public static final String Tipo = "Tipo";
    }
    public static class DBPrestamoDetalle{
        public static final String TABLE_NAME = "AccountsPrestamosDetalle";
        public static final String Cantidad = "Cantidad";
        public static final String Fecha = "Fecha";
        public static final String IdTotales = "IdTotales";
        public static final String IdMoneda = "IdMoneda";
        public static final String IdPrestamo = "IdPrestamo";
        public static final String Cambio = "Cambio";
    }
    public static class DBPersona{
        public static final String TABLE_NAME = "AccountsPersonas";
        public static final String Nombre = "Nombre";
        public static final String Activo = "Active";
    }

    public static class DBConfig{
        public static final String TABLE_NAME = "AccountsConfig";
        public static final String Key = "KeyCode";
        public static final String Value = "ValueCode";


        public static final int LastUpdated = 1;
        public static final int LastSync = 2;
        public static final int Wifi = 3;
    }
    /*
    public static class DBAndroidConfig{
        public static final String TABLE_NAME = "AndroidAccountConfig";
        public static final String KEY = "KeyCode";
        public static final String Value = "ValueCode";


        public static final int authKeyValue = 1;
    }
    */
}
