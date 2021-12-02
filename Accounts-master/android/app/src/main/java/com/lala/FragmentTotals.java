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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;

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
    //private TextView tvPos, tvNeg, tvSum;
    private TextView tvCash, tvCreditCard, tvCantActual, tvDeudas, tvDeudores, tvInversion, tvTotal;
    private TextView tvCash2, tvCreditCard2, tvCantActual2, tvDeudas2, tvDeudores2, tvInversion2, tvTotal2;
    private LinearLayout llCreditCard, llDeudas, llDeudores, llInversion;
    private CheckBox cbActivas;
    private static Fragment fragmentEventsUniqueInstance;
    private NumberFormat instance;
    private Cursor c;
    private TextView tvHint;
    private Button btnTransfer, btnRetiro;
    private Spinner spMoneda;

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

        listView = (ListView) view.findViewById(R.id.listView_Totals);
        btnRetiro = view.findViewById(R.id.btnRetiro);
        btnTransfer = view.findViewById(R.id.btnTransfer);
        tvCash = view.findViewById(R.id.tvCash);
        tvCreditCard = view.findViewById(R.id.tvCreditCard);
        tvCantActual = view.findViewById(R.id.tvCantCorto);
        tvDeudores = view.findViewById(R.id.tvDeudores);
        tvDeudas = view.findViewById(R.id.tvDeudas);
        tvInversion = view.findViewById(R.id.tvInversion);
        tvTotal = view.findViewById(R.id.tvTotal);
        tvCash2 = view.findViewById(R.id.tvCash2);
        tvCreditCard2 = view.findViewById(R.id.tvCreditCard2);
        tvCantActual2 = view.findViewById(R.id.tvCantCorto2);
        tvDeudores2 = view.findViewById(R.id.tvDeudores2);
        tvDeudas2 = view.findViewById(R.id.tvDeudas2);
        tvInversion2 = view.findViewById(R.id.tvInversion2);
        tvTotal2 = view.findViewById(R.id.tvTotal2);
        llCreditCard = view.findViewById(R.id.llCreditCard);
        llDeudas = view.findViewById(R.id.llDeudas);
        llDeudores = view.findViewById(R.id.llDeudores);
        llInversion = view.findViewById(R.id.llInversion);
        cbActivas = view.findViewById(R.id.cbActiva);
        tvHint = (TextView) view.findViewById(R.id.tvHintAcc);
        spMoneda = view.findViewById(R.id.spMoneda);

        final FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(),NewAccount.class);
                startActivity(i);
            }
        });

        instance = NumberFormat.getInstance();
        instance.setMinimumFractionDigits(2);
        // adapter
        c = Principal.getTotales(false);
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
        final Cursor cursorMoneda = Principal.getMoneda();
        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(getContext(),android.R.layout.simple_list_item_1,cursorMoneda,new String[]{"Moneda"},new int[] {android.R.id.text1},0);
        spMoneda.setAdapter(simpleCursorAdapter);

        spMoneda.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateMoneda(cursorMoneda.getInt(cursorMoneda.getColumnIndex("_id")));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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

        cbActivas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAdapter();
            }
        });
        btnRetiro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(),Traspaso.class);
                i.putExtra("Retiro",true);
                startActivity(i);
            }
        });

        btnTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(),Traspaso.class);
                i.putExtra("Retiro",false);
                startActivity(i);
            }
        });
    }
    public void onResume(){
        super.onResume();
        updateAdapter();

    }
    public void updateAdapter(){
        if(!isAdded()) return;
        c = Principal.getTotales(cbActivas.isChecked());
        adapter.changeCursor(c);
        if(c.getCount()<=0){
            tvHint.setVisibility(View.VISIBLE);
            tvHint.setText(getString(R.string.hint_no_accounts));
        } else {
            tvHint.setVisibility(View.GONE);
        }
    }

    private void updateMoneda(int moneda){
        double cash = Principal.getTotalesCash(moneda, false);
        double credit = Principal.getTotalesCreditCard(moneda, false);
        double invest = Principal.getTotalesInvests(moneda, false);
        Cursor loans = Principal.getLoansTotalByCurrency(moneda);
        double deudas = loans.getDouble(loans.getColumnIndex("deuda"));
        double deudores = loans.getDouble(loans.getColumnIndex("deudores"));
        tvCash.setText(instance.format(cash));
        tvCreditCard.setText(instance.format(credit));
        tvInversion.setText(instance.format(invest));
        tvCantActual.setText(instance.format(cash + credit));
        tvTotal.setText(instance.format(cash + credit + invest + deudas + deudores));
        tvDeudores.setText(instance.format(deudores));
        tvDeudas.setText(instance.format(deudas));
        llDeudores.setVisibility(View.VISIBLE);
        llDeudas.setVisibility(View.VISIBLE);
        llCreditCard.setVisibility(View.VISIBLE);
        llInversion.setVisibility(View.VISIBLE);
        if(cash > 0){
            tvCash2.setTextColor(Color.rgb(11, 79, 34));
            tvCash.setTextColor(Color.rgb(11, 79, 34));
        }
        else if(cash < 0){
            tvCash.setTextColor(Color.RED);
            tvCash2.setTextColor(Color.RED);
        } else {
            tvCash.setTextColor(Color.BLACK);
            tvCash2.setTextColor(Color.BLACK);
        }

        if(credit > 0){
            tvCreditCard2.setTextColor(Color.rgb(11, 79, 34));
            tvCreditCard.setTextColor(Color.rgb(11, 79, 34));
        }
        else if(credit < 0){
            tvCreditCard2.setTextColor(Color.RED);
            tvCreditCard.setTextColor(Color.RED);
        } else {
            tvCreditCard.setTextColor(Color.BLACK);
            tvCreditCard2.setTextColor(Color.BLACK);
            llCreditCard.setVisibility(View.GONE);
        }

        if(deudas > 0){
            tvDeudas2.setTextColor(Color.rgb(11, 79, 34));
            tvDeudas.setTextColor(Color.rgb(11, 79, 34));
            llDeudas.setVisibility(View.VISIBLE);
        } else if(deudas < 0){
            tvDeudas.setTextColor(Color.RED);
            tvDeudas2.setTextColor(Color.RED);
            llDeudas.setVisibility(View.VISIBLE);
        } else {
            tvDeudas.setTextColor(Color.BLACK);
            tvDeudas2.setTextColor(Color.BLACK);
            llDeudas.setVisibility(View.GONE);
        }

        if(deudores > 0){
            tvDeudores.setTextColor(Color.rgb(11, 79, 34));
            tvDeudores2.setTextColor(Color.rgb(11, 79, 34));
            llDeudores.setVisibility(View.VISIBLE);
        } else if(deudores < 0){
            tvDeudores.setTextColor(Color.RED);
            tvDeudores2.setTextColor(Color.RED);
            llDeudores.setVisibility(View.VISIBLE);
        } else {
            tvDeudores.setTextColor(Color.BLACK);
            tvDeudores2.setTextColor(Color.BLACK);
            llDeudores.setVisibility(View.GONE);
        }

        if(invest > 0){
            tvInversion2.setTextColor(Color.rgb(11, 79, 34));
            tvInversion.setTextColor(Color.rgb(11, 79, 34));
        }
        else if (invest < 0){
            tvInversion2.setTextColor(Color.RED);
            tvInversion.setTextColor(Color.RED);
        } else {
            tvInversion.setTextColor(Color.BLACK);
            tvInversion2.setTextColor(Color.BLACK);
            llInversion.setVisibility(View.GONE);
        }

        if((cash + credit) > 0){
            tvCantActual2.setTextColor(Color.rgb(11, 79, 34));
            tvCantActual.setTextColor(Color.rgb(11, 79, 34));
        }
        else if((cash + credit) < 0){
            tvCantActual2.setTextColor(Color.RED);
            tvCantActual.setTextColor(Color.RED);
        } else {
            tvCantActual.setTextColor(Color.BLACK);
            tvCantActual2.setTextColor(Color.BLACK);
        }

        if((cash + credit + invest + deudas + deudores) > 0) {
            tvTotal2.setTextColor(Color.rgb(11, 79, 34));
            tvTotal.setTextColor(Color.rgb(11, 79, 34));
        }
        else if((cash + credit + invest + deudas + deudores) < 0){
            tvTotal2.setTextColor(Color.RED);
            tvTotal.setTextColor(Color.RED);
        } else {
            tvTotal.setTextColor(Color.BLACK);
            tvTotal2.setTextColor(Color.BLACK);
        }
    }
    public void loading(){
        tvHint.setVisibility(View.VISIBLE);
        tvHint.setText("Loading...");
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
            if(cantidad > 0){
                tvCenta.setTextColor(Color.rgb(11, 79, 34));
                tvCantidad.setTextColor(Color.rgb(11, 79, 34));
                tvMoneda.setTextColor(Color.rgb(11, 79, 34));
            } else if(cantidad < 0) {
                tvCenta.setTextColor(Color.RED);
                tvCantidad.setTextColor(Color.RED);
                tvMoneda.setTextColor(Color.RED);
            }
            tvCantidad.setText("$" + instance.format(cantidad));
            tvMoneda.setText(moneda);
        }
    }

}
