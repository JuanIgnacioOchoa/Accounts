package com.lala;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;

public class seeCuentas extends AppCompatActivity {

    private TextView tvCurrent, tvInicial;
    private ListView listView;
    private NumberFormat instance;
    private Cursor cursorTotales, cursorMoves;
    private Double cant = 0.0;
    private myAdapter adapter;
    private int id;
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
        tvInicial = (TextView) findViewById(R.id.see_acc_tvIni);
        listView = (ListView) findViewById(R.id.listView_see_acc);


        cant = cursorTotales.getDouble(cursorTotales.getColumnIndex(DBMan.DBTotales.CantidadActual));
        adapter = new myAdapter(getApplicationContext(),cursorMoves);
        tvCurrent.setText(instance.format(cant));
        tvInicial.setText(instance.format(cursorTotales.getDouble(cursorTotales.getColumnIndex(DBMan.DBTotales.CantidadInicial))));
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int motivo = cursorMoves.getInt(cursorMoves.getColumnIndex(DBMan.DBMovimientos.IdMotivo));
                if(motivo==1){
                    Intent i = new Intent(getApplicationContext(),seeTraspaso.class);
                    i.putExtra("_id",cursorMoves.getInt(cursorMoves.getColumnIndex("_id")));
                    startActivity(i);
                }else if(motivo == 2){
                    Intent i = new Intent(getApplicationContext(),seeTraspaso.class);
                    i.putExtra("_id",cursorMoves.getInt(cursorMoves.getColumnIndex("_id")));
                    startActivity(i);
                }
                else {
                    Intent i = new Intent(getApplicationContext(), seeMove.class);
                    i.putExtra("id", cursorMoves.getInt(cursorMoves.getColumnIndex("_id")));
                    startActivity(i);
                }
            }
        });

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
                final LinearLayout layout = new LinearLayout(getApplicationContext());
                final LinearLayout linear2 = new LinearLayout(getApplicationContext());
                final LinearLayout linear3 = new LinearLayout(getApplicationContext());
                final RelativeLayout relativeLayout = new RelativeLayout(getApplicationContext());
// Specify the type of input expected;
                layout.setWeightSum(1);
                linear2.setWeightSum(2);
                linear3.setWeightSum(2);

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

                relativeLayout.setLayoutParams(rl);
                inputCant.setLayoutParams(lp2);
                inputNombre.setLayoutParams(lp2);
                textCant.setLayoutParams(lp2);
                textNombre.setLayoutParams(lp2);
                layout.setLayoutParams(rl);
                layout.setId(2);
                linear2.setLayoutParams(r2);
                linear2.setId(3);
                linear3.setLayoutParams(r3);
                r2.addRule(RelativeLayout.BELOW, 2);
                r3.addRule(RelativeLayout.BELOW, 3);


                inputCant.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                textNombre.setText(getString(R.string.account_name));
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
// Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final Double nCant = Double.parseDouble(inputCant.getText().toString());
                        final Double dif = Math.abs(cant - nCant);

                        if(dif < 0.01){
                            Log.d("Accoun", "Dif < 0.01");
                            Toast.makeText(getApplicationContext(), getString(R.string.small_amount), Toast.LENGTH_LONG).show();
                            Principal.updateTotalesInfo(nCant, inputNombre.getText().toString(), id, cbActivar.isChecked());
                        } else if(dif == 0){
                            Principal.updateTotalesInfo(nCant, inputNombre.getText().toString(), id, cbActivar.isChecked());
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

                linear3.addView(textCant);
                linear3.addView(inputCant);

                linear2.addView(textNombre);
                linear2.addView(inputNombre);

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
            cursorMoves = Principal.getTotalMoves(id, year);
        } else if(year != null && month != null){
            cursorMoves = Principal.getTotalMoves(id, month, year);
        } else {
            cursorMoves = Principal.getTotalMoves(id);
        }
        adapter.changeCursor(cursorMoves);
        cursorTotales = Principal.getTotal(id);
        cursorTotales.moveToFirst();
        tvCurrent.setText(instance.format(cursorTotales.getDouble(cursorTotales.getColumnIndex(DBMan.DBTotales.CantidadActual))));
    }
    public class myAdapter extends CursorAdapter {

        public myAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.list_see_cuentas, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // Find fields to populate in inflated template
            //TextView tvId = (TextView) view.findViewById(R.id.lv_see_cuentas_id);
            TextView tvCantidad = (TextView) view.findViewById(R.id.lv_sse_cuentas_cant);
            TextView tvMotivo = (TextView) view.findViewById(R.id.lv_see_cuentas_motivo);
            TextView tvFecha = (TextView) view.findViewById(R.id.lv_see_cuentas_fecha);
            // Extract properties from cursor
            String idCursor = "" + (cursor.getString(cursor.getColumnIndex("_id")));
            Double cantidad = cursor.getDouble(cursor.getColumnIndex(DBMan.DBMovimientos.Cantidad));
            int idMotivo = cursor.getInt(cursor.getColumnIndex(DBMan.DBMovimientos.IdMotivo));

            String motivo;
            motivo = Principal.getMotiveId(idMotivo);
            //String motivo = Principal.getMotiveId(cursor.getInt(cursor.getColumnIndex(DBMan.DBMovimientos.IdMotivo)));
            String fecha = cursor.getString(cursor.getColumnIndex(DBMan.DBMovimientos.Fecha));
            String cambio = cursor.getString(cursor.getColumnIndex(DBMan.DBMovimientos.Cambio));
            //set
            //tvId.setText(" ");
            tvFecha.setText(fecha);
            tvMotivo.setText(motivo);

            if(idMotivo > 2){
                if(cantidad > 0)  tvCantidad.setTextColor(Color.rgb(11, 79, 34));
                else tvCantidad.setTextColor(Color.RED);
                if(cambio != null) cantidad = cantidad * Double.parseDouble(cambio);
            } else if(idMotivo == 1){
                if(id == cursor.getInt(cursor.getColumnIndex(DBMan.DBMovimientos.Traspaso))){
                    tvCantidad.setTextColor(Color.rgb(11, 79, 34));
                    if(cambio != null) cantidad = cantidad * Double.parseDouble(cambio);
                }
                else tvCantidad.setTextColor(Color.RED);
            } else { //IdMotivo == 2
                if(id == cursor.getInt(cursor.getColumnIndex(DBMan.DBMovimientos.Traspaso))){
                    tvCantidad.setTextColor(Color.rgb(11, 79, 34));
                }
                else{
                    if(cambio != null) cantidad = cantidad * Double.parseDouble(cambio);
                    tvCantidad.setTextColor(Color.RED);
                }
            }

            tvCantidad.setText("$" + instance.format(cantidad));
        }
    }
}
