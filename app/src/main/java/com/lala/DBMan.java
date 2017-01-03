package com.lala;

/**
 * Created by Juan on 07/02/2016.
 */
public class DBMan {

    public static final String Id = "_id";

    public static class DBTotales{
        public static final String TABLE_NAME = "Totales";
        public static final String Cuenta = "Cuenta";
        public static final String CantidadActual = "CurrentCantidad";
        public static final String CantidadInicial = "CantidadInicial";
        public static final String Moneda = "IdMoneda";
    }
    public static class DBMovimientos{
        public static final String TABLE_NAME = "Movimiento";
        public static final String Cantidad = "Cantidad";
        public static final String Fecha = "Fecha";
        public static final String IdTotales = "IdTotales";
        public static final String IdComment = "IdComment";
        public static final String IdMotivo = "IdMotivo";
        public static final String IdMoneda = "IdMoneda";
        public static final String Cambio = "Cambio";
        public static final String Traspaso = "Traspaso";
    }
    public static class DBComment{
        public static final String TABLE_NAME = "Comment";
        public static final String Comments = "Comment";
    }
    public static class DBMotivo{
        public static final String TABLE_NAME = "Motivo";
        public static final String Motivo = "Motivo";
    }
    public static class DBMoneda{
        public static final String TABLE_NAME = "Moneda";
        public static final String Moneda = "Moneda";
    }
    public static class DBCambioMoneda{
        public static final String TABLE_NAME = "CambioMoneda";
        public static final String Moneda1    = "IdMoneda1";
        public static final String Moneda2    = "IdMoneda2";
        public static final String Cambio     = "Tipo_de_cambio";
    }



}
