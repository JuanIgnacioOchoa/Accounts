package com.lala;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class NewAccount extends AppCompatActivity {

    private EditText etCuenta, etCantidad;
    private Spinner spMoneda;
    private Button bGuardar, bCancelar;
    private Cursor cursorMoneda;
    private String[] from;
    private int[] to;
    private SimpleCursorAdapter cursorAdapterMoneda;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                //handleOnBackPress();
            }
        });

        etCuenta = (EditText) findViewById(R.id.new_account_cuenta);
        etCantidad = (EditText) findViewById(R.id.new_account_cantidad);
        spMoneda = (Spinner) findViewById(R.id.new_account_Smoneda);
        bGuardar = (Button) findViewById(R.id.new_account_bGuardar);
        bCancelar = (Button) findViewById(R.id.new_account_bCancelar);

        cursorMoneda = Principal.getMoneda();
        from   = new String[]{"Moneda"};
        to = new int[]{android.R.id.text1};
        cursorAdapterMoneda = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,cursorMoneda,from,to,0);
        spMoneda.setAdapter(cursorAdapterMoneda);

        bGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!EditTextError.checkError(etCuenta, getString(R.string.err_data))&&!EditTextError.checkError(etCantidad, getString(R.string.err_data))){
                    try {
                        Double cantidad = Double.parseDouble(etCantidad.getText().toString());
                        String cuenta = etCuenta.getText().toString();
                        int moneda = cursorMoneda.getInt(cursorMoneda.getColumnIndex("_id"));
                        Principal.insertTotales(cuenta,cantidad,moneda);
                        Principal.hideKeyboard(NewAccount.this);
                        finish();
                    } catch (Exception e){
                        Toast.makeText(getApplicationContext(),getString(R.string.err_data_save),Toast.LENGTH_LONG).show();
                    }

                }
            }
        });
        bCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if(cursorAdapterMoneda.getCount() == 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(NewAccount.this);
            builder.setTitle(getString(R.string.alert_info_data));
            builder.setMessage(getString(R.string.alert_info_data_msg_acc));

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
}