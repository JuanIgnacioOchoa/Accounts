package com.lala;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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
                if(!EditTextError.checkError(etCuenta)&&!EditTextError.checkError(etCantidad)){
                    try {
                        Double cantidad = Double.parseDouble(etCantidad.getText().toString());
                        String cuenta = etCuenta.getText().toString();
                        int moneda = cursorMoneda.getInt(cursorMoneda.getColumnIndex("_id"));
                        Principal.insertTotales(cuenta,cantidad,moneda);
                        finish();
                    } catch (Exception e){
                        Toast.makeText(getApplicationContext(),"Error al guardar los datos",Toast.LENGTH_LONG).show();
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
    }
}
