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

import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
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
    private Spinner spMonth, spTimeLapse;
    private Calendar calendar;
    private static Fragment fragmentProfileUniqueInstance;
    private String[] months ;
    private SimpleCursorAdapter simpleCursorAdapter;
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
        calendar = Calendar.getInstance();
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_activity_moves_swipeRefreshLayout);
        spTimeLapse = view.findViewById(R.id.spYear);
        spMonth = view.findViewById(R.id.spMonth);

        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setRefreshing(false);
        lv = (ListView) view.findViewById(R.id.listView);

        months = new String[]{
                getString(R.string.jan),
                getString(R.string.feb),
                getString(R.string.mar),
                getString(R.string.apr),
                getString(R.string.may),
                getString(R.string.jun),
                getString(R.string.jul),
                getString(R.string.aug),
                getString(R.string.sep),
                getString(R.string.oct),
                getString(R.string.nov),
                getString(R.string.dec)};

        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ArrayAdapter<String> spAdapterTimeLapse = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,
                new String[]{
                        getString(R.string.weekly),
                        getString(R.string.monthly),
                        getString(R.string.yearly)
                });
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
        final String[] monthsYear = monthsYearList.toArray(new String[monthsYearList.size()]);
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

        spMonth.setAdapter(spAdapteMonth);
        spTimeLapse.setAdapter(spAdapterTimeLapse);
        spMonth.setSelection(0);
        spTimeLapse.setSelection(1);

        spMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (spTimeLapse.getSelectedItemPosition()) {
                    case 0:
                        break;
                    case 1:
                        String s = spAdapteMonth.getItem(position);
                        year = s.substring(4,8);
                        month = s.substring(0,3);
                        String x = months[0];
                        int monthCount = 0;
                        while(!(month.equals(x)) || monthCount >= 12){
                            monthCount++;
                            x = months[monthCount];
                        }
                        switch (monthCount){
                            case 0:
                                month = "01";
                                break;
                            case 1:
                                month = "02";
                                break;
                            case 2:
                                month = "03";
                                break;
                            case 3:
                                month = "04";
                                break;
                            case 4:
                                month = "05";
                                break;
                            case 5:
                                month = "06";
                                break;
                            case 6:
                                month = "07";
                                break;
                            case 7:
                                month = "08";
                                break;
                            case 8:
                                month = "09";
                                break;
                            case 9:
                                month = "10";
                                break;
                            case 10:
                                month = "11";
                                break;
                            case 11:
                                month = "12";
                                break;
                        }
                        updateAdapter(month, year);
                        break;
                    case 2:
                        year = spAdapterYear.getItem(position);
                        month = null;
                        updateAdapter(month, year);
                        break;
                    default:
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spTimeLapse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        break;
                    case 1:
                        spMonth.setAdapter(spAdapteMonth);
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
        instance = NumberFormat.getInstance();
        instance.setMinimumFractionDigits(2);


        //porcentaje = (ganancia / ingreso) * 100;

        //TVPorcentaje.setText("     " + instance.format(porcentaje) + "%");
        Calendar calendar = Calendar.getInstance();
        int m = calendar.get(Calendar.YEAR) + 1;
        String sm = "";
        calendar.get(Calendar.MONTH);
        if (m < 10){
            sm = "0"+m;
        } else {
            sm = "" + m;
        }
        c = Principal.getTotalesCuentasByMonth(sm, calendar.get(Calendar.YEAR)+"");
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
            TVMotivo.setText(cursor.getString(cursor.getColumnIndex("Cuenta")));
            TVMotivo.setTextColor(Color.BLACK);

        }
    }

}
