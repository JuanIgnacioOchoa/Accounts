package com.lala;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;

public class Gasto extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditText etCantidad, etComment, etOtro;
    private TextView tvPersona;
    private Spinner spCuenta, spMotivo, spMoneda;
    private boolean gasto;
    private Cursor cursorCuenta, cursorMoneda, cursorMotivo;
    private int[] to;
    private String[] from;
    private SimpleCursorAdapter simpleCursorAdapter;
    private double cant, tipoDeCambio;
    private String coment, moneda, motivo, monedaCuenta;
    private int idCuenta, idPersona;
    private Context context;
    private NumberFormat instance;
    private int idViaje;
    //private NumberFormat instance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gasto);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        instance = NumberFormat.getInstance();
        instance.setMinimumFractionDigits(2);
        context = this;
        Intent i = this.getIntent();
        gasto = i.getBooleanExtra("Gasto",true);
        idViaje = i.getIntExtra("IdViaje", -1);
        String title = "";
        if(idViaje >= 0){
            title = Principal.getTripNameById(idViaje) + " ";
        }
        idCuenta = i.getIntExtra("Totales", -1);

        etCantidad = (EditText) findViewById(R.id.gasto_etCantidad);
        etComment = (EditText) findViewById(R.id.gasto_etComment);
        etOtro = (EditText) findViewById(R.id.new_move_otro);
        spCuenta = (Spinner) findViewById(R.id.gasto_spFrom);
        spMoneda = (Spinner) findViewById(R.id.gasto_spMoneda);
        spMotivo = (Spinner) findViewById(R.id.gasto_spTo);
        tvPersona = (TextView) findViewById(R.id.TVPersona);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.accept);
        fab.setBackgroundColor(Color.BLUE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(context, cuenta, Toast.LENGTH_SHORT).show();

                Toast.makeText(context, motivo, Toast.LENGTH_SHORT).show();
                if(EditTextError.checkError(etCantidad, getString(R.string.required_field))){
                }
                else if (verificarDatos()){
                    monedaCuenta = Principal.getMonedaTotales(idCuenta);
                    if(!monedaCuenta.equals(moneda) && idCuenta != 1){
                        Toast.makeText(context, getString(R.string.dif_curr) + " " + moneda + " " + monedaCuenta, Toast.LENGTH_SHORT).show();
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(getString(R.string.currency_change));

// Set up the input
                        final EditText input = new EditText(context);
// Specify the type of input expected;
                        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        input.setText(Principal.getTipodeCambio(moneda,monedaCuenta));
                        builder.setView(input);
// Set up the buttons
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                tipoDeCambio = Double.parseDouble(input.getText().toString());
                                guardarDif();
                                finish();
                            }
                        });
                        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        AlertDialog alertDialog = builder.show();
                        alertDialog.setCanceledOnTouchOutside(false);
                    }else {
                        guardar();
                        finish();
                    }
                }
                else Toast.makeText(context, getString(R.string.err_data), Toast.LENGTH_SHORT).show();
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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

        if(gasto){
            this.setTitle(getString(R.string.outcome) + " " + title + "(-)");
            etCantidad.setTextColor(Color.RED);
        } else{
            this.setTitle(getString(R.string.income) + " " + title + "(+)");
            etCantidad.setTextColor(Color.rgb(11, 79, 34));
        }
        cursorMotivo = Principal.getMotive();
        from = new String[]{DBMan.DBMotivo.Motivo};
        to = new int[] {android.R.id.text1};
        simpleCursorAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursorMotivo, from, to,0);
        spMotivo.setAdapter(simpleCursorAdapter);
        if(idCuenta > 0){
            cursorCuenta = Principal.getSingleTotales(idCuenta);
            cursorMoneda = Principal.getSingleMoneda(Principal.getMonedaId(idCuenta));
            cant = i.getDoubleExtra("Cantidad", 0.0);
            etCantidad.setText(cant+"");
        } else {
            if (gasto) {
                cursorCuenta = Principal.getTotalesWithPrestamo();
            } else {
                cursorCuenta = Principal.getTotales(false);
            }
            cursorMoneda = Principal.getMoneda();
        }
        from = new String[]{DBMan.DBTotales.Cuenta};
        simpleCursorAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,cursorCuenta,from,to,0);
        spCuenta.setAdapter(simpleCursorAdapter);


        from = new String[]{"Moneda"};
        simpleCursorAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,cursorMoneda,from,to,0);
        spMoneda.setAdapter(simpleCursorAdapter);

        spCuenta.setOnItemSelectedListener(this);
        spMotivo.setOnItemSelectedListener(this);
        spMoneda.setOnItemSelectedListener(this);
        etOtro.setVisibility(View.GONE);

        if(cursorCuenta.getCount() == 0 || cursorMoneda.getCount() == 0 || cursorMotivo.getCount() == 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(Gasto.this);
            builder.setTitle(getString(R.string.alert_info_data));
            builder.setMessage(getString(R.string.alert_info_data_msg));

// Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            AlertDialog alertDialog = builder.show();
            alertDialog.setCanceledOnTouchOutside(false);

        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner sp = (Spinner) parent;
        if(sp.getId()==R.id.gasto_spFrom) {


            //cuenta = cursorCuenta.getString(cursorCuenta.getColumnIndex(DBMan.DBTotales.Cuenta));
            idCuenta = cursorCuenta.getInt(cursorCuenta.getColumnIndex("_id"));
            if(idCuenta == 1){
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(getString(R.string.who_lend));

// Set up the input
                final Spinner spinner = new Spinner(context);
                final EditText input = new EditText(context);
                LinearLayout layout = new LinearLayout(context);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                layout.setLayoutParams(lp);
                spinner.setLayoutParams(lp);
                input.setLayoutParams(lp);
                final Cursor cursorPersonas = Principal.getPersonas();
                from = new String[]{"Nombre"};
                simpleCursorAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,cursorPersonas,from,to,0);
                spinner.setAdapter(simpleCursorAdapter);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(cursorPersonas.getInt(cursorPersonas.getColumnIndex("_id")) == -1){
                            spinner.setVisibility(View.GONE);
                            input.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
// Specify the type of input expected;
                layout.addView(spinner);
                layout.addView(input);
                builder.setView(layout);
                input.setVisibility(View.GONE);
// Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        idPersona = cursorPersonas.getInt(cursorPersonas.getColumnIndex("_id"));
                        if(idPersona == -1){
                            idPersona = (int) Principal.insertPersona(input.getText().toString());
                        }
                        spCuenta.setVisibility(View.INVISIBLE);
                        tvPersona.setVisibility(View.VISIBLE);
                        tvPersona.setText(Principal.getPersonaNombreById(idPersona));
                        ((TextView)findViewById(R.id.textView2)).setText(getString(R.string.person_));
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog = builder.show();
                alertDialog.setCanceledOnTouchOutside(false);
            }
            //this.position = position;
        } else if(sp.getId()==R.id.gasto_spMoneda) {

            moneda = cursorMoneda.getString(cursorMoneda.getColumnIndex("Moneda"));
        } else if (sp.getId() == R.id.gasto_spTo){
            motivo = cursorMotivo.getString(cursorMotivo.getColumnIndex(DBMan.DBMotivo.Motivo));

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    private boolean verificarDatos(){

        try{
            cant = Double.parseDouble(etCantidad.getText().toString());
            if(gasto) cant = cant * -1;
        }catch (Exception e){
            return false;
        }
        if(EditTextError.checkError(etComment, getString(R.string.required_field))){
            coment = null;
            etComment.setError(null);
        }
        else coment = etComment.getText().toString();
        /*if(others){
            motivo = ETMotivo.getText().toString();
        }*/
        return true;
    }
    public void guardar(){
        if(idCuenta == 1){
            if(coment == null)
                coment = "%-%" +  getString(R.string.loans_for) + " " + motivo;
            else
                coment += "%-%" + getString(R.string.loans_for) + " " + motivo;
        }
        long idMove = Principal.newMove(cant, idCuenta, coment, motivo, moneda,-1);
        Principal.newMoveCuenta(cant,idCuenta);
        if(idViaje >= 0) {
            Principal.updateMoveTrip((int) idMove, idViaje, cant);
        }
        if(idCuenta == 1){
            Principal.createPrestamo(cant * -1, 1, Principal.getIdMoneda(moneda), idPersona, coment, 1.0, idMove);
        }
    }
    public void guardarDif(){
        Toast.makeText(context, getString(R.string.err_data), Toast.LENGTH_SHORT).show();
        if(coment == null){
            coment = "#-#" + cant + " x " + tipoDeCambio + " = " + cant*tipoDeCambio+"#-#";
        } else  coment += "  #-# " + cant + " x " + tipoDeCambio + " = " + (cant*tipoDeCambio)+"#-#";
        if(idCuenta == 1){
            coment += "%-%" + getString(R.string.loans_for) + " " + motivo;
        }
        long idMove = Principal.newMove(cant,idCuenta,coment,motivo,moneda,tipoDeCambio);
        Principal.newMoveCuenta(cant*tipoDeCambio,idCuenta);
        Principal.actualizarTipoDeCambio(moneda,monedaCuenta,tipoDeCambio);
        if(idViaje >= 0) {
            Principal.updateMoveTrip((int) idMove, idViaje, cant);
        }
        if(idCuenta == 1){
            Principal.createPrestamo(cant * -1, 1, Principal.getIdMoneda(moneda), idPersona, coment, 1.0, idMove);
        }
    }
}
