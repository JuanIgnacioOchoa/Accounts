package com.lala;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

import java.lang.reflect.Array;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class FragmentReportesMotives extends Fragment implements AdapterView.OnItemClickListener {
    private NumberFormat instance;
    private ListView lv;
    private String year;
    private String month;
    private Cursor c;
    private myAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static Fragment fragmentProfileUniqueInstance;
    private int idMoneda = 1;

    private TextView TVGanancia,TVGasto, TVIngreso, TVPorcentaje;
    private Double gasto, ingreso, ganancia, porcentaje;

    private Calendar calendar;

    @SuppressLint("ValidFragment")
    private FragmentReportesMotives(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = NumberFormat.getInstance();
        instance.setMinimumFractionDigits(2);

        calendar = Calendar.getInstance();
    }

    public static Fragment getInstance() {
        if(fragmentProfileUniqueInstance == null) {
            fragmentProfileUniqueInstance = new FragmentReportesMotives();
        }
        return fragmentProfileUniqueInstance;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_activity_reportes_motives, container, false);

        //Cast to each element from Fragment

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_activity_moves_swipeRefreshLayout);
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setRefreshing(false);
        lv = (ListView) view.findViewById(R.id.listView);
        TVGanancia = view.findViewById(R.id.CGanancia);
        TVGasto = view.findViewById(R.id.CGasto);
        TVIngreso = view.findViewById(R.id.CIngreso);
        TVPorcentaje = view.findViewById(R.id.Porcentaje);

        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onResume(){
        super.onResume();
        swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.fragment_activity_moves_swipeRefreshLayout);
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setRefreshing(false);
        lv = (ListView) getView().findViewById(R.id.listView);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        gasto = Principal.getGastoTotal(idMoneda);
        ingreso = Principal.getIngresoTotal(idMoneda);
        ganancia = ingreso + gasto;
        int m = calendar.get(Calendar.YEAR) + 1;
        String sm = "";
        calendar.get(Calendar.MONTH);
        if (m < 10){
            sm = "0"+m;
        } else {
            sm = "" + m;
        }
        TVGasto.setText(instance.format(Principal.round(gasto, 2)));
        TVGasto.setTextColor(Color.RED);
        TVIngreso.setText(instance.format(Principal.round(ingreso,2)));
        TVIngreso.setTextColor(Color.rgb(11,79,34));
        TVGanancia.setText(instance.format(Principal.round(ganancia,2)));
        if(ganancia  <= 0){
            porcentaje = (ganancia / -gasto) * 100;
            TVGanancia.setTextColor(Color.RED );
            TVPorcentaje.setTextColor(Color.RED );
        }else{
            porcentaje = (ganancia / ingreso) * 100;
            TVGanancia.setTextColor(Color.rgb(11,79,34));
            TVPorcentaje.setTextColor(Color.rgb(11,79,34));
        }
        TVPorcentaje.setText("     " + instance.format(Principal.round(porcentaje,2)) + "%");
        c = Principal.getSumByMotivesMonth(1, sm, calendar.get(Calendar.YEAR)+"");
        adapter = new myAdapter(getContext(), c);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
        updateAdapter();
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int isViaje = c.getInt(c.getColumnIndex("isViaje"));
        double in = c.getDouble(c.getColumnIndex("Ingreso"));
        double ga = c.getDouble(c.getColumnIndex("Gasto"));
        //if(in == null) in = 0.0;
        //if(ga == null) ga = 0.0;
        in = Principal.round(in, 2);
        ga = Principal.round(ga, 2);
        Intent i;
        if(isViaje==1){
            i = new Intent(getContext(),SeeTripMainActivity.class);
        } else{
            i = new Intent(getContext(),SeeByMotive.class);
        }

        i.putExtra("_id",(int) id);
        i.putExtra("month", month);
        i.putExtra("year", year);
        i.putExtra("Gasto", ga);
        i.putExtra("Ingreso", in);
        i.putExtra("IdMoneda", idMoneda);
        startActivity(i);
    }

    public void updateData(int idMoneda, String month, String year){
        this.idMoneda = idMoneda;
        this.month = month;
        this.year = year;
    }
    public void updateAdapter(){
        if(month == null){
            c = Principal.getSumByMotivesYear(idMoneda,year);
            //gasto = Principal.round(Principal.getGastoTotalYearly(idMoneda, year, false), 2);
            //ingreso = Principal.round(Principal.getIngresoTotalYearly(idMoneda, year, false), 2);
        } else{
            c = Principal.getSumByMotivesMonth(idMoneda,month,year);
            //gasto = Principal.round(Principal.getGastoTotalMonthly(idMoneda, month, year, false), 2);
            //ingreso = Principal.round(Principal.getIngresoTotalMonthly(idMoneda, month, year, false), 2);
        }
        ganancia = ingreso + gasto;
        if (ganancia <= 0) {
            porcentaje = ganancia / -gasto * 100;
            TVGanancia.setTextColor(Color.RED);
            TVPorcentaje.setTextColor(Color.RED);
        } else {
            porcentaje = ganancia / ingreso * 100;
            TVGanancia.setTextColor(Color.rgb(11, 79, 34));
            TVPorcentaje.setTextColor(Color.rgb(11, 79, 34));
        }
        TVPorcentaje.setText("     " + instance.format(Principal.round(porcentaje, 2)) + "%");
        TVIngreso.setText(instance.format(ingreso));
        TVGasto.setText(instance.format(gasto));
        TVGanancia.setText(instance.format(ganancia));
        adapter.changeCursor(c);
    }
    public class myAdapter extends CursorAdapter {

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
            TVMotivo.setText(cursor.getString(cursor.getColumnIndex("Motivo")));


        }
    }

}
