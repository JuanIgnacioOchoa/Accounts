package com.lala;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Carlos Alexis on 21/11/2015.
 */
public class FragmentTotals extends Fragment {

    //private RecyclerView recyclerView;
    //private FloatingActionButton floatingActionButton;
    private ListView listView;
    private myAdapter adapter;
    //private AdapterEvents adapterEvents;
    //private ArrayList<Event> events;
    private CursorAdapter cursorAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static Fragment fragmentEventsUniqueInstance;
    private NumberFormat instance;
    private Cursor c;


    @SuppressLint("ValidFragment")
    private FragmentTotals(){

    }

    public static Fragment getInstance() {
        if(fragmentEventsUniqueInstance == null) {
            fragmentEventsUniqueInstance = new FragmentTotals();
        }
        return fragmentEventsUniqueInstance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_activity_totals, container, false);

        //Cast to each element from Fragment
        //recyclerView = (RecyclerView) view.findViewById(R.id.fragment_activity_event_recyclerView);
        //floatingActionButton = (FloatingActionButton) view.findViewById(R.id.fragment_activity_event_floatingActionButton);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_activity_event_swipeRefreshLayout);
        listView = (ListView) view.findViewById(R.id.listView_Totals);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //db = Principal.getDb();
/*        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.colorPrimary)));
*/
        instance = NumberFormat.getInstance();
        instance.setMinimumFractionDigits(2);
        // adapter
        c = Principal.getTotales();
        adapter = new myAdapter(this.getContext(),c);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getContext(),seeCuentas.class);
                i.putExtra("_id",c.getInt(c.getColumnIndex("_id")));
                startActivity(i);
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
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
        });

    }
    public void onResume(){
        super.onResume();
        c = Principal.getTotales();
        adapter.changeCursor(c);
    }
/*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_CREATE_EVENT) {
            if (data != null && data.getExtras() != null) {
                Place place = data.getParcelableExtra(PLACE);
                Event event = data.getParcelableExtra(EVENT);
                event.setPlace(place.getName());
                events.add(event);
                Collections.rotate(events, 1);
                adapterEvents.notifyDataSetChanged();
                recyclerView.scrollToPosition(0);
            }
        }
    }
    */
    public class myAdapter extends CursorAdapter{

        public myAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.list_totals, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // Find fields to populate in inflated template
            TextView tvCenta = (TextView) view.findViewById(R.id.LM_Cuenta);
            TextView tvCantidad = (TextView) view.findViewById(R.id.LM_Cantidad);
            TextView tvMoneda = (TextView) view.findViewById(R.id.LM_Moneda);
            // Extract properties from cursor
            String cuenta = cursor.getString(cursor.getColumnIndex(DBMan.DBTotales.Cuenta));
            double cantidad = cursor.getDouble(cursor.getColumnIndex(DBMan.DBTotales.CantidadActual));
            String moneda = cursor.getString(cursor.getColumnIndex("Moneda"));
            // Populate fields with extracted properties
            tvCenta.setText(cuenta);
            tvCantidad.setText("$" + instance.format(cantidad));
            tvMoneda.setText(moneda);
        }
    }

}
