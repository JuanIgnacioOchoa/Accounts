package com.lala;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.mortbay.jetty.Server;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Carlos Alexis on 21/11/2015.
 */
public class FragmentMoves extends Fragment{


    //private SwipeRefreshLayout swipeRefreshLayout;
    private static Fragment fragmentProfileUniqueInstance;
    private ListView listView;
    private myAdapterFecha adapter;
    private NumberFormat instance;
    private Cursor c, cursorMoneda, cursorFecha;
    private String[] from;
    private int[] to;
    private SimpleCursorAdapter simpleCursorAdapter;
    private TextView tvHint;
    private String month = null, year = null;

    @SuppressLint("ValidFragment")
    private FragmentMoves(){

    }

    public static Fragment getInstance() {
        if(fragmentProfileUniqueInstance == null) {
            fragmentProfileUniqueInstance = new FragmentMoves();
        }
        return fragmentProfileUniqueInstance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_activity_moves, container, false);

        //Cast to each element from Fragment

        //swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_activity_moves_swipeRefreshLayout);
        listView = (ListView) view.findViewById(R.id.listView_Movimientos);
        tvHint = (TextView) view.findViewById(R.id.tvHintMove);

        final FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(),Gasto.class);
                i.putExtra("Gasto", true);
                startActivity(i);
            }
        });
        return view;
    }
    @Override
    public void onResume(){
        super.onResume();
        updateAdapter();
        //listView.setAdapter(adapter);
    }
    public void updateData(String month, String year){
        this.month = month;
        this.year = year;
    }
    public void updateAdapter(){
        if(!isAdded()) return;
        if(year == null){
            c = Principal.getMovimientos();
            cursorFecha = Principal.getMovimientosFecha();
        } else if (month == null){
            c = Principal.getMovimientosYearly(year);
            cursorFecha = Principal.getMovimientosFechaYearly(year);
        } else {
            c = Principal.getMovimientosMonthly(month, year);
            cursorFecha = Principal.getMovimientosFechaMonthly(month, year);
        }
        adapter.changeCursor(cursorFecha);
        cursorMoneda = Principal.getMoneda();
        simpleCursorAdapter.changeCursor(cursorMoneda);

        if(c.getCount()<=0){
            tvHint.setVisibility(View.VISIBLE);
            tvHint.setText(getString(R.string.hint_no_moves));
        } else {
            tvHint.setVisibility(View.GONE);
        }
    }
    public void loading(){
        tvHint.setVisibility(View.VISIBLE);
        tvHint.setText("Loading...");
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
/*
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.colorPrimary)));
*/
        instance = NumberFormat.getInstance();
        instance.setMinimumFractionDigits(2);



        cursorMoneda = Principal.getMoneda();
        from = new String[]{"Moneda"};
        to = new int[]{android.R.id.text1};
        simpleCursorAdapter = new SimpleCursorAdapter(getContext(),android.R.layout.simple_list_item_1,cursorMoneda,from,to,0);

        c = Principal.getMovimientos();
        cursorFecha = Principal.getMovimientosFecha();
        adapter = new myAdapterFecha(this.getContext(),cursorFecha);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(null);
        //listView.setEnabled(false);

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
            Cursor c = Principal.getMovimientosByDate(fecha);
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
                    cuentaTV.setTextColor(ContextCompat.getColor(getContext(),R.color.neutral_yellow));
                    motivoTV.setTextColor(ContextCompat.getColor(getContext(),R.color.neutral_yellow));
                    cantidadTV.setTextColor(ContextCompat.getColor(getContext(),R.color.neutral_yellow));
                }
                else if(cantidad < 0){
                    cuentaTV.setTextColor(Color.RED);
                    motivoTV.setTextColor(Color.RED);
                    cantidadTV.setTextColor(Color.RED);
                } else {
                    cuentaTV.setTextColor(ContextCompat.getColor(getContext(),R.color.positive_green));
                    motivoTV.setTextColor(ContextCompat.getColor(getContext(),R.color.positive_green));
                    cantidadTV.setTextColor(ContextCompat.getColor(getContext(),R.color.positive_green));
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
                                    i = new Intent(context, Traspaso.class);
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

    public class myAdapter extends CursorAdapter {

        public myAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.list_movimientos, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // Find fields to populate in inflated template
            TextView tvCuenta = (TextView) view.findViewById(R.id.LM_Cuenta);
            TextView tvCantidad = (TextView) view.findViewById(R.id.LM_Cantidad);
            TextView tvMoneda = (TextView) view.findViewById(R.id.LM_Moneda);
            TextView tvMotivo = (TextView) view.findViewById(R.id.LM_Motivo);
            TextView tvFecha = (TextView) view.findViewById(R.id.LM_Fecha);
            // Extract properties from cursor
            //int id = 0;
            //id = 0;
            String cuenta = Principal.getCuentaTotales(cursor.getInt(cursor.getColumnIndex("IdTotales")));
            double cantidad = cursor.getDouble(cursor.getColumnIndex(DBMan.DBMovimientos.Cantidad));
            int idMoneda = cursor.getInt(cursor.getColumnIndex("IdMoneda"));
            String moneda;
            moneda= Principal.getIdMoneda(idMoneda);
            String motivo = Principal.getMotiveId(cursor.getInt(cursor.getColumnIndex(DBMan.DBMovimientos.IdMotivo)));
            String fecha = cursor.getString(cursor.getColumnIndex(DBMan.DBMovimientos.Fecha));
            // Populate fields with extracted properties
            //Toast.makeText(context,fecha + " aa",Toast.LENGTH_SHORT).show();
            tvCuenta.setText(cuenta);
            if(cantidad<0){
                cantidad = cantidad*-1;
                tvCantidad.setTextColor(Color.RED);
            } else tvCantidad.setTextColor(Color.rgb(11, 79, 34));
            tvCantidad.setText("$" + instance.format(cantidad));
            tvMoneda.setText(moneda);
            tvMotivo.setText(motivo);
            tvFecha.setText(fecha);
        }
    }

}
