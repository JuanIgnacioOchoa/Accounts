package com.lala;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
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

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Gasto extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditText etCantidad, etComment, etOtro, etCambio;
    private TextView tvPersona, tvCambio, tvResult;
    private Spinner spCuenta, spMotivo, spMoneda;
    private boolean gasto, spSelectedResult = false, fast;
    private Cursor cursorCuenta, cursorMoneda, cursorMotivo, cursorMov;
    private int[] to;
    private String[] from;
    private SimpleCursorAdapter simpleCursorAdapterMoneda, simpleCursorAdapterCuenta, simpleCursorAdapterMotivo, simpleCursorAdapterPersonas;
    private double cant, tipoDeCambio;
    private String coment, title;
    private int idCuenta, idPersona, idMotivo, idMoneda;
    private Context context;
    private NumberFormat instance;
    private int idViaje, idMove;
    private RadioButton ingresoBtn, gastoBtn, hoyBtn, ayerBtn, otroDate;
    private Calendar calendar;
    private String fecha = null;
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
        fast = i.getBooleanExtra("FastAdd", false);
        idViaje = i.getIntExtra("IdViaje", -1);
        idMove = i.getIntExtra("id",0);
        cant = i.getDoubleExtra("Cantidad", 0.0);


        title = "";
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
        gastoBtn = (RadioButton) findViewById(R.id.gastoBtn);
        ingresoBtn = (RadioButton) findViewById(R.id.ingresoBtn);
        hoyBtn = (RadioButton) findViewById(R.id.hoyBtn);
        ayerBtn = (RadioButton) findViewById(R.id.ayerBtn);
        otroDate = (RadioButton) findViewById(R.id.otroBtn);
        etCambio = (EditText) findViewById(R.id.see_move_Tipo_de_cambio);
        tvCambio = (TextView) findViewById(R.id.see_move_tvCambio);
        tvResult = findViewById(R.id.resultCant);
        tvResult.setVisibility(View.INVISIBLE);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.accept);
        fab.setBackgroundColor(Color.BLUE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(EditTextError.checkError(etCantidad, getString(R.string.required_field))){
                }
                else if (verificarDatos()){
                    if(idMove > 0){
                        Principal.actualizarMovimiento(idMove,cant,idCuenta,coment,idMotivo,idMoneda,tipoDeCambio,fecha);
                        if(Principal.getIdMonedaTotales(idCuenta) != idMoneda && idCuenta != 1)
                            Principal.actualizarTipoDeCambio(idMoneda,Principal.getIdMonedaTotales(idCuenta),tipoDeCambio);
                        if(idCuenta == 1){
                            if(tipoDeCambio <= 0.0)
                                tipoDeCambio = 1.0;
                            Principal.updatePrestamoFromMove(idMotivo, cant, tipoDeCambio, idMoneda);
                        }
                        Principal.hideKeyboard(Gasto.this);
                        finish();
                    } else {
                        if(Principal.getIdMonedaTotales(idCuenta) != idMoneda && idCuenta != 1){
                            tipoDeCambio = Double.parseDouble(etCambio.getText().toString());
                            if(tipoDeCambio == 0.0){
                                tipoDeCambio = 1;
                            }
                            guardarDif();
                            Principal.hideKeyboard(Gasto.this);
                            finish();
                        } else {
                            guardar();
                            Principal.hideKeyboard(Gasto.this);
                            finish();
                        }
                    }
                }
                else Toast.makeText(context, getString(R.string.err_data), Toast.LENGTH_SHORT).show();
            }
        });

        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Principal.hideKeyboard(Gasto.this);
                finish();
                //handleOnBackPress();
            }
        });



        ingresoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ingresoBtn.setChecked(true);
                etCantidad.setTextColor(Color.rgb(11, 79, 34));
                gasto = false;
                actualizarResulCant();
                spSelectedResult = true;
            }
        });
        gastoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //gastoBtn.setChecked(true);
                etCantidad.setTextColor(Color.RED);
                gasto = true;
                actualizarResulCant();
                spSelectedResult = true;
            }
        });

        hoyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fecha = null;
            }
        });

        ayerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                fecha = dateFormat.format(calendar.getTime());
                int i = 0;
            }
        });

        otroDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar = Calendar.getInstance();
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy");
                    if(fecha != null){
                        calendar.setTime(sdf.parse(fecha));// all done
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                AlertDialog alertDialog = new DatePickerDialog(Gasto.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        DateFormatSymbols dfs = new DateFormatSymbols();
                        String[] months = dfs.getMonths();
                        String date = months[monthOfYear].substring(0,3) + "-" + String.valueOf(dayOfMonth) + "-" + String.valueOf(year);
                        otroDate.setText(date);
                        String m;
                        String d;
                        if(dayOfMonth < 10) d = "0" + dayOfMonth;
                        else d = "" + dayOfMonth;
                        if(monthOfYear < 9) m = "0"+(monthOfYear+1);
                        else m = (monthOfYear+1)+"";
                        fecha = year + "-" + m + "-" + d;
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                //AlertDialog alertDialog2 = alertDialog.show();
                alertDialog.show();
                alertDialog.setCanceledOnTouchOutside(false);
            }
        });

        etCantidad.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Log.d("Accounts", "Before");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Log.d("Accounts", "On");
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Log.d("Accounts", "After");
                spSelectedResult = true;
                actualizarResulCant();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(gasto){
            //this.setTitle(getString(R.string.outcome) + " " + title + "(-)");
            //gastoBtn.setBackgroundColor(Color.BLUE);
            //ingresoBtn.setBackgroundColor(Color.TRANSPARENT);
            etCantidad.setTextColor(Color.RED);
        } else{
            //this.setTitle(getString(R.string.income) + " " + title + "(+)");
            //gastoBtn.setBackgroundColor(Color.TRANSPARENT);
            //ingresoBtn.setBackgroundColor(Color.BLUE);
            etCantidad.setTextColor(Color.rgb(11, 79, 34));
        }



        cursorMotivo = Principal.getMotive();
        from = new String[]{DBMan.DBMotivo.Motivo};
        to = new int[] {android.R.id.text1};
        simpleCursorAdapterMotivo = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursorMotivo, from, to,0);
        spMotivo.setAdapter(simpleCursorAdapterMotivo);
        if(idCuenta > 0 && !fast){
            cursorCuenta = Principal.getSingleTotales(idCuenta);
            cursorMoneda = Principal.getSingleMoneda(Principal.getMonedaId(idCuenta));
            if(cant != 0.0)
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
        simpleCursorAdapterCuenta = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,cursorCuenta,from,to,0);
        spCuenta.setAdapter(simpleCursorAdapterCuenta);
        from = new String[]{"Moneda"};
        simpleCursorAdapterMoneda = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,cursorMoneda,from,to,0);
        spMoneda.setAdapter(simpleCursorAdapterMoneda);

        spMoneda.setOnItemSelectedListener(this);
        spCuenta.setOnItemSelectedListener(this);
        spMotivo.setOnItemSelectedListener(this);
        etOtro.setVisibility(View.GONE);

        if(idCuenta > 0 && fast){
            for(int j = 0; j < cursorCuenta.getCount(); j++){
                Cursor value = (Cursor) spCuenta.getItemAtPosition(j);
                int id = value.getInt(value.getColumnIndex("_id"));
                if(id == idCuenta){
                    spCuenta.setSelection(j);
                    j = cursorCuenta.getCount()+1;
                }
            }
            idMoneda = Principal.getIdMonedaTotales(idCuenta);
            for(int j = 0; j < cursorMoneda.getCount(); j++){
                Cursor value = (Cursor) spMoneda.getItemAtPosition(j);
                int id = value.getInt(value.getColumnIndex("_id"));
                if(id == idMoneda){
                    spMoneda.setSelection(j);
                    j = cursorMoneda.getCount()+1;
                }
            }
        }
        if(idMove != 0){
            cursorMov = Principal.getData(idMove);
            cursorMov.moveToNext();
            idCuenta = cursorMov.getInt(cursorMov.getColumnIndex(DBMan.DBMovimientos.IdTotales));
            idMotivo = cursorMov.getInt(cursorMov.getColumnIndex(DBMan.DBMovimientos.IdMotivo));
            idMoneda = cursorMov.getInt(cursorMov.getColumnIndex(DBMan.DBMovimientos.IdMoneda));
            coment = cursorMov.getString(cursorMov.getColumnIndex(DBMan.DBMovimientos.Comment));
            String nfecha = cursorMov.getString(cursorMov.getColumnIndex(DBMan.DBMovimientos.Fecha));
            cursorMoneda = Principal.getMonedaWith(idMoneda);
            cursorCuenta = Principal.getTotalesWith(idCuenta);
            cursorMotivo = Principal.getMotiveWith(idMotivo);
            simpleCursorAdapterCuenta.changeCursor(cursorCuenta);
            simpleCursorAdapterMoneda.changeCursor(cursorMoneda);
            simpleCursorAdapterMotivo.changeCursor(cursorMotivo);
            fecha = cursorMov.getString(cursorMov.getColumnIndex("nFecha"));
            cant = cursorMov.getDouble(cursorMov.getColumnIndex(DBMan.DBMovimientos.Cantidad));
            if(Principal.getIdMonedaTotales(idCuenta) != idMoneda){
                etCambio.setVisibility(View.VISIBLE);
                tvCambio.setVisibility(View.VISIBLE);
                tipoDeCambio = cursorMov.getDouble(cursorMov.getColumnIndex(DBMan.DBMovimientos.Cambio));
            } else {
                etCambio.setVisibility(View.GONE);
                tvCambio.setVisibility(View.GONE);
                etCambio.setText(instance.format(1.0));
                tipoDeCambio = 1.0;
            }
            idViaje = cursorMov.getInt(cursorMov.getColumnIndex(DBMan.DBMovimientos.IdTrip));
            title = Principal.getTripNameById(idViaje) + " ";

            etCambio.setText((instance.format(tipoDeCambio)));
            etComment.setText(coment);
            otroDate.setChecked(true);
            otroDate.setText(nfecha);
            if(cant > 0){
                ingresoBtn.setChecked(true);
                etCantidad.setTextColor(Color.rgb(11, 79, 34));
                gasto = false;
            } else if (cant < 0){
                cant = cant*-1;
                gastoBtn.setChecked(true);
                etCantidad.setTextColor(Color.RED);
            } else {
                etCantidad.setTextColor(Color.BLACK);
            }
            for(int j = 0; j < cursorCuenta.getCount(); j++){
                Cursor value = (Cursor) spCuenta.getItemAtPosition(j);
                int id = value.getInt(value.getColumnIndex("_id"));
                if(id == idCuenta){
                    spCuenta.setSelection(j);
                    j = cursorCuenta.getCount()+1;
                }
            }
            for(int j = 0; j < cursorMotivo.getCount(); j++){
                Cursor value = (Cursor) spMotivo.getItemAtPosition(j);
                int id = value.getInt(value.getColumnIndex("_id"));
                if(id == idMotivo){
                    spMotivo.setSelection(j);
                    j = cursorMotivo.getCount()+1;
                }
            }
            for(int j = 0; j < cursorMoneda.getCount(); j++){
                Cursor value = (Cursor) spMoneda.getItemAtPosition(j);
                int id = value.getInt(value.getColumnIndex("_id"));
                if(id == idMoneda){
                    spMoneda.setSelection(j);
                    j = cursorMoneda.getCount()+1;
                }
            }
            etCantidad.setText((cant + ""));
        }
        if(idMove > 0){
            this.setTitle(title);
        } else {
            this.setTitle("Crear Movimiento " + title);
        }




        if(idCuenta > 1 && Principal.getIdMonedaTotales(idCuenta) != idMoneda && idCuenta != 1){
            tvCambio.setVisibility(View.VISIBLE);
            etCambio.setVisibility(View.VISIBLE);
        } else {
            tvCambio.setVisibility(View.GONE);
            etCambio.setVisibility(View.GONE);
            etCambio.setText(instance.format(1.0));
            tipoDeCambio = 1.0;
        }

        if(cursorCuenta.getCount() == 0 || cursorMoneda.getCount() == 0 || cursorMotivo.getCount() == 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(Gasto.this);
            builder.setTitle(getString(R.string.alert_info_data));
            builder.setMessage(getString(R.string.alert_info_data_msg));

// Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Principal.hideKeyboard(Gasto.this);
                    if(cursorCuenta.getCount() == 0){
                        Intent i = new Intent(context, NewAccount.class);
                        startActivity(i);
                    } else if(cursorMotivo.getCount() == 0){
                        Intent i = new Intent(context, NewMotive.class);
                        startActivity(i);
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
                simpleCursorAdapterPersonas = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,cursorPersonas,from,to,0);
                spinner.setAdapter(simpleCursorAdapterPersonas);
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
            } else {
                if(Principal.getIdMonedaTotales(idCuenta) != idMoneda){
                    etCambio.setVisibility(View.VISIBLE);
                    tvCambio.setVisibility(View.VISIBLE);
                    double scambio = Principal.getTipodeCambio(idMoneda, Principal.getIdMonedaTotales(idCuenta));
                    etCambio.setText(instance.format(scambio));
                } else {
                    etCambio.setVisibility(View.GONE);
                    tvCambio.setVisibility(View.GONE);
                    etCambio.setText(instance.format(1.0));
                }
            }
            if(spSelectedResult)
                actualizarResulCant();
            //this.position = position;
        } else if(sp.getId()==R.id.gasto_spMoneda) {

            idMoneda = cursorMoneda.getInt(cursorMoneda.getColumnIndex("_id"));
            if(Principal.getIdMonedaTotales(idCuenta) != idMoneda){
                etCambio.setVisibility(View.VISIBLE);
                tvCambio.setVisibility(View.VISIBLE);
                double scambio = Principal.getTipodeCambio(idMoneda, Principal.getIdMonedaTotales(idCuenta));
                etCambio.setText(instance.format(scambio));
            } else {
                etCambio.setVisibility(View.GONE);
                tvCambio.setVisibility(View.GONE);
                etCambio.setText(instance.format(1.0));
            }
            if(spSelectedResult)
                actualizarResulCant();
        } else if (sp.getId() == R.id.gasto_spTo){
            idMotivo = cursorMotivo.getInt(cursorMotivo.getColumnIndex("_id"));

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void actualizarResulCant(){
        //cursorCuenta.moveToFirst();
        double cantidadActual = cursorCuenta.getDouble(cursorCuenta.getColumnIndex(DBMan.DBTotales.CantidadActual));
        if(idMove > 0){
            cantidadActual -= cursorMov.getDouble(cursorMov.getColumnIndex(DBMan.DBMovimientos.Cantidad)) *
                    cursorMov.getDouble(cursorMov.getColumnIndex(DBMan.DBMovimientos.Cambio));
        }
        double newCantidad = 0;
        double cambio = 1.0;
        try {
            newCantidad = Double.parseDouble(etCantidad.getText().toString());
        } catch (NumberFormatException e){

        }try {
            cambio = Double.parseDouble(etCambio.getText().toString());
        } catch (NumberFormatException e){

        }
        newCantidad = newCantidad * cambio;
        double totalCantidad = cantidadActual + newCantidad;
        if(gasto){
            totalCantidad = cantidadActual - newCantidad;
        }
        tvResult.setVisibility(View.VISIBLE);
        tvResult.setText(instance.format(totalCantidad));
        if(totalCantidad > 0){
            tvResult.setTextColor(Color.rgb(11, 79, 34));
        } else {
            tvResult.setTextColor(Color.RED);
        }
        Log.d("Accounts", "After: " + cantidadActual + " +/- " + newCantidad + " = " + gasto);
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
                coment = "%-%" +  getString(R.string.loans_for) + " " + Principal.getMotiveId(idMotivo);
            else
                coment += "%-%" + getString(R.string.loans_for) + " " + Principal.getMotiveId(idMotivo);
        }
        long idMove = Principal.newMove(cant, idCuenta, coment, idMotivo, idMoneda,1.0, fecha);
        Principal.newMoveCuenta(cant,idCuenta);
        if(idViaje > 0) {
            Principal.updateMoveTrip((int) idMove, idViaje, cant);
        }
        if(idCuenta == 1){
            Principal.createPrestamo(cant, 1, idMoneda, idPersona, coment, 1.0, idMove);
        }
    }
    public void guardarDif(){
        Toast.makeText(context, getString(R.string.err_data), Toast.LENGTH_SHORT).show();
        if(coment == null){
            coment = "#-#" + cant + " x " + tipoDeCambio + " = " + cant*tipoDeCambio+"#-#";
        } else  coment += "  #-# " + cant + " x " + tipoDeCambio + " = " + (cant*tipoDeCambio)+"#-#";
        if(idCuenta == 1){
            coment += "%-%" + getString(R.string.loans_for) + " " + Principal.getMotiveId(idMotivo);
        }
        long idMove = Principal.newMove(cant,idCuenta,coment,idMotivo,idMoneda,tipoDeCambio, fecha);
        Principal.newMoveCuenta(cant*tipoDeCambio,idCuenta);
        Principal.actualizarTipoDeCambio(idMoneda,Principal.getIdMonedaTotales(idCuenta),tipoDeCambio);
        if(idViaje > 0) {
            Principal.updateMoveTrip((int) idMove, idViaje, cant);
        }
        if(idCuenta == 1){
            Principal.createPrestamo(cant * -1, 1, idMoneda, idPersona, coment, 1.0, idMove);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(idMove > 0){
            getMenuInflater().inflate(R.menu.menu_see, menu);
            return true;
        }

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int idM = item.getItemId();
        if(idM == R.id.action_delete){
            Principal.eliminarMov(idMove);
            Toast.makeText(context,getString(R.string.del_move), Toast.LENGTH_SHORT).show();
            Principal.hideKeyboard(Gasto.this);
            finish();
        } else if(idM == R.id.action_trip){
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(getString(R.string.currency_change));

// Set up the input
            final Spinner spinner = new Spinner(context);
// Specify the type of input expected;
            final Cursor c = Principal.getTrips();
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,c,new String[]{DBMan.DBViaje.Nombre},to,0);
            spinner.setAdapter(adapter);
            builder.setView(spinner);
// Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Principal.addMoveToTrip(idMove, c.getInt(c.getColumnIndex("_id")));
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
        return super.onOptionsItemSelected(item);
    }
}
