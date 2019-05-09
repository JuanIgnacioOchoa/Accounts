package com.lala;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.support.v4.app.Fragment;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class FragmentReportesCuentas extends Fragment {
    private NumberFormat instance;
    private ListView lv;
    private String year;
    private String month;

    private myAdapter adapter;
    private Cursor c;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static Spinner spMoneda;
    private static Fragment fragmentProfileUniqueInstance;
    @SuppressLint("ValidFragment")
    private FragmentReportesCuentas(){

    }
    public static Fragment getInstance() {
        if(fragmentProfileUniqueInstance == null) {
            fragmentProfileUniqueInstance = new FragmentReportesCuentas();
        }
        return fragmentProfileUniqueInstance;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_activity_reportes_cuentas, container, false);

        //Cast to each element from Fragment

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_activity_moves_swipeRefreshLayout);
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setRefreshing(false);
        lv = (ListView) view.findViewById(R.id.listView);


        return view;

    }

    @Override
    public void onResume(){
        super.onResume();
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        instance = NumberFormat.getInstance();
        instance.setMinimumFractionDigits(2);


        //porcentaje = (ganancia / ingreso) * 100;

        //TVPorcentaje.setText("     " + instance.format(porcentaje) + "%");

        c = Principal.getTotalesCuentasByMonth("06", "2016");
        adapter = new myAdapter(getContext(), c);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getContext(),seeCuentas.class);
                i.putExtra("_id",c.getInt(c.getColumnIndex("_id")));
                i.putExtra("year", year);
                i.putExtra("month", month);
                startActivity(i);
            }
        });
    }


    public void updateAdapter(String month, String year){
        this.year = year;
        this.month = month;
        if(month == null){
            c = Principal.getTotalesCuentasByYear(year);
        } else{
            c = Principal.getTotalesCuentasByMonth(month,year);
        }
        adapter.changeCursor(c);
    }

    public class myAdapter extends android.support.v4.widget.CursorAdapter {

        public myAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.inflate_reportes_motive, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            Double in = cursor.getDouble(cursor.getColumnIndex("Ingreso"));
            Double ga = cursor.getDouble(cursor.getColumnIndex("Gasto"));
            if(in == null) in = 0.0;
            if(ga == null) ga = 0.0;
            in = Principal.round(in, 2);
            ga = Principal.round(ga, 2);
            TextView TVCantidadI = (TextView) view.findViewById(R.id.textView7);
            TextView TVCantidadG = (TextView) view.findViewById(R.id.textView8);
            TextView TVMotivo = (TextView) view.findViewById(R.id.textView6);
            TVCantidadI.setText("$" + instance.format(in));
            TVCantidadG.setText("$" + instance.format(ga));
            TVMotivo.setText(cursor.getString(cursor.getColumnIndex("Cuenta")));
            TVMotivo.setTextColor(Color.BLACK);

        }
    }

}
