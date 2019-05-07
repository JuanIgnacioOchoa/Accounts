package com.lala;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;

public class SeeByMotive extends AppCompatActivity {

    private Cursor cursorMotives;
    private NumberFormat instance;
    private myAdapter adapter;
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_by_motive);
        Intent i = this.getIntent();
        int id = i.getIntExtra("id", 0);
        String month = i.getStringExtra("month");
        String year = i.getStringExtra("year");
        cursorMotives = Principal.getSumByMotive(id, month, year);
        String title = Principal.getMotiveId(id);
        this.setTitle(title);
        instance = NumberFormat.getInstance();
        instance.setMinimumFractionDigits(2);
        lv = (ListView) findViewById(R.id.listView);

        adapter = new myAdapter(getApplicationContext(), cursorMotives);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getApplicationContext(), seeMove.class);
                i.putExtra("id", cursorMotives.getInt(cursorMotives.getColumnIndex("_id")));
                startActivity(i);

            }
        });
    }

    public class myAdapter extends android.support.v4.widget.CursorAdapter {

        public myAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.inflate_see_by_motive, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            Double cant = cursor.getDouble(cursor.getColumnIndex("Cantidad"));
            String comment = cursor.getString((cursor.getColumnIndex(DBMan.DBMovimientos.Comment)));
            String cuenta = cursor.getString((cursor.getColumnIndex("Cuenta")));
            String Fecha = cursor.getString((cursor.getColumnIndex(DBMan.DBMovimientos.Fecha)));
            String moneda = Principal.getIdMoneda(cursor.getInt(cursor.getColumnIndex(DBMan.DBMovimientos.IdMoneda)));
            cant = Principal.round(cant, 2);
            TextView TVCantidad = (TextView) view.findViewById(R.id.Cantidad);
            TextView TVComentario = (TextView) view.findViewById(R.id.Comentario);
            TextView TVCuenta = (TextView) view.findViewById(R.id.Cuenta);
            TextView TVFecha = (TextView) view.findViewById(R.id.Fecha);
            if(cant < 0) TVCantidad.setTextColor(Color.RED); else TVCantidad.setTextColor(Color.rgb(11,79,34));
            TVCantidad.setText("$" + instance.format(cant) + moneda);
            TVComentario.setText(comment);
            TVCuenta.setText(cuenta);
            TVFecha.setText(Fecha);
        }
    }

}
