package com.lala;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.NumberFormat;

public class Gasto extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditText etCantidad, etComment, etOtro;
    private Spinner spCuenta, spMotivo, spMoneda;
    private boolean gasto;
    private Cursor cursorCuenta, cursorMoneda, cursorMotivo;
    private int[] to;
    private String[] from;
    private SimpleCursorAdapter simpleCursorAdapter;
    private double cant, tipoDeCambio;
    private String coment, moneda, motivo, monedaCuenta;
    private int idCuenta;
    private Context context;
    private NumberFormat instance;

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
        //cuenta = "valiendo verga";
        Intent i = this.getIntent();
        gasto = i.getBooleanExtra("Gasto",true);
        if(gasto){
            this.setTitle("Gasto (-)");
        } else this.setTitle("Ingreso (+)");

        etCantidad = (EditText) findViewById(R.id.gasto_etCantidad);
        etComment = (EditText) findViewById(R.id.gasto_etComment);
        etOtro = (EditText) findViewById(R.id.new_move_otro);
        spCuenta = (Spinner) findViewById(R.id.gasto_spFrom);
        spMoneda = (Spinner) findViewById(R.id.gasto_spMoneda);
        spMotivo = (Spinner) findViewById(R.id.gasto_spTo);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.accept);
        fab.setBackgroundColor(Color.BLUE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(context, cuenta, Toast.LENGTH_SHORT).show();

                Toast.makeText(context, motivo, Toast.LENGTH_SHORT).show();
                if(EditTextError.checkError(etCantidad)){
                }
                else if (verificarDatos()){
                    monedaCuenta = Principal.getMonedaTotales(idCuenta);
                    Toast.makeText(context, "1 " + moneda, Toast.LENGTH_SHORT).show();
                    Toast.makeText(context, "2 " + monedaCuenta, Toast.LENGTH_SHORT).show();
                    if(!monedaCuenta.equals(moneda)){
                        Toast.makeText(context, "Monedas diferentes", Toast.LENGTH_SHORT).show();
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Tipo de cambio");

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
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builder.show();
                    }else {
                        guardar();
                        finish();
                    }
                }
                else Toast.makeText(context, "Error en los datos", Toast.LENGTH_SHORT).show();
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                //handleOnBackPress();
            }
        });
        if(gasto){
            this.setTitle("Gasto (-)");
            etCantidad.setTextColor(Color.RED);
        } else{
            this.setTitle("Ingreso (+)");
            etCantidad.setTextColor(Color.rgb(11, 79, 34));
        }
        cursorMotivo = Principal.getMotive();
        from = new String[]{DBMan.DBMotivo.Motivo};
        to = new int[] {android.R.id.text1};
        simpleCursorAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursorMotivo, from, to,0);
        spMotivo.setAdapter(simpleCursorAdapter);

        cursorCuenta = Principal.getTotales();
        from = new String[]{DBMan.DBTotales.Cuenta};
        simpleCursorAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,cursorCuenta,from,to,0);
        spCuenta.setAdapter(simpleCursorAdapter);

        cursorMoneda = Principal.getMoneda();
        from = new String[]{"Moneda"};
        simpleCursorAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,cursorMoneda,from,to,0);

        spMoneda.setAdapter(simpleCursorAdapter);
        spCuenta.setOnItemSelectedListener(this);
        spMotivo.setOnItemSelectedListener(this);
        spMoneda.setOnItemSelectedListener(this);
        etOtro.setVisibility(View.GONE);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner sp = (Spinner) parent;
        if(sp.getId()==R.id.gasto_spFrom) {


            //cuenta = cursorCuenta.getString(cursorCuenta.getColumnIndex(DBMan.DBTotales.Cuenta));
            idCuenta = cursorCuenta.getInt(cursorCuenta.getColumnIndex("_id"));
            //this.position = position;
        } else if(sp.getId()==R.id.gasto_spMoneda) {

            moneda = cursorMoneda.getString(cursorMoneda.getColumnIndex("Moneda"));
            /*
            if(!spDataMotivo[position].equals("otros")){
                etOtro.setVisibility(View.GONE);
                etOtro.setEnabled(false);
                etOtro.setInputType(InputType.TYPE_NULL);
                etOtro.setFocusable(false);
                etOtro.setFocusableInTouchMode(false);
                etOtro = spDataMotivo[position];
                //others = false;
            }else{
                etOtro.setVisibility(View.VISIBLE);
                etOtro.setEnabled(true);
                etOtro.setInputType(InputType.TYPE_CLASS_TEXT);
                etOtro.setFocusable(true);
                etOtro.setFocusableInTouchMode(true);

               *///others = true;
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
        if(EditTextError.checkError(etComment)){
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
        Principal.newMove(cant, idCuenta, coment, motivo, moneda,-1);
        Principal.newMoveCuenta(cant,idCuenta);
    }
    public void guardarDif(){
        Toast.makeText(context, "Error en los datos", Toast.LENGTH_SHORT).show();
        if(coment == null){
            coment = "# " + cant + " x " + tipoDeCambio + " = " + cant*tipoDeCambio;
        } else  coment += "  #-# " + cant + " x " + tipoDeCambio + " = " + instance.format(cant*tipoDeCambio);
        Toast.makeText(context, coment, Toast.LENGTH_SHORT).show();
        Principal.newMove(cant,idCuenta,coment,motivo,moneda,tipoDeCambio);
        Principal.newMoveCuenta(cant*tipoDeCambio,idCuenta);
        Principal.actualizarTipoDeCambio(moneda,monedaCuenta,tipoDeCambio);
    }
}
