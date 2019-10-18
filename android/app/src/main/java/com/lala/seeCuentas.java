package com.lala;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.core.content.ContextCompat;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class seeCuentas extends AppCompatActivity {

    private TextView tvCurrent;
    private ListView listView;
    private NumberFormat instance;
    private Cursor cursorTotales, cursorMoves;
    private Double cant = 0.0;
    private myAdapterFecha adapter;
    private int id, tipo;
    private String month, year;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_cuentas);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                //handleOnBackPress();
            }
        });
        Intent i = getIntent();
        id = i.getIntExtra("_id", 1);
        year = i.getStringExtra("year");
        month = i.getStringExtra("month");

        cursorTotales = Principal.getTotal(id);
        cursorTotales.moveToFirst();
        String title = cursorTotales.getString(cursorTotales.getColumnIndex(DBMan.DBTotales.Cuenta));
        this.setTitle(title+" "+Principal.getIdMoneda(cursorTotales.getInt(cursorTotales.getColumnIndex(DBMan.DBTotales.Moneda))));
        instance = NumberFormat.getInstance();
        instance.setMinimumFractionDigits(2);
        tvCurrent = (TextView) findViewById(R.id.see_acc_tvCurr);
        listView = (ListView) findViewById(R.id.listView_see_acc);


        cant = cursorTotales.getDouble(cursorTotales.getColumnIndex(DBMan.DBTotales.CantidadActual));
        tipo = cursorTotales.getInt(cursorTotales.getColumnIndex(DBMan.DBTotales.Tipo));
        adapter = new myAdapterFecha(getApplicationContext(),cursorMoves);
        tvCurrent.setText(instance.format(cant));
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(null);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_seeMove);
        fab.setBackgroundColor(Color.BLUE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(seeCuentas.this, "Click", Toast.LENGTH_SHORT).show();
                final AlertDialog.Builder builder = new AlertDialog.Builder(seeCuentas.this);
                builder.setTitle(getString(R.string.account_edit));

// Set up the input
                final EditText inputNombre = new EditText(seeCuentas.this);
                final TextView textNombre = new TextView(seeCuentas.this);
                final EditText inputCant = new EditText(seeCuentas.this);
                final TextView textCant = new TextView(seeCuentas.this);
                final CheckBox cbActivar = new CheckBox(seeCuentas.this);
                final Spinner spTipo = new Spinner(seeCuentas.this);
                final TextView tvTipo = new TextView(seeCuentas.this);
                final LinearLayout layout = new LinearLayout(getApplicationContext());
                final LinearLayout linear2 = new LinearLayout(getApplicationContext());
                final LinearLayout linear3 = new LinearLayout(getApplicationContext());
                final LinearLayout linear4 = new LinearLayout(getApplicationContext());
                final RelativeLayout relativeLayout = new RelativeLayout(getApplicationContext());
// Specify the type of input expected;
                layout.setWeightSum(1);
                linear2.setWeightSum(2);
                linear3.setWeightSum(2);
                linear4.setWeightSum(2);

                LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,1f);
                RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                RelativeLayout.LayoutParams r2 = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);

                RelativeLayout.LayoutParams r3 = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                RelativeLayout.LayoutParams r4 = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);

                relativeLayout.setLayoutParams(rl);
                inputCant.setLayoutParams(lp2);
                inputNombre.setLayoutParams(lp2);
                textCant.setLayoutParams(lp2);
                textNombre.setLayoutParams(lp2);
                tvTipo.setLayoutParams(lp2);
                spTipo.setLayoutParams(lp2);
                layout.setLayoutParams(rl);
                layout.setId(1);
                linear2.setLayoutParams(r2);
                linear2.setId(2);
                linear3.setLayoutParams(r3);
                linear3.setId(3);
                linear4.setLayoutParams(r4);
                r2.addRule(RelativeLayout.BELOW, 1);
                r3.addRule(RelativeLayout.BELOW, 2);
                r4.addRule(RelativeLayout.BELOW, 3);

                final Cursor cursorTipo = Principal.getTiposCuentas();
                SimpleCursorAdapter cursorAdapterTipo = new SimpleCursorAdapter(seeCuentas.this,android.R.layout.simple_list_item_1,cursorTipo,new String[]{"Tipo"},new int[]{android.R.id.text1},0);
                spTipo.setAdapter(cursorAdapterTipo);
                inputCant.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                textNombre.setText(getString(R.string.account_name));
                tvTipo.setText("Tipo de Cuenta");
                inputNombre.setInputType(InputType.TYPE_CLASS_TEXT);
                textCant.setText(getString(R.string.account_amount));
                inputCant.setText(cant+"");
                cbActivar.setText(getString(R.string.account_active));
                if(cursorTotales.getInt(cursorTotales.getColumnIndex(DBMan.DBTotales.Activa)) == 1){
                    cbActivar.setChecked(true);
                } else {
                    cbActivar.setChecked(false);
                }
                inputNombre.setText(cursorTotales.getString(cursorTotales.getColumnIndex(DBMan.DBTotales.Cuenta)));

                for(int j = 0; j < cursorTipo.getCount(); j++){
                    Cursor value = (Cursor) spTipo.getItemAtPosition(j);
                    int id = value.getInt(value.getColumnIndex("_id"));
                    if(id == tipo){
                        spTipo.setSelection(j);
                        j = cursorTipo.getCount()+1;
                    }
                }


// Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final Double nCant = Double.parseDouble(inputCant.getText().toString());
                        final Double dif = Math.abs(cant - nCant);
                        tipo = cursorTipo.getInt(cursorTipo.getColumnIndex("_id"));
                        if(dif < 0.01){
                            Log.d("Accoun", "Dif < 0.01");
                            Toast.makeText(getApplicationContext(), getString(R.string.small_amount), Toast.LENGTH_LONG).show();
                            Principal.updateTotalesInfo(nCant, inputNombre.getText().toString(), id, cbActivar.isChecked(), tipo);
                        } else if(dif == 0){
                            Principal.updateTotalesInfo(nCant, inputNombre.getText().toString(), id, cbActivar.isChecked(), tipo);
                        } else {
                            AlertDialog.Builder builder2 = new AlertDialog.Builder(seeCuentas.this);
                            builder2.setTitle(getString(R.string.big_change));
                            builder2.setMessage(getString(R.string.big_change_msg));
                            builder2.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent i = new Intent(getApplicationContext(),Gasto.class);
                                    if(cant > nCant){
                                        i.putExtra("Gasto", true);
                                    } else {
                                        i.putExtra("Gasto", false);
                                    }
                                    i.putExtra("Totales", id);
                                    i.putExtra("Cantidad", dif);
                                    startActivity(i);
                                }
                            });
                            builder2.setNeutralButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            builder2.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            AlertDialog alertDialog = builder2.show();
                            alertDialog.setCanceledOnTouchOutside(false);
                        }
                        //finish();
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                layout.addView(cbActivar);
                //layout.addView(inputNombre);

                linear4.addView(tvTipo);
                linear4.addView(spTipo);

                linear3.addView(textCant);
                linear3.addView(inputCant);

                linear2.addView(textNombre);
                linear2.addView(inputNombre);

                relativeLayout.addView(linear4);
                relativeLayout.addView(linear3);
                relativeLayout.addView(linear2);
                relativeLayout.addView(layout);
                //relativeLayout.addView(cbActivar);
                builder.setView(relativeLayout);
                AlertDialog alertDialog = builder.show();
                alertDialog.setCanceledOnTouchOutside(false);
            }
        });

        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                //handleOnBackPress();
            }
        });
    }
    @Override
    public void onResume(){
        super.onResume();
        if(year != null && month == null){
            cursorMoves = Principal.getTotalMovesFecha(id, year);
        } else if(year != null && month != null){
            cursorMoves = Principal.getTotalMovesFecha(id, month, year);
        } else {
            cursorMoves = Principal.getTotalMovesFecha(id);
        }
        adapter.changeCursor(cursorMoves);
        cursorTotales = Principal.getTotal(id);
        cursorTotales.moveToFirst();
        tvCurrent.setText(instance.format(cursorTotales.getDouble(cursorTotales.getColumnIndex(DBMan.DBTotales.CantidadActual))));
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
            Cursor c = Principal.getTotalMovesByFecha(fecha, id);
            int x = c.getCount();
            //c.moveToNext();
            while(c.moveToNext()){
                LinearLayout linear = new LinearLayout(context);
                linear.setWeightSum(2);
                LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,1f);
                //TextView cuentaTV = new TextView(context);
                TextView motivoTV = new TextView(context);
                TextView cantidadTV = new TextView(context);
                linear.setLayoutParams(lp1);
                //cuentaTV.setLayoutParams(lp2);
                motivoTV.setLayoutParams(lp2);
                cantidadTV.setLayoutParams(lp2);
                int idCuenta = (c.getInt(c.getColumnIndex("IdTotales")));
                String cuenta = Principal.getCuentaTotales(idCuenta);
                double cambio = c.getDouble(c.getColumnIndex(DBMan.DBMovimientos.Cambio));
                double cantidad = c.getDouble(c.getColumnIndex(DBMan.DBMovimientos.Cantidad));
                int idMoneda = c.getInt(c.getColumnIndex("IdMoneda"));
                final int idTraspaso = c.getInt(c.getColumnIndex("Traspaso"));
                String moneda = Principal.getMonedaTotales(id);
                int idMotivo = (c.getInt(c.getColumnIndex(DBMan.DBMovimientos.IdMotivo)));
                String motivo = Principal.getMotiveId(idMotivo);
                final int localid = c.getInt(c.getColumnIndex("_id"));
                if(idTraspaso != 0){
                    /*
                    if((idMotivo == 1 || idMotivo == 2 ) && idTraspaso == id) {
                        motivoTV.setTextColor(ContextCompat.getColor(context, R.color.positive_green));
                        cantidadTV.setTextColor(ContextCompat.getColor(context, R.color.positive_green));
                    } else {
                        motivoTV.setTextColor(Color.RED);
                        cantidadTV.setTextColor(Color.RED);
                    }

                     */
                    motivoTV.setTextColor(ContextCompat.getColor(context,R.color.neutral_yellow));
                    cantidadTV.setTextColor(ContextCompat.getColor(context,R.color.neutral_yellow));
                }
                else if(cantidad < 0){
                    motivoTV.setTextColor(Color.RED);
                    cantidadTV.setTextColor(Color.RED);
                } else {
                    //cuentaTV.setTextColor(ContextCompat.getColor(context,R.color.positive_green));
                    motivoTV.setTextColor(ContextCompat.getColor(context,R.color.positive_green));
                    cantidadTV.setTextColor(ContextCompat.getColor(context,R.color.positive_green));
                }
                //cuentaTV.setText(cuenta);
                motivoTV.setText(motivo);
                if(idTraspaso == 0){
                    cantidad = cantidad * cambio;
                    cantidadTV.setText(instance.format(cantidad)+" " + moneda);
                } else {
                    if(idTraspaso != id){
                        cantidad = cantidad * cambio;
                    }
                    cantidadTV.setText(instance.format(cantidad) + " " + moneda);
                }
                cantidadTV.setGravity(Gravity.RIGHT);
                //linear.addView(cuentaTV);
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
                                    i.putExtra("id", localid);
                                } else {
                                    i = new Intent(context, seeTraspaso.class);
                                    i.putExtra("_id", localid);
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
