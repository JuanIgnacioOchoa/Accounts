package com.lala;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

public class seeTraspaso extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private Spinner spFromCuenta, spToCuenta;
    private EditText etCantidad, etComment, etCambio, etFecha;
    private TextView tvMoneda, tvCambio;
    private Cursor cursorFromCuenta, cursorToCuenta, c;
    private int idCuentaFrom, idCuentaTo, id, idMotivo, nIdFrom, nIdTo, nIdMotivo;
    private int[] to = new int[]{android.R.id.text1};
    private String[] from;
    private Context context = this;
    private SimpleCursorAdapter simpleCursorAdapter;
    private String comment, nComment, date, nDate;
    private Double cantidad, tipoDeCambio, nCantidad, nCambio;
    private NumberFormat instance;
    private Boolean edit;
    private Calendar calendar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_traspaso);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent i = getIntent();
        id = i.getIntExtra("_id",0);
        c = Principal.getData(id);
        c.moveToFirst();
        calendar = Calendar.getInstance();

        edit = false;
        spFromCuenta = (Spinner) findViewById(R.id.see_traspaso_spFrom);
        spToCuenta = (Spinner) findViewById(R.id.see_traspaso_spTo);
        etCantidad = (EditText) findViewById(R.id.see_traspaso_etCantidad);
        etComment = (EditText) findViewById(R.id.see_traspaso_etComment);
        tvMoneda = (TextView) findViewById(R.id.see_traspaso_tvMoneda);
        etCambio = (EditText) findViewById(R.id.see_traspaso_etCambio);
        tvCambio = (TextView) findViewById(R.id.see_traspaso_tvCambio);
        etFecha = (EditText) findViewById(R.id.see_trasp_date);

        //etCambio.setFocusable(false);
        //etCambio.setEnabled(false);
        etComment.setFocusable(false);
        etComment.setEnabled(false);
        etFecha.setFocusable(false);
        etFecha.setEnabled(false);
        etCantidad.setFocusable(false);
        etCantidad.setEnabled(false);
        etCambio.setEnabled(false);
        etCambio.setFocusable(false);
        spFromCuenta.setEnabled(false);
        spFromCuenta.setFocusable(false);
        spToCuenta.setEnabled(false);
        spToCuenta.setEnabled(false);

        etFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new DatePickerDialog(seeTraspaso.this, new DatePickerDialog.OnDateSetListener() {
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
                alertDialog.show();
            }
        });
        instance = NumberFormat.getInstance();
        instance.setMinimumFractionDigits(2);

        comment = c.getString(c.getColumnIndex(DBMan.DBMovimientos.Comment));
        nComment = comment;
        idCuentaFrom = c.getInt(c.getColumnIndex("IdTotales"));
        nIdFrom = idCuentaFrom;
        idCuentaTo = c.getInt(c.getColumnIndex(DBMan.DBMovimientos.Traspaso));
        nIdTo = idCuentaTo;
        idMotivo = c.getInt(c.getColumnIndex(DBMan.DBMovimientos.IdMotivo));
        nIdMotivo = idMotivo;
        cantidad = c.getDouble(c.getColumnIndex(DBMan.DBMovimientos.Cantidad));
        nCantidad = cantidad;
        etComment.setText(nComment);
        etCantidad.setText(cantidad+"");
        if(idMotivo == 1){
            cursorFromCuenta = Principal.getTotales(idCuentaFrom);
            cursorToCuenta = Principal.getTotales(idCuentaTo);
            tvMoneda.setText(Principal.getMonedaTotales(c.getInt(c.getColumnIndex(DBMan.DBMovimientos.IdTotales))));
        }
        else{
            cursorFromCuenta = Principal.getTotalCredit(idCuentaFrom);
            cursorToCuenta = Principal.getTotalWallet(idCuentaTo);
            tvMoneda.setText(Principal.getMonedaTotales(c.getInt(c.getColumnIndex(DBMan.DBMovimientos.Traspaso))));
        }
        date = c.getString(c.getColumnIndex(DBMan.DBMovimientos.Fecha));
        nDate = c.getString(c.getColumnIndex("nFecha"));;
        etFecha.setText(date);


        from = new String[]{DBMan.DBTotales.Cuenta};
        simpleCursorAdapter = new SimpleCursorAdapter(context,android.R.layout.simple_list_item_1,cursorFromCuenta,from,to,0);
        spFromCuenta.setAdapter(simpleCursorAdapter);

        simpleCursorAdapter = new SimpleCursorAdapter(context,android.R.layout.simple_list_item_1,cursorToCuenta,from,to,0);
        spToCuenta.setAdapter(simpleCursorAdapter);

        spFromCuenta.setOnItemSelectedListener(this);
        spToCuenta.setOnItemSelectedListener(this);
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.see_trasp_check);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(edit){
                    Toast.makeText(context, "hola", Toast.LENGTH_SHORT).show();
                    if (EditTextError.checkError(etCantidad)) {
                    } else if (verificarDatos()) {
                        Principal.actualizarTraspaso(id,nCantidad,nIdFrom,nIdTo,nComment,nIdMotivo,nCambio, nDate);
                        //Principal.actualizarTipoDeCambio(Principal.getIdMoneda(nIdMoneda),Principal.getIdMoneda(Principal.getMonedaId(nIdCuenta)),nCambio);
                        finish();
                    } else Toast.makeText(context, "Error en los datos", Toast.LENGTH_SHORT).show();
                }else{
                    edit = true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        fab.setImageDrawable(getResources().getDrawable(R.drawable.check, context.getTheme()));
                    } else fab.setImageDrawable(getResources().getDrawable(R.drawable.check));
                    etFecha.setEnabled(true);
                    etCambio.setFocusable(true);
                    etCambio.setEnabled(true);
                    etCambio.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    etCambio.setFocusableInTouchMode(true);
                    etComment.setFocusable(true);
                    etComment.setEnabled(true);
                    etComment.setInputType(InputType.TYPE_CLASS_TEXT);
                    etComment.setFocusableInTouchMode(true);
                    etCantidad.setFocusable(true);
                    etCantidad.setEnabled(true);
                    etCantidad.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    etCantidad.setFocusableInTouchMode(true);
                    spFromCuenta.setFocusable(true);
                    spToCuenta.setFocusable(true);
                    spFromCuenta.setEnabled(true);
                    spToCuenta.setEnabled(true);

                }
            }
        });


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
        if(Principal.getMonedaId(idCuentaTo) == Principal.getMonedaId(idCuentaFrom)){
            etCambio.setVisibility(View.INVISIBLE);
            tvCambio.setVisibility(View.INVISIBLE);
        } else {
            nCambio = c.getDouble(c.getColumnIndex(DBMan.DBMovimientos.Cambio));
            etCambio.setText((nCambio.toString()));
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
            if(Principal.getMonedaId(nIdFrom) != Principal.getMonedaId(nIdTo)){
                nCambio = Double.parseDouble(etCambio.getText().toString());
            }
        }catch (Exception e){
            return false;
        }
        if(EditTextError.checkError(etComment)){
            nComment = null;
            etComment.setError(null);
        }
        else nComment = etComment.getText().toString();

        /*if(others){
            motivo = ETMotivo.getText().toString();
        }*/
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner sp = (Spinner) parent;
        if(sp.getId() == spFromCuenta.getId()){
            nIdFrom = cursorFromCuenta.getInt(cursorFromCuenta.getColumnIndex("_id"));
            Toast.makeText(context,"From: "+ nIdFrom,Toast.LENGTH_SHORT).show();
        } else if(sp.getId() == spToCuenta.getId()){
            nIdTo = cursorToCuenta.getInt(cursorToCuenta.getColumnIndex("_id"));
            Toast.makeText(context,"To: "+ nIdTo,Toast.LENGTH_SHORT).show();
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
            Principal.eliminarTras(id);
            Toast.makeText(context,"Movimiento ha sido eliminado", Toast.LENGTH_SHORT).show();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
