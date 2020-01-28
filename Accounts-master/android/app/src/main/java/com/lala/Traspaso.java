

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
import androidx.core.content.ContextCompat;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
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

public class Traspaso extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Spinner spFromCuenta, spToCuenta;
    private EditText etCantidad, etComment, etCambio;
    private TextView tvMoneda, tvCambio, tvResultTo, tvResultFrom;
    private Boolean Retiro;
    private Cursor cursorFromCuenta, cursorToCuenta;
    private int idCuentaFrom, idCuentaTo, idMotivo;
    private int[] to = new int[]{android.R.id.text1};
    private String[] from;
    private Context context = this;
    private SimpleCursorAdapter simpleCursorAdapter;
    private String comment;
    private Double cantidad = 0.0, tipoDeCambio, oldCant, oldCambio;
    private RadioButton hoyBtn, ayerBtn, otroDate;
    private String fecha;
    private int id, idMonedaFrom, idMonedaTo, oldCuentaTo = 0, oldCuentaFrom = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traspaso);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent i = getIntent();
        id = i.getIntExtra("_id",0);
        Retiro = i.getBooleanExtra("Retiro",false);
        if(Retiro) this.setTitle(getString(R.string.withrawal));
        else this.setTitle(getString(R.string.transfer));
        Toast.makeText(context,Retiro+"",Toast.LENGTH_SHORT).show();
        spFromCuenta = (Spinner) findViewById(R.id.traspaso_spFrom);
        spToCuenta = (Spinner) findViewById(R.id.traspaso_spTo);
        etCantidad = (EditText) findViewById(R.id.traspaso_etCantidad);
        etComment = (EditText) findViewById(R.id.traspaso_etComment);
        tvMoneda = (TextView) findViewById(R.id.traspaso_tvMoneda);
        hoyBtn = (RadioButton) findViewById(R.id.hoyBtn);
        ayerBtn = (RadioButton) findViewById(R.id.ayerBtn);
        otroDate = (RadioButton) findViewById(R.id.otroBtn);
        etCambio = findViewById(R.id.etCambio);
        tvCambio = findViewById(R.id.tvCambio);
        tvResultFrom = findViewById(R.id.res_from);
        tvResultTo = findViewById(R.id.res_to);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        String d = dateFormat.format(calendar.getTime());
        fecha = d;
        cursorFromCuenta = Principal.getTotales(false);
        cursorToCuenta = Principal.getTotales(false);

        from = new String[]{DBMan.DBTotales.Cuenta};
        simpleCursorAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,cursorFromCuenta,from,to,0);
        spFromCuenta.setAdapter(simpleCursorAdapter);

        simpleCursorAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,cursorToCuenta,from,to,0);
        spToCuenta.setAdapter(simpleCursorAdapter);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.trasp_check);
        fab.setBackgroundColor(Color.BLUE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(EditTextError.checkError(etCantidad, getString(R.string.required_field))){
                }
                else if (verificarDatos()){
                    if(id > 0) {
                        //Principal.actualizarTraspaso(id,nCantidad,nIdFrom,nIdTo,nComment,nIdMotivo,nCambio, nDate);
                        Principal.actualizarTraspaso(id,cantidad, idCuentaFrom, idCuentaTo, comment, idMotivo, tipoDeCambio, fecha);
                        //Principal.actualizarTipoDeCambio(Principal.getIdMoneda(nIdMoneda),Principal.getIdMoneda(Principal.getMonedaId(nIdCuenta)),nCambio);
                        Principal.hideKeyboard(Traspaso.this);
                        finish();
                    } else {
                        if (idMonedaFrom != idMonedaTo) {
                            tipoDeCambio = Double.parseDouble(etCambio.getText().toString());
                            guardarDif();
                            Principal.hideKeyboard(Traspaso.this);
                            finish();
                        } else {
                            guardar();
                            Principal.hideKeyboard(Traspaso.this);
                            finish();
                        }
                    }
                }
                else Toast.makeText(context, getString(R.string.err_data), Toast.LENGTH_SHORT).show();
                Snackbar.make(view, getString(R.string.err_data), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });

        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Principal.hideKeyboard(Traspaso.this);
                finish();
                //handleOnBackPress();
            }
        });

        if(cursorFromCuenta.getCount() == 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(Traspaso.this);
            builder.setTitle(getString(R.string.alert_info_data));
            builder.setMessage(getString(R.string.alert_info_data_msg_tras));

// Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Principal.hideKeyboard(Traspaso.this);
                    finish();
                }
            });
            AlertDialog alertDialog = builder.show();
            alertDialog.setCanceledOnTouchOutside(false);
        }
        hoyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Calendar calendar = Calendar.getInstance();
                String d = dateFormat.format(calendar.getTime());
                fecha = d;
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
                Calendar calendar = Calendar.getInstance();
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy");
                    if(fecha != null){
                        calendar.setTime(sdf.parse(fecha));// all done
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                AlertDialog alertDialog = new DatePickerDialog(Traspaso.this, new DatePickerDialog.OnDateSetListener() {
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

        if(id > 0){
            Cursor c = Principal.getData(id);
            c.moveToFirst();
            comment = c.getString(c.getColumnIndex(DBMan.DBMovimientos.Comment));
            idCuentaFrom = c.getInt(c.getColumnIndex("IdTotales"));
            idCuentaTo = c.getInt(c.getColumnIndex(DBMan.DBMovimientos.Traspaso));
            idMotivo = c.getInt(c.getColumnIndex(DBMan.DBMovimientos.IdMotivo));
            cantidad = c.getDouble(c.getColumnIndex(DBMan.DBMovimientos.Cantidad));
            tipoDeCambio = c.getDouble(c.getColumnIndex(DBMan.DBMovimientos.Cambio));
            oldCambio = tipoDeCambio;
            oldCant = cantidad;
            oldCuentaFrom = idCuentaFrom;
            oldCuentaTo = idCuentaTo;
            etComment.setText(comment);
            etCantidad.setText(cantidad+"");
            if(idMotivo == 1){
                tvMoneda.setText(Principal.getMonedaTotales(c.getInt(c.getColumnIndex(DBMan.DBMovimientos.IdTotales))));
                Retiro = false;
            }
            else{
                tvMoneda.setText(Principal.getMonedaTotales(c.getInt(c.getColumnIndex(DBMan.DBMovimientos.Traspaso))));
                Retiro = true;
            }
            if(idMonedaTo != idMonedaFrom){
                etCambio.setVisibility(View.VISIBLE);
                tvCambio.setVisibility(View.VISIBLE);
                etCambio.setText(tipoDeCambio+"");
            } else {
                etCambio.setVisibility(View.INVISIBLE);
                tvCambio.setVisibility(View.INVISIBLE);
                tipoDeCambio = 1.0;
                etCambio.setText(tipoDeCambio+"");
            }
            fecha = c.getString(c.getColumnIndex(DBMan.DBMovimientos.Fecha));
            otroDate.setText(fecha);
            //if(Retiro) this.setTitle(getString(R.string.withrawal));
            //else this.setTitle(getString(R.string.transfer));
            otroDate.setChecked(true);
            spToCuenta.setOnItemSelectedListener(this);
            spFromCuenta.setOnItemSelectedListener(this);
            for(int j = 0; j < cursorFromCuenta.getCount(); j++){
                Cursor value = (Cursor) spFromCuenta.getItemAtPosition(j);
                int id = value.getInt(value.getColumnIndex("_id"));
                if(id == idCuentaFrom){
                    spFromCuenta.setSelection(j);
                    j = cursorFromCuenta.getCount()+1;
                }
            }
            for(int j = 0; j < cursorToCuenta.getCount(); j++){
                Cursor value = (Cursor) spToCuenta.getItemAtPosition(j);
                int id = value.getInt(value.getColumnIndex("_id"));
                if(id == idCuentaTo){
                    spToCuenta.setSelection(j);
                    j = cursorToCuenta.getCount()+1;
                }
            }
        } else {
            oldCant = 0.0;
            oldCambio = 0.0;
            spToCuenta.setOnItemSelectedListener(this);
            spFromCuenta.setOnItemSelectedListener(this);

            spToCuenta.setSelection(1);
        }

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
                try {
                    cantidad = Double.parseDouble(s.toString());
                } catch (Exception e){
                    cantidad = 0.0;
                }
                actualizarResulCant();
            }
        });


        etCambio.addTextChangedListener(new TextWatcher() {
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
                if(cantidad > 0){
                    actualizarResulCant();
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner sp = (Spinner) parent;
        if(sp.getId()==R.id.traspaso_spFrom) {
            // cuentaFrom = cursorFromCuenta.getString(cursorFromCuenta.getColumnIndex(DBMan.DBTotales.Cuenta));
            idCuentaFrom = cursorFromCuenta.getInt(cursorFromCuenta.getColumnIndex("_id"));
            tvMoneda.setText(Principal.getMonedaTotales(cursorFromCuenta.getInt(cursorFromCuenta.getColumnIndex("_id"))));
            Toast.makeText(getApplicationContext(),""+idCuentaFrom,Toast.LENGTH_SHORT).show();

        } else if (sp.getId() == R.id.traspaso_spTo){
            idCuentaTo = cursorToCuenta.getInt(cursorToCuenta.getColumnIndex("_id"));
            Toast.makeText(getApplicationContext(),""+idCuentaTo,Toast.LENGTH_SHORT).show();
        }
        idMonedaFrom = cursorFromCuenta.getInt(cursorFromCuenta.getColumnIndex("IdMoneda"));
        idMonedaTo = cursorToCuenta.getInt(cursorToCuenta.getColumnIndex("IdMoneda"));
        if(idMonedaFrom != idMonedaTo){
            tvCambio.setVisibility(View.VISIBLE);
            etCambio.setVisibility(View.VISIBLE);
            if (Retiro) {
                tipoDeCambio = Principal.getTipodeCambio(idMonedaTo, idMonedaFrom);
                etCambio.setText(tipoDeCambio+"");
            }
            else {
                tipoDeCambio = Principal.getTipodeCambio(idMonedaFrom, idMonedaTo);
                etCambio.setText(tipoDeCambio+"");
            }
        } else {
            tvCambio.setVisibility(View.INVISIBLE);
            etCambio.setVisibility(View.INVISIBLE);
            tipoDeCambio = 1.0;
            etCambio.setText(tipoDeCambio+"");
        }
        if(cantidad > 0.0){
            actualizarResulCant();
        } else if (idCuentaFrom != idCuentaTo){
            NumberFormat instance = NumberFormat.getInstance();
            instance.setMinimumFractionDigits(2);
            double cantidadActualTo = cursorToCuenta.getDouble(cursorToCuenta.getColumnIndex(DBMan.DBTotales.CantidadActual));
            double cantidadActualFrom = cursorFromCuenta.getDouble(cursorFromCuenta.getColumnIndex(DBMan.DBTotales.CantidadActual));
            tvResultFrom.setVisibility(View.VISIBLE);
            tvResultFrom.setText(instance.format(cantidadActualFrom));
            tvResultTo.setVisibility(View.VISIBLE);
            tvResultTo.setText(instance.format(cantidadActualTo));
            if(cantidadActualFrom > 0){
                tvResultFrom.setTextColor(ContextCompat.getColor(context, R.color.positive_green));
            } else {
                tvResultFrom.setTextColor(Color.RED);
            }
            if(cantidadActualTo > 0){
                tvResultTo.setTextColor(ContextCompat.getColor(context, R.color.positive_green));
            } else {
                tvResultTo.setTextColor(Color.RED);
            }
        } else {
            tvResultFrom.setVisibility(View.INVISIBLE);
            tvResultTo.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void actualizarResulCant(){
        if(idCuentaFrom == idCuentaTo){
            tvResultFrom.setVisibility(View.INVISIBLE);
            tvResultTo.setVisibility(View.INVISIBLE);
            return;
        }
        double cantidadActualTo = cursorToCuenta.getDouble(cursorToCuenta.getColumnIndex(DBMan.DBTotales.CantidadActual));
        double cantidadActualFrom = cursorFromCuenta.getDouble(cursorFromCuenta.getColumnIndex(DBMan.DBTotales.CantidadActual));
        if(id > 0){
            //cantidadActual -= cursorMov.getDouble(cursorMov.getColumnIndex(DBMan.DBMovimientos.Cantidad)) *
            //        cursorMov.getDouble(cursorMov.getColumnIndex(DBMan.DBMovimientos.Cambio));
        }
        double newCantidad = cantidad;
        double cambio = 1.0;
        try {
            cambio = Double.parseDouble(etCambio.getText().toString());
        } catch (NumberFormatException e){
            cambio = 1.0;
        }
        double totalCantidadTo = cantidadActualTo;
        double totalCantidadFrom = cantidadActualFrom;
        if(!Retiro){
            totalCantidadFrom = totalCantidadFrom - (newCantidad);
            totalCantidadTo = totalCantidadTo + (newCantidad / cambio);
        } else {
            totalCantidadFrom = totalCantidadFrom - (newCantidad * cambio);
            totalCantidadTo = totalCantidadTo + (newCantidad);
        }
        if(id > 0){
            if(!Retiro){
                if(idCuentaFrom == oldCuentaFrom){
                    totalCantidadFrom = totalCantidadFrom + (oldCant);
                }
                if(idCuentaTo == oldCuentaTo){
                    totalCantidadTo = totalCantidadTo - (oldCant * oldCambio);
                }
            } else {
                if(idCuentaFrom == oldCuentaFrom){
                    totalCantidadFrom = totalCantidadFrom + (oldCant * oldCambio);
                }
                if(idCuentaTo == oldCuentaTo){
                    totalCantidadTo = totalCantidadTo - (oldCant);
                }
            }
        }

        NumberFormat instance = NumberFormat.getInstance();
        instance.setMinimumFractionDigits(2);

        tvResultFrom.setVisibility(View.VISIBLE);
        tvResultFrom.setText(instance.format(totalCantidadFrom));
        tvResultTo.setVisibility(View.VISIBLE);
        tvResultTo.setText(instance.format(totalCantidadTo));
        if(totalCantidadFrom > 0){
            tvResultFrom.setTextColor(ContextCompat.getColor(context, R.color.positive_green));
        } else {
            tvResultFrom.setTextColor(Color.RED);
        }
        if(totalCantidadTo > 0){
            tvResultTo.setTextColor(ContextCompat.getColor(context, R.color.positive_green));
        } else {
            tvResultTo.setTextColor(Color.RED);
        }
    }


    private boolean verificarDatos(){

        try{
            cantidad = Double.parseDouble(etCantidad.getText().toString());
        }catch (Exception e){
            return false;
        }
        if(EditTextError.checkError(etComment, getString(R.string.required_field))){
            comment = null;
            etComment.setError(null);
        }
        else comment = etComment.getText().toString();
        if(idCuentaFrom == idCuentaTo){
            Toast.makeText(context,"Cuentas iguales",Toast.LENGTH_SHORT).show();
            return false;
        }
        /*if(others){
            motivo = ETMotivo.getText().toString();
        }*/
        return true;
    }

    public void guardar(){
        if(!Retiro) {
            Principal.newTraspaso(idCuentaFrom, idCuentaTo, cantidad, 1.0, comment, fecha);
            Principal.newMoveCuenta(cantidad * -1, idCuentaFrom);
            Principal.newMoveCuenta(cantidad, idCuentaTo);
        } else {
            Principal.newRetiro(idCuentaFrom, idCuentaTo, cantidad, 1.0, comment, fecha);
            Principal.newMoveCuenta(cantidad * -1, idCuentaFrom);
            Principal.newMoveCuenta(cantidad, idCuentaTo);
        }
    }
    public void guardarDif(){
        if(!Retiro) {
            double cant = cantidad * -1;
            if (comment == null) {
                comment = "# " + cantidad + " x " + tipoDeCambio + " = " + cantidad * tipoDeCambio;
            } else
                comment += "  #-# " + cantidad + " x " + tipoDeCambio + " = " + (cantidad * tipoDeCambio);
            //Toast.makeText(context, comment, Toast.LENGTH_SHORT).show();
            //if(tipoDeCambio == null)
            //    tipoDeCambio = 1.0;
            Principal.newTraspaso(idCuentaFrom, idCuentaTo, cantidad, tipoDeCambio, comment, fecha);
            //Principal.newMoveCuenta(cant*tipoDeCambio,idCuenta);
            Principal.actualizarTipoDeCambio(Principal.getMonedaTotales(idCuentaFrom), Principal.getMonedaTotales(idCuentaTo), tipoDeCambio);
            Principal.newMoveCuenta(cant, idCuentaFrom);
            Principal.newMoveCuenta(cantidad * tipoDeCambio, idCuentaTo);
        } else {

            if (comment == null) {
                comment = "# " + cantidad + " x " + tipoDeCambio + " = " + cantidad * tipoDeCambio;
            } else
                comment += "  #-# " + cantidad + " x " + tipoDeCambio + " = " + (cantidad * tipoDeCambio);
            //if(tipoDeCambio == null)
            //    tipoDeCambio = 1.0;
            Principal.newRetiro(idCuentaFrom, idCuentaTo, cantidad, tipoDeCambio, comment, fecha);
            Principal.actualizarTipoDeCambio(Principal.getMonedaTotales(idCuentaTo), Principal.getMonedaTotales(idCuentaFrom), tipoDeCambio);
            Principal.newMoveCuenta(cantidad * tipoDeCambio * -1, idCuentaFrom);
            Principal.newMoveCuenta(cantidad, idCuentaTo);
        }
    }
}
