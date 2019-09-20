package com.lala;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.util.Calendar;

public class seeMove extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Spinner spMotivo, spMoneda, spCuenta;
    private EditText etCantidad, etComentario, etOtro, etCambio, etFecha;
    private TextView tvCambio;
    private int id, idMoneda, idCuenta, idMotivo, nIdMoneda, nIdCuenta, nIdMotivo;
    private Cursor c, cursorMotivo, cursorCuenta, cursorMoneda;
    private SimpleCursorAdapter simpleCursorAdapter;
    private int[] to;
    private String[] from;
    private NumberFormat instance;
    private Double cantidad, nCantidad, cambio, nCambio;
    private String comment, nComment, date, nDate;
    private boolean edit = false, gasto;
    private Context context = this;
    private Calendar calendar;
    private int idViaje;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_move);
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
        id = i.getIntExtra("id",0);
        c = Principal.getData(id);
        c.moveToFirst();
        calendar = Calendar.getInstance();
        String title = "";

        idViaje = c.getInt(c.getColumnIndex(DBMan.DBMovimientos.IdTrip));

        idMoneda = c.getInt(c.getColumnIndex("IdMoneda"));
        nIdMoneda = idMoneda;
        idCuenta = c.getInt(c.getColumnIndex("IdTotales"));
        nIdCuenta = idCuenta;
        idMotivo = c.getInt(c.getColumnIndex(DBMan.DBMovimientos.IdMotivo));
        nIdMotivo = idMotivo;
        comment = c.getString(c.getColumnIndex(DBMan.DBMovimientos.Comment));
        nComment = comment;
        cursorMotivo = Principal.getMotive(idMotivo);
        cursorCuenta = Principal.getTotales(idCuenta);
        cursorMoneda = Principal.getMoneda();

        instance = NumberFormat.getInstance();
        instance.setMinimumFractionDigits(2);

        spMotivo = (Spinner) findViewById(R.id.see_move_spMotivo);
        spMoneda = (Spinner) findViewById(R.id.see_move_spMoneda);
        spCuenta = (Spinner) findViewById(R.id.see_move_spCuenta);
        etCantidad = (EditText) findViewById(R.id.see_move_etCantidad);
        etComentario = (EditText) findViewById(R.id.see_move_etComment);
        etOtro = (EditText) findViewById(R.id.see_move_otro);
        etCambio = (EditText) findViewById(R.id.see_move_Tipo_de_cambio);
        tvCambio = (TextView) findViewById(R.id.see_move_tvCambio);
        etFecha = (EditText) findViewById(R.id.see_move_date);

        cantidad = c.getDouble(c.getColumnIndex(DBMan.DBMovimientos.Cantidad));
        gasto = (cantidad < 0);
        if(idViaje > 0){
            title = Principal.getTripNameById(idViaje) + " ";
        }
        if(gasto){
            this.setTitle(getString(R.string.outcome) + " " + title + "(-)");
            etCantidad.setTextColor(Color.RED);
        } else{
            this.setTitle(getString(R.string.income) + " " + title + "(+)");
            etCantidad.setTextColor(Color.rgb(11, 79, 34));
        }

        etCambio.setFocusable(false);
        etCambio.setEnabled(false);
        etFecha.setFocusable(false);
        etFecha.setEnabled(false);
        etOtro.setFocusable(false);
        etOtro.setEnabled(false);
        etComentario.setFocusable(false);
        etComentario.setEnabled(false);
        etCantidad.setFocusable(false);
        etCantidad.setEnabled(false);
        spCuenta.setFocusable(false);
        spMoneda.setFocusable(false);
        spMotivo.setFocusable(false);
        spCuenta.setEnabled(false);
        spMoneda.setEnabled(false);
        spMotivo.setEnabled(false);

        etFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new DatePickerDialog(seeMove.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        DateFormatSymbols dfs = new DateFormatSymbols();
                        String[] months = dfs.getMonths();
                        String date = months[monthOfYear].substring(0,3) + "-" + String.valueOf(dayOfMonth) + "-" + String.valueOf(year);
                        etFecha.setText(date);
                        String m;
                        String d;
                        if(dayOfMonth < 10) d = "0" + dayOfMonth;
                        else d = "" + dayOfMonth;
                        if(monthOfYear < 9) m = "0"+(monthOfYear+1);
                        else m = (monthOfYear+1)+"";
                        nDate = year + "-" + m + "-" + d;
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                //AlertDialog alertDialog2 = alertDialog.show();
                alertDialog.show();
                alertDialog.setCanceledOnTouchOutside(false);
            }
        });

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_seeMove);
        fab.setBackgroundColor(Color.BLUE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edit) {
                    if (EditTextError.checkError(etCantidad, getString(R.string.required_field))) {
                    } else if (verificarDatos()) {
                        Principal.actualizarMovimiento(id,nCantidad,nIdCuenta,nComment,nIdMotivo,nIdMoneda,nCambio,nDate);
                        if(!Principal.getIdMoneda(nIdMoneda).equals(Principal.getIdMoneda(Principal.getMonedaId(nIdCuenta))))
                            Principal.actualizarTipoDeCambio(Principal.getIdMoneda(nIdMoneda),Principal.getIdMoneda(Principal.getMonedaId(nIdCuenta)),nCambio);
                        if(idCuenta == 1){
                            if(nCambio <= 0.0 || nCambio == null)
                                nCambio = 1.0;
                            Principal.updatePrestamoFromMove(Principal.getIdPrestamoByMoveId(id), nCantidad, 1.0, idMoneda);
                        }
                        finish();
                    } else Toast.makeText(context, getString(R.string.err_data), Toast.LENGTH_SHORT).show();

                } else{
                    edit = true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        fab.setImageDrawable(getResources().getDrawable(R.drawable.check, context.getTheme()));
                    } else fab.setImageDrawable(getResources().getDrawable(R.drawable.check));
                    etCambio.setFocusable(true);
                    etCambio.setEnabled(true);
                    etCambio.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    etCambio.setFocusableInTouchMode(true);
                    etFecha.setEnabled(true);
                    etOtro.setFocusable(true);
                    etOtro.setEnabled(true);
                    etOtro.setInputType(InputType.TYPE_CLASS_TEXT);
                    etOtro.setFocusableInTouchMode(true);
                    etComentario.setFocusable(true);
                    etComentario.setEnabled(true);
                    etComentario.setInputType(InputType.TYPE_CLASS_TEXT);
                    etComentario.setFocusableInTouchMode(true);
                    etCantidad.setFocusable(true);
                    etCantidad.setEnabled(true);
                    etCantidad.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                    etCantidad.setFocusableInTouchMode(true);
                    spCuenta.setFocusable(true);
                    spMoneda.setFocusable(true);
                    spMotivo.setFocusable(true);
                    if(idCuenta > 20)
                        spCuenta.setEnabled(true);
                    spMotivo.setEnabled(true);
                    spMoneda.setEnabled(true);

                }
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

        to = new int[]{android.R.id.text1};
        from = new String[]{DBMan.DBTotales.Cuenta};
        simpleCursorAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,cursorCuenta,from,to,0);
        spCuenta.setAdapter(simpleCursorAdapter);
        spCuenta.setOnItemSelectedListener(this);

        from = new String[]{DBMan.DBMotivo.Motivo};
        simpleCursorAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,cursorMotivo,from,to,0);
        spMotivo.setAdapter(simpleCursorAdapter);
        spMotivo.setOnItemSelectedListener(this);

        from = new String[]{"Moneda"};
        simpleCursorAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,cursorMoneda,from,to,0);
        spMoneda.setAdapter(simpleCursorAdapter);
        spMoneda.setOnItemSelectedListener(this);


        comment = c.getString(c.getColumnIndex(DBMan.DBMovimientos.Comment));
        idMoneda = c.getInt(c.getColumnIndex("IdMoneda"));
        nIdMoneda = idMoneda;
        idCuenta = c.getInt(c.getColumnIndex("IdTotales"));
        nIdCuenta = idCuenta;
        idMotivo = c.getInt(c.getColumnIndex(DBMan.DBMovimientos.IdMotivo));
        nIdMotivo = idMotivo;

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

        //spCuenta.setSelection(idCuenta -1);
        nCantidad = cantidad;
        date = c.getString(c.getColumnIndex(DBMan.DBMovimientos.Fecha));
        nDate = c.getString(c.getColumnIndex("nFecha"));;
        Toast.makeText(this, nDate,Toast.LENGTH_LONG).show();
        etCantidad.setText("$"+instance.format(cantidad));
        etFecha.setText(date);
        etComentario.setText(comment);
        etOtro.setVisibility(View.GONE);


        if(Principal.getMonedaId(idCuenta)== idMoneda){
            Toast.makeText(this,""+Principal.getMonedaId(idCuenta) + " = " + idMoneda, Toast.LENGTH_LONG).show();
            etCambio.setVisibility(View.INVISIBLE);
            tvCambio.setVisibility(View.INVISIBLE);
        }else{
            cambio = c.getDouble(c.getColumnIndex(DBMan.DBMovimientos.Cambio));
            nCambio = cambio;
            etCambio.setText(cambio.toString());
        }

    }
    private boolean verificarDatos(){

        try{
            String sCant = etCantidad.getText().toString();

            String sCantidad = "";
            if (sCant.charAt(0) == '$') {
                sCant = sCant.substring(1, sCant.length());
            }
            for (int i = 0; i < sCant.length(); i++){
                if(sCant.charAt(i) != ',') sCantidad += sCant.charAt(i);
            }
            nCantidad = Double.parseDouble(sCantidad);
            if(Principal.getMonedaId(nIdCuenta) != nIdMoneda){
                nCambio = Double.parseDouble(etCambio.getText().toString());
            }
        }catch (Exception e){
            return false;
        }
        if(EditTextError.checkError(etComentario, getString(R.string.required_field))){
            nComment = null;
            etComentario.setError(null);
        }
        else{
            nComment = etComentario.getText().toString();

        }
        /*if(others){
            motivo = ETMotivo.getText().toString();
        }*/
        return true;
    }
    public void guardar(){
        /*
        Principal.newMove(cant, idCuenta, coment, motivo, moneda,-1);
        Principal.newMoveCuenta(cant,idCuenta);
        */
    }
    public void guardarDif(){
        /*
        Toast.makeText(context, "Error en los datos", Toast.LENGTH_SHORT).show();
        if(coment == null){
            coment = "# " + cant + " x " + tipoDeCambio + " = " + cant*tipoDeCambio;
        } else  coment += "  #-# " + cant + " x " + tipoDeCambio + " = " + instance.format(cant*tipoDeCambio);
        Toast.makeText(context, coment, Toast.LENGTH_SHORT).show();
        Principal.newMove(cant,idCuenta,coment,motivo,moneda,tipoDeCambio);
        Principal.newMoveCuenta(cant*tipoDeCambio,idCuenta);
        Principal.actualizarTipoDeCambio(moneda,monedaCuenta,tipoDeCambio);
        */
    }
    public void tipoDeCambio(){
        if(Principal.getMonedaId(nIdCuenta)== nIdMoneda){
            Toast.makeText(this,""+Principal.getMonedaId(nIdCuenta) + " = " + nIdMoneda, Toast.LENGTH_LONG).show();
            nCambio = -1.0;
            if(nIdCuenta != 1) {
                etCambio.setVisibility(View.GONE);
                tvCambio.setVisibility(View.GONE);
                nCambio = 1.0;
            }
        }else{
            String camb = c.getString(c.getColumnIndex(DBMan.DBMovimientos.Cambio));
            if(camb == null) nCambio = Double.parseDouble(Principal.getTipodeCambio(nIdMoneda,Principal.getMonedaId(nIdCuenta)));
            else nCambio = c.getDouble(c.getColumnIndex(DBMan.DBMovimientos.Cambio));
            etCambio.setText(nCambio.toString());
            etCambio.setVisibility(View.VISIBLE);
            tvCambio.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner sp = (Spinner) parent;
        if(sp.getId()==R.id.see_move_spCuenta) {
            nIdCuenta = cursorCuenta.getInt(cursorCuenta.getColumnIndex("_id"));
            tipoDeCambio();
        } else if(sp.getId()==R.id.see_move_spMoneda) {
            nIdMoneda = cursorMoneda.getInt(cursorMoneda.getColumnIndex("_id"));
            tipoDeCambio();
        } else if (sp.getId() == R.id.see_move_spMotivo){
            nIdMotivo = cursorMotivo.getInt(cursorMotivo.getColumnIndex("_id"));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_see, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int idM = item.getItemId();
        if(idM == R.id.action_delete){
            Principal.eliminarMov(id);
            Toast.makeText(context,getString(R.string.del_move), Toast.LENGTH_SHORT).show();
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
                    Principal.addMoveToTrip(id, c.getInt(c.getColumnIndex("_id")));
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
