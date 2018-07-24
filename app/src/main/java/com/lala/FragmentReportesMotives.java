package com.lala;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.support.v4.app.Fragment;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class FragmentReportesMotives extends Fragment implements AdapterView.OnItemClickListener {
    private NumberFormat instance;
    private ListView lv;
    private TextView TVGanancia,TVGasto, TVIngreso, TVPorcentaje;
    private Double gasto, ingreso, ganancia, porcentaje;
    private Spinner spMonth, spYear;
    private Calendar calendar;
    private String year;
    private String month;
    private final String[] months = new String[]{"Ene","Feb","Mar", "Abr", "May", "Jun", "Jul", "Ago" ,"Sep", "Oct","Nov", "Dic"};

    private myAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static Fragment fragmentProfileUniqueInstance;
    @SuppressLint("ValidFragment")
    private FragmentReportesMotives(){

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
        lv = (ListView) view.findViewById(R.id.listView);
        TVGanancia   = (TextView) view.findViewById(R.id.CGanancia);
        TVGasto      = (TextView) view.findViewById(R.id.CGasto);
        TVIngreso    = (TextView) view.findViewById(R.id.CIngreso);
        TVPorcentaje = (TextView) view.findViewById(R.id.Porcentaje);
        spYear        = (Spinner)  view.findViewById(R.id.spYear);
        spMonth      = (Spinner) view.findViewById(R.id.spinner4);

        return view;
        /*super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportes);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        lv = (ListView) findViewById(R.id.listView);
        TVGanancia   = (TextView) findViewById(R.id.CGanancia);
        TVGasto      = (TextView) findViewById(R.id.CGasto);
        TVIngreso    = (TextView) findViewById(R.id.CIngreso);
        TVPorcentaje = (TextView) findViewById(R.id.Porcentaje);
        spYear        = (Spinner)  findViewById(R.id.spYear);
        spMonth      = (Spinner) findViewById(R.id.spinner4);

        instance = NumberFormat.getInstance();
        instance.setMinimumFractionDigits(2);
        calendar = Calendar.getInstance();

        ArrayAdapter<String> spAdapterTimeLapse = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[]{"Weekly", "Monthly", "Yearly"});
        Calendar focus = Principal.getFirstDate();

        ArrayList<String> monthsYearList = new ArrayList<>();
        while(focus.get(Calendar.YEAR) != calendar.get(Calendar.YEAR) || focus.get(Calendar.MONTH) != calendar.get(Calendar.MONTH)){
            monthsYearList.add(months[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.YEAR));
            if(calendar.get(Calendar.MONTH)<=0){
                calendar.set(Calendar.MONTH,Calendar.DECEMBER);
                calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) -1 );
            }
            else calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) -1);
        }
        monthsYearList.add(months[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.YEAR));
        String[] monthsYear = monthsYearList.toArray(new String[monthsYearList.size()]);
        final ArrayAdapter<String> spAdapteMonth = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, monthsYear);
        Date date = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        int yearI = calendar.get(calendar.YEAR);
        int yearArraySize = yearI - 2016;
        String[] years = new String[yearArraySize + 1];
        for(int i = 0, y = yearI; y >= 2016; i++, y--)
        {
            years[i] = Integer.toString(y);
        }
        final ArrayAdapter<String> spAdapterYear= new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, years);

        //calendar.get(Calendar.MONTH) + 1;
        //calendar.get(Calendar.YEAR);
        spMonth.setAdapter(spAdapteMonth);
        spYear.setAdapter(spAdapterTimeLapse);
        spMonth.setSelection(0);
        spYear.setSelection(1);

        gasto = Principal.getGastoTotal(1);
        ingreso = Principal.getIngresoTotal(1);
        ganancia = ingreso + gasto;

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


        //porcentaje = (ganancia / ingreso) * 100;

        //TVPorcentaje.setText("     " + instance.format(porcentaje) + "%");

        Cursor c = Principal.getSumByMoitiveMonth(1, "06", "2016");
        adapter = new myAdapter(getApplicationContext(), c);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
        spMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Cursor c;
                switch (spYear.getSelectedItemPosition()) {
                    case 0:
                        break;
                    case 1:
                        String s = spAdapteMonth.getItem(position);
                        year = s.substring(4,8);
                        month = s.substring(0,3);
                        switch (month){
                            case "Ene":
                                month = "01";
                                break;
                            case "Feb":
                                month = "02";
                                break;
                            case "Mar":
                                month = "03";
                                break;
                            case "Abr":
                                month = "04";
                                break;
                            case "May":
                                month = "05";
                                break;
                            case "Jun":
                                month = "06";
                                break;
                            case "Jul":
                                month = "07";
                                break;
                            case "Ago":
                                month = "08";
                                break;
                            case "Sep":
                                month = "09";
                                break;
                            case "Oct":
                                month = "10";
                                break;
                            case "Nov":
                                month = "11";
                                break;
                            case "Dic":
                                month = "12";
                                break;
                        }
                        gasto    = Principal.round(Principal.getGastoTotalMonthly(1,month, year),2);
                        ingreso  = Principal.round(Principal.getIngresoTotalMonthly(1, month, year),2);
                        ganancia = ingreso + gasto;
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
                        TVIngreso.setText(instance.format(ingreso));
                        TVGasto.setText(instance.format(gasto));
                        TVGanancia.setText(instance.format(ganancia));

                        c = Principal.getSumByMoitiveMonth(1, month,year);
                        adapter.changeCursor(c);
                        break;
                    case 2:
                        year = spAdapterYear.getItem(position);
                        gasto    = Principal.round(Principal.getGastoTotalYearly(1, year),2);
                        ingreso  = Principal.round(Principal.getIngresoTotalYearly(1, year),2);
                        ganancia = ingreso + gasto;
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
                        TVIngreso.setText(instance.format(ingreso));
                        TVGasto.setText(instance.format(gasto));
                        TVGanancia.setText(instance.format(ganancia));
                        c = Principal.getSumByMoitiveYear(1, year);
                        adapter.changeCursor(c);
                        break;
                    default:
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        spMonth.setAdapter(spAdapterYear);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        */
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
        calendar = Calendar.getInstance();

        ArrayAdapter<String> spAdapterTimeLapse = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, new String[]{"Weekly", "Monthly", "Yearly"});
        Calendar focus = Principal.getFirstDate();

        ArrayList<String> monthsYearList = new ArrayList<>();
        while(focus.get(Calendar.YEAR) != calendar.get(Calendar.YEAR) || focus.get(Calendar.MONTH) != calendar.get(Calendar.MONTH)){
            monthsYearList.add(months[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.YEAR));
            if(calendar.get(Calendar.MONTH)<=0){
                calendar.set(Calendar.MONTH,Calendar.DECEMBER);
                calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) -1 );
            }
            else calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) -1);
        }
        monthsYearList.add(months[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.YEAR));
        String[] monthsYear = monthsYearList.toArray(new String[monthsYearList.size()]);
        final ArrayAdapter<String> spAdapteMonth = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1, monthsYear);
        Date date = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        int yearI = calendar.get(calendar.YEAR);
        int yearArraySize = yearI - 2016;
        String[] years = new String[yearArraySize + 1];
        for(int i = 0, y = yearI; y >= 2016; i++, y--)
        {
            years[i] = Integer.toString(y);
        }
        final ArrayAdapter<String> spAdapterYear= new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1, years);

        //calendar.get(Calendar.MONTH) + 1;
        //calendar.get(Calendar.YEAR);
        spMonth.setAdapter(spAdapteMonth);
        spYear.setAdapter(spAdapterTimeLapse);
        spMonth.setSelection(0);
        spYear.setSelection(1);

        gasto = Principal.getGastoTotal(1);
        ingreso = Principal.getIngresoTotal(1);
        ganancia = ingreso + gasto;

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


        //porcentaje = (ganancia / ingreso) * 100;

        //TVPorcentaje.setText("     " + instance.format(porcentaje) + "%");

        Cursor c = Principal.getSumByMoitiveMonth(1, "06", "2016");
        adapter = new myAdapter(getContext(), c);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
        spMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Cursor c;
                switch (spYear.getSelectedItemPosition()) {
                    case 0:
                        break;
                    case 1:
                        String s = spAdapteMonth.getItem(position);
                        year = s.substring(4,8);
                        month = s.substring(0,3);
                        switch (month){
                            case "Ene":
                                month = "01";
                                break;
                            case "Feb":
                                month = "02";
                                break;
                            case "Mar":
                                month = "03";
                                break;
                            case "Abr":
                                month = "04";
                                break;
                            case "May":
                                month = "05";
                                break;
                            case "Jun":
                                month = "06";
                                break;
                            case "Jul":
                                month = "07";
                                break;
                            case "Ago":
                                month = "08";
                                break;
                            case "Sep":
                                month = "09";
                                break;
                            case "Oct":
                                month = "10";
                                break;
                            case "Nov":
                                month = "11";
                                break;
                            case "Dic":
                                month = "12";
                                break;
                        }
                        gasto    = Principal.round(Principal.getGastoTotalMonthly(1,month, year),2);
                        ingreso  = Principal.round(Principal.getIngresoTotalMonthly(1, month, year),2);
                        ganancia = ingreso + gasto;
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
                        TVIngreso.setText(instance.format(ingreso));
                        TVGasto.setText(instance.format(gasto));
                        TVGanancia.setText(instance.format(ganancia));

                        c = Principal.getSumByMoitiveMonth(1, month,year);
                        adapter.changeCursor(c);
                        break;
                    case 2:
                        year = spAdapterYear.getItem(position);
                        gasto    = Principal.round(Principal.getGastoTotalYearly(1, year),2);
                        ingreso  = Principal.round(Principal.getIngresoTotalYearly(1, year),2);
                        ganancia = ingreso + gasto;
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
                        TVIngreso.setText(instance.format(ingreso));
                        TVGasto.setText(instance.format(gasto));
                        TVGanancia.setText(instance.format(ganancia));
                        c = Principal.getSumByMoitiveYear(1, year);
                        adapter.changeCursor(c);
                        break;
                    default:
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        spMonth.setAdapter(spAdapterYear);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent i = new Intent(getContext(),SeeByMotive.class);
        switch (spYear.getSelectedItemPosition()) {
            case 0:
                break;
            case 1:
                i.putExtra("id",(int) id);
                i.putExtra("month", month);
                i.putExtra("year", year);
                break;
            case 2:
                break;
            default:
        }
        startActivity(i);
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
            TVMotivo.setText(cursor.getString(cursor.getColumnIndex("Motivo")));


        }
    }

}
