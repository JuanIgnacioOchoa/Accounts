

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
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Traspaso extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Spinner spFromCuenta, spToCuenta;
    private EditText etCantidad, etComment;
    private TextView tvMoneda;
    private Boolean Retiro;
    private Cursor cursorFromCuenta, cursorToCuenta;
    private int idCuentaFrom, idCuentaTo;
    private int[] to = new int[]{android.R.id.text1};
    private String[] from;
    private Context context = this;
    private SimpleCursorAdapter simpleCursorAdapter;
    private String comment, monedaTo, monedaFrom;
    private Double cantidad, tipoDeCambio;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traspaso);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Intent i = getIntent();
        Retiro = i.getBooleanExtra("Retiro",false);
        if(Retiro) this.setTitle(getString(R.string.withrawal));
        else this.setTitle(getString(R.string.transfer));
        Toast.makeText(context,Retiro+"",Toast.LENGTH_SHORT).show();
        spFromCuenta = (Spinner) findViewById(R.id.traspaso_spFrom);
        spToCuenta = (Spinner) findViewById(R.id.traspaso_spTo);
        etCantidad = (EditText) findViewById(R.id.traspaso_etCantidad);
        etComment = (EditText) findViewById(R.id.traspaso_etComment);
        tvMoneda = (TextView) findViewById(R.id.traspaso_tvMoneda);
        if(Retiro){
            cursorFromCuenta = Principal.getTotalCredit();
            cursorToCuenta = Principal.getTotalWallet();
        }else {
            cursorFromCuenta = Principal.getTotales(false);
            cursorToCuenta = Principal.getTotales(false);
        }

        from = new String[]{DBMan.DBTotales.Cuenta};
        simpleCursorAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,cursorFromCuenta,from,to,0);
        spFromCuenta.setAdapter(simpleCursorAdapter);

        simpleCursorAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,cursorToCuenta,from,to,0);
        spToCuenta.setAdapter(simpleCursorAdapter);

        spToCuenta.setOnItemSelectedListener(this);
        spFromCuenta.setOnItemSelectedListener(this);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.trasp_check);
        fab.setBackgroundColor(Color.BLUE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(EditTextError.checkError(etCantidad, getString(R.string.required_field))){
                }
                else if (verificarDatos()){

                    monedaTo = Principal.getMonedaTotales(idCuentaTo);
                    monedaFrom = Principal.getMonedaTotales(idCuentaFrom);
                    if(!monedaTo.equals(monedaFrom)){
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(getString(R.string.currency_change));

// Set up the input
                        final EditText input = new EditText(context);
// Specify the type of input expected;
                        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        if(Retiro)  input.setText(Principal.getTipodeCambio(monedaTo,monedaFrom));
                        else input.setText(Principal.getTipodeCambio(monedaFrom,monedaTo));
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

        if(cursorFromCuenta.getCount() == 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(Traspaso.this);
            builder.setTitle(getString(R.string.alert_info_data));
            builder.setMessage(getString(R.string.alert_info_data_msg_tras));

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
        if(sp.getId()==R.id.traspaso_spFrom) {
            // cuentaFrom = cursorFromCuenta.getString(cursorFromCuenta.getColumnIndex(DBMan.DBTotales.Cuenta));
            idCuentaFrom = cursorFromCuenta.getInt(cursorFromCuenta.getColumnIndex("_id"));
            tvMoneda.setText(Principal.getMonedaTotales(cursorFromCuenta.getInt(cursorFromCuenta.getColumnIndex("_id"))));
            Toast.makeText(getApplicationContext(),""+idCuentaFrom,Toast.LENGTH_SHORT).show();

        } else if (sp.getId() == R.id.traspaso_spTo){
            idCuentaTo = cursorToCuenta.getInt(cursorToCuenta.getColumnIndex("_id"));
            Toast.makeText(getApplicationContext(),""+idCuentaTo,Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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
            Principal.newTraspaso(idCuentaFrom, idCuentaTo, cantidad, -1, comment);
            Principal.newMoveCuenta(cantidad * -1, idCuentaFrom);
            Principal.newMoveCuenta(cantidad, idCuentaTo);
        } else {
            Principal.newRetiro(idCuentaFrom, idCuentaTo, cantidad, -1, comment);
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
            Principal.newTraspaso(idCuentaFrom, idCuentaTo, cantidad, tipoDeCambio, comment);
            //Principal.newMoveCuenta(cant*tipoDeCambio,idCuenta);
            Principal.actualizarTipoDeCambio(Principal.getMonedaTotales(idCuentaFrom), Principal.getMonedaTotales(idCuentaTo), tipoDeCambio);
            Principal.newMoveCuenta(cant, idCuentaFrom);
            Principal.newMoveCuenta(cantidad * tipoDeCambio, idCuentaTo);
        } else {

            if (comment == null) {
                comment = "# " + cantidad + " x " + tipoDeCambio + " = " + cantidad * tipoDeCambio;
            } else
                comment += "  #-# " + cantidad + " x " + tipoDeCambio + " = " + (cantidad * tipoDeCambio);
            Principal.newRetiro(idCuentaFrom, idCuentaTo, cantidad, tipoDeCambio, comment);
            Principal.actualizarTipoDeCambio(Principal.getMonedaTotales(idCuentaTo), Principal.getMonedaTotales(idCuentaFrom), tipoDeCambio);
            Toast.makeText(context, (cantidad * tipoDeCambio * -1 )+" a " + idCuentaFrom, Toast.LENGTH_SHORT).show();
            Principal.newMoveCuenta(cantidad * tipoDeCambio * -1, idCuentaFrom);
            Principal.newMoveCuenta(cantidad, idCuentaTo);
        }
    }
}
