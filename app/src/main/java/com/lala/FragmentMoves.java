package com.lala;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.NumberFormat;

/**
 * Created by Carlos Alexis on 21/11/2015.
 */
public class FragmentMoves extends Fragment{


    private SwipeRefreshLayout swipeRefreshLayout;
    private static Fragment fragmentProfileUniqueInstance;
    private ListView listView;
    private myAdapter  adapter;
    private NumberFormat instance;
    private Cursor c, cursorMoneda;
    private String[] from;
    private int[] to;
    private SimpleCursorAdapter simpleCursorAdapter;
    private TextView tvGasto, tvIngreso;
    private Spinner spMoneda;
    private TextView tvHint;

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

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_activity_moves_swipeRefreshLayout);
        listView = (ListView) view.findViewById(R.id.listView_Movimientos);
        tvGasto = (TextView) view.findViewById(R.id.frag_moves_tvGasto);
        tvIngreso = (TextView) view.findViewById(R.id.frag_moves_tvIngreso);
        spMoneda = (Spinner) view.findViewById(R.id.frag_moves_spMoneda);
        tvHint = (TextView) view.findViewById(R.id.tvHintMove);

        return view;
    }
    @Override
    public void onResume(){
        super.onResume();
        updateAdapter();
        //listView.setAdapter(adapter);
    }
    public void updateAdapter(){
        if(!isAdded()) return;
        c = Principal.getMovimientos();
        adapter.changeCursor(c);
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

        tvGasto.setTextColor(Color.RED);
        tvIngreso.setTextColor(Color.rgb(11, 79, 34));


        cursorMoneda = Principal.getMoneda();
        from = new String[]{"Moneda"};
        to = new int[]{android.R.id.text1};
        simpleCursorAdapter = new SimpleCursorAdapter(getContext(),android.R.layout.simple_list_item_1,cursorMoneda,from,to,0);
        spMoneda.setAdapter(simpleCursorAdapter);
        spMoneda.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int idM = cursorMoneda.getInt(cursorMoneda.getColumnIndex("_id"));
                tvGasto.setText(instance.format(Principal.getGastoTotal(idM)));
                tvIngreso.setText(instance.format(Principal.getIngresoTotal(idM)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        c = Principal.getMovimientos();
        adapter = new myAdapter(this.getContext(),c);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getContext(), seeMove.class);
                i.putExtra("id", c.getInt(c.getColumnIndex("_id")));
                startActivity(i);

            }
        });
        /*swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                }, 1000);

            }
        });*/
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setRefreshing(false);
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
