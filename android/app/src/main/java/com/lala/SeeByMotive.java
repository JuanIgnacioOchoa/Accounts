package com.lala;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.cursoradapter.widget.CursorAdapter;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SeeByMotive extends AppCompatActivity {

    //private Cursor cursorMotives;
    private Cursor cursorFecha;
    private NumberFormat instance;
    private myAdapterFecha adapter;
    private ListView lv;
    private TextView textViewGa, textViewIn;
    private int id, idMoneda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_by_motive);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                //handleOnBackPress();
            }
        });
        Intent i = this.getIntent();
        id = i.getIntExtra("_id", 0);
        double ga = i.getDoubleExtra("Gasto", 0.0);
        double in = i.getDoubleExtra("Ingreso", 0.0);
        idMoneda = i.getIntExtra("IdMoneda", 1);
        String month = i.getStringExtra("month");
        String year = i.getStringExtra("year");
        cursorFecha = Principal.getMovimientosFecha(id, month, year, idMoneda);
        String title = Principal.getMotiveId(id);
        this.setTitle(title);
        instance = NumberFormat.getInstance();
        instance.setMinimumFractionDigits(2);
        lv = (ListView) findViewById(R.id.listView);
        textViewIn = findViewById(R.id.textViewIn);
        textViewGa = findViewById(R.id.textViewGa);
        textViewGa.setText(instance.format(ga));
        textViewIn.setText(instance.format(in));
        adapter = new myAdapterFecha(getApplicationContext(), cursorFecha);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(null);
        /*
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getApplicationContext(), Gasto.class);
                i.putExtra("id", cursorMotives.getInt(cursorMotives.getColumnIndex("_id")));
                startActivity(i);

            }
        });

         */
    }

    public class myAdapterFecha extends CursorAdapter {

        public myAdapterFecha(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.inflate_dates, parent, false);
        }

        @Override
        public void bindView(View view, final Context context, Cursor cursor) {
            // Find fields to populate in inflated template
            TextView tvFecha = (TextView) view.findViewById(R.id.tv_inflate_date);
            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.linear_inflate_dates);
            linearLayout.removeAllViewsInLayout();
            // Extract properties from cursor
            //int id = 0;
            //id = 0;
            String fecha = cursor.getString(cursor.getColumnIndex(DBMan.DBMovimientos.Fecha));
            Cursor c = Principal.getMotiveByFecha(id, fecha, idMoneda);
            int x = c.getCount();
            //c.moveToNext();
            while(c.moveToNext()){
                LinearLayout linear = new LinearLayout(context);
                linear.setWeightSum(3);
                LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,1f);
                TextView cuentaTV = new TextView(context);
                TextView motivoTV = new TextView(context);
                TextView cantidadTV = new TextView(context);
                linear.setLayoutParams(lp1);
                cuentaTV.setLayoutParams(lp2);
                motivoTV.setLayoutParams(lp2);
                cantidadTV.setLayoutParams(lp2);
                String cuenta = Principal.getCuentaTotales(c.getInt(c.getColumnIndex("IdTotales")));
                double cantidad = c.getDouble(c.getColumnIndex(DBMan.DBMovimientos.Cantidad));
                int idMoneda = c.getInt(c.getColumnIndex("IdMoneda"));
                String moneda = Principal.getIdMoneda(idMoneda);
                String motivo = Principal.getMotiveId(c.getInt(c.getColumnIndex(DBMan.DBMovimientos.IdMotivo)));
                final int idTraspaso = c.getInt(c.getColumnIndex("Traspaso"));
                final int id = c.getInt(c.getColumnIndex("_id"));
                if(idTraspaso != 0){
                    motivo += " " + Principal.getCuentaTotales(idTraspaso);
                    moneda = "";
                    cuentaTV.setTextColor(ContextCompat.getColor(context,R.color.neutral_yellow));
                    motivoTV.setTextColor(ContextCompat.getColor(context,R.color.neutral_yellow));
                    cantidadTV.setTextColor(ContextCompat.getColor(context,R.color.neutral_yellow));
                }
                else if(cantidad < 0){
                    cuentaTV.setTextColor(Color.RED);
                    motivoTV.setTextColor(Color.RED);
                    cantidadTV.setTextColor(Color.RED);
                } else {
                    cuentaTV.setTextColor(ContextCompat.getColor(context,R.color.positive_green));
                    motivoTV.setTextColor(ContextCompat.getColor(context,R.color.positive_green));
                    cantidadTV.setTextColor(ContextCompat.getColor(context,R.color.positive_green));
                }
                cuentaTV.setText(cuenta);
                motivoTV.setText(motivo);
                cantidadTV.setText(instance.format(cantidad)+" " + moneda);
                cantidadTV.setGravity(Gravity.RIGHT);
                linear.addView(cuentaTV);
                linear.addView(motivoTV);
                linear.addView(cantidadTV);
                linearLayout.addView(linear);

                linear.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch(event.getAction())
                        {
                            case MotionEvent.ACTION_DOWN:
                                v.setBackgroundColor(Color.LTGRAY);
                                break;
                            case MotionEvent.ACTION_UP:
                                Intent i;
                                if(idTraspaso == 0){
                                    i = new Intent(context, Gasto.class);
                                    i.putExtra("id", id);
                                } else {
                                    i = new Intent(context, seeTraspaso.class);
                                    i.putExtra("_id", id);
                                }
                                startActivity(i);
                                break;
                            case MotionEvent.ACTION_MOVE:
                                v.setBackgroundColor(Color.TRANSPARENT);
                                break;
                        }
                        return true;
                    }
                });
            }

            // Populate fields with extracted properties
            Calendar calendar = Calendar.getInstance();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                calendar.setTime(sdf.parse(fecha));// all done
                sdf = new SimpleDateFormat("EEEE, d MMMM, yyyy");
                fecha = sdf.format(calendar.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tvFecha.setText(fecha);
        }
    }

}
