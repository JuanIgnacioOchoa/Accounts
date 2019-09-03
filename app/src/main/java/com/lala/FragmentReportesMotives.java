package com.lala;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.cursoradapter.widget.CursorAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import java.text.NumberFormat;

public class FragmentReportesMotives extends Fragment implements AdapterView.OnItemClickListener {
    private NumberFormat instance;
    private ListView lv;
    private String year;
    private String month;
    private Cursor c;
    private myAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static Fragment fragmentProfileUniqueInstance;
    private int idMoneda = 0;
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

        c = Principal.getSumByMotivesMonth(1, "06", "2016");
        adapter = new myAdapter(getContext(), c);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int isViaje = c.getInt(c.getColumnIndex("isViaje"));
        Intent i;
        if(isViaje==1){
            i = new Intent(getContext(),SeeTripMainActivity.class);
        } else{
            i = new Intent(getContext(),SeeByMotive.class);
        }

        i.putExtra("_id",(int) id);
        i.putExtra("month", month);
        i.putExtra("year", year);
        startActivity(i);
    }

    public void updateAdapter(int idMoneda, String month, String year){
        this.month = month;
        this.year = year;
        this.idMoneda = idMoneda;
        //TODO colocar moneda
        if(month == null){
            c = Principal.getSumByMotivesYear(idMoneda,year);
        } else{
            c = Principal.getSumByMotivesMonth(idMoneda,month,year);
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
            TVMotivo.setText(cursor.getString(cursor.getColumnIndex("Motivo")));


        }
    }

}
