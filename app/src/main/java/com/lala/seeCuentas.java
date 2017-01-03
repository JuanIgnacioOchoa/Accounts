package com.lala;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import java.text.NumberFormat;

public class seeCuentas extends AppCompatActivity {

    private TextView tvCurrent, tvInicial;
    private ListView listView;
    private NumberFormat instance;
    private Cursor cursorTotales, cursorMoves;
    private myAdapter adapter;
    private int id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_cuentas);
        Intent i = getIntent();
        id = i.getIntExtra("_id", 1);
        cursorTotales = Principal.getTotal(id);
        cursorTotales.moveToFirst();
        String title = cursorTotales.getString(cursorTotales.getColumnIndex(DBMan.DBTotales.Cuenta));
        this.setTitle(title+" "+Principal.getIdMoneda(cursorTotales.getInt(cursorTotales.getColumnIndex(DBMan.DBTotales.Moneda))));
        instance = NumberFormat.getInstance();
        instance.setMinimumFractionDigits(2);
        tvCurrent = (TextView) findViewById(R.id.see_acc_tvCurr);
        tvInicial = (TextView) findViewById(R.id.see_acc_tvIni);
        listView = (ListView) findViewById(R.id.listView_see_acc);


        cursorMoves = Principal.getTotalMoves(id);
        adapter = new myAdapter(getApplicationContext(),cursorMoves);
        tvCurrent.setText(instance.format(cursorTotales.getDouble(cursorTotales.getColumnIndex(DBMan.DBTotales.CantidadActual))));
        tvInicial.setText(instance.format(cursorTotales.getDouble(cursorTotales.getColumnIndex(DBMan.DBTotales.CantidadInicial))));
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int motivo = cursorMoves.getInt(cursorMoves.getColumnIndex(DBMan.DBMovimientos.IdMotivo));
                if(motivo==1){
                    Intent i = new Intent(getApplicationContext(),seeTraspaso.class);
                    i.putExtra("_id",cursorMoves.getInt(cursorMoves.getColumnIndex("_id")));
                    startActivity(i);
                }else if(motivo == 2){
                    Intent i = new Intent(getApplicationContext(),seeTraspaso.class);
                    i.putExtra("_id",cursorMoves.getInt(cursorMoves.getColumnIndex("_id")));
                    startActivity(i);
                }
                else {
                    Intent i = new Intent(getApplicationContext(), seeMove.class);
                    i.putExtra("id", cursorMoves.getInt(cursorMoves.getColumnIndex("_id")));
                    startActivity(i);
                }
            }
        });
    }
    @Override
    public void onResume(){
        super.onResume();
        cursorMoves = Principal.getTotalMoves(id);
        adapter.changeCursor(cursorMoves);
        cursorTotales = Principal.getTotal(id);
        cursorTotales.moveToFirst();
        tvCurrent.setText(instance.format(cursorTotales.getDouble(cursorTotales.getColumnIndex(DBMan.DBTotales.CantidadActual))));
    }
    public class myAdapter extends CursorAdapter {

        public myAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.list_see_cuentas, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // Find fields to populate in inflated template
            TextView tvId = (TextView) view.findViewById(R.id.lv_see_cuentas_id);
            TextView tvCantidad = (TextView) view.findViewById(R.id.lv_sse_cuentas_cant);
            TextView tvMotivo = (TextView) view.findViewById(R.id.lv_see_cuentas_motivo);
            TextView tvFecha = (TextView) view.findViewById(R.id.lv_see_cuentas_fecha);
            // Extract properties from cursor
            String idCursor = "" + (cursor.getString(cursor.getColumnIndex("_id")));
            Double cantidad = cursor.getDouble(cursor.getColumnIndex(DBMan.DBMovimientos.Cantidad));
            int idMotivo = cursor.getInt(cursor.getColumnIndex(DBMan.DBMovimientos.IdMotivo));

            String motivo;
            motivo = Principal.getMotiveId(idMotivo);
            //String motivo = Principal.getMotiveId(cursor.getInt(cursor.getColumnIndex(DBMan.DBMovimientos.IdMotivo)));
            String fecha = cursor.getString(cursor.getColumnIndex(DBMan.DBMovimientos.Fecha));
            String cambio = cursor.getString(cursor.getColumnIndex(DBMan.DBMovimientos.Cambio));
            //set
            tvId.setText(idCursor);
            tvFecha.setText(fecha);
            tvMotivo.setText(motivo);

            if(idMotivo > 2){
                if(cantidad > 0)  tvCantidad.setTextColor(Color.rgb(11, 79, 34));
                else tvCantidad.setTextColor(Color.RED);
                if(cambio != null) cantidad = cantidad * Double.parseDouble(cambio);
            } else if(idMotivo == 1){
                if(id == cursor.getInt(cursor.getColumnIndex(DBMan.DBMovimientos.Traspaso))){
                    tvCantidad.setTextColor(Color.rgb(11, 79, 34));
                    if(cambio != null) cantidad = cantidad * Double.parseDouble(cambio);
                }
                else tvCantidad.setTextColor(Color.RED);
            } else { //IdMotivo == 2
                if(id == cursor.getInt(cursor.getColumnIndex(DBMan.DBMovimientos.Traspaso))){
                    tvCantidad.setTextColor(Color.rgb(11, 79, 34));
                }
                else{
                    if(cambio != null) cantidad = cantidad * Double.parseDouble(cambio);
                    tvCantidad.setTextColor(Color.RED);
                }
            }

            tvCantidad.setText("$" + instance.format(cantidad));
        }
    }
}
