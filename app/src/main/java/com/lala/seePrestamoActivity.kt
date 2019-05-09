package com.lala

import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.view.View
import android.widget.EditText
import android.widget.SimpleCursorAdapter
import android.widget.Spinner

import kotlinx.android.synthetic.main.activity_see_prestamo.*
import kotlinx.android.synthetic.main.activity_see_prestamo.fab
import kotlinx.android.synthetic.main.activity_see_prestamo.toolbar
import kotlinx.android.synthetic.main.activity_see_trip_main.*
import java.text.NumberFormat

class seePrestamoActivity : AppCompatActivity() {

    private lateinit var cursorPrestamo:Cursor
    private lateinit var cursorMoneda:Cursor
    private lateinit var cursorPersona:Cursor
    private lateinit var cursorCuenta:Cursor
    private var id = 0
    private lateinit var etCant:EditText
    private lateinit var etComment:EditText
    private lateinit var etCambio:EditText
    private lateinit var etPersona:EditText
    private lateinit var spPersonas:Spinner
    private lateinit var spCuentas:Spinner
    private lateinit var spMonedas:Spinner
    private var cant = 0.0
    private var idMoneda = 0
    private var idCuenta = 0
    private var idPersona = 0
    private var cambio = 1.0
    private var comment = ""
    private val instance = NumberFormat.getInstance()
    private lateinit var adapterMoneda: SimpleCursorAdapter
    private lateinit var adapterCuenta: SimpleCursorAdapter
    private lateinit var adapterPersona: SimpleCursorAdapter
    private var editable = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_see_prestamo)
        setSupportActionBar(toolbar)

        id = intent.getIntExtra("_id", 0)
        cursorPrestamo = Principal.getCursorPrestamo(id)
        instance.minimumFractionDigits = 2

        etCant = findViewById(R.id.ETCantidad)
        etComment = findViewById(R.id.ETDesc)
        spMonedas = findViewById(R.id.spMoneda)
        spCuentas = findViewById(R.id.SPCuenta)
        spPersonas = findViewById(R.id.SPPersona)
        etPersona = findViewById(R.id.etPersona)
        etCambio = findViewById(R.id.etCambio)

        cant = cursorPrestamo.getDouble(cursorPrestamo.getColumnIndex(DBMan.DBPrestamo.Cantidad))
        idMoneda = cursorPrestamo.getInt(cursorPrestamo.getColumnIndex(DBMan.DBPrestamo.IdMoneda))
        idCuenta = cursorPrestamo.getInt(cursorPrestamo.getColumnIndex(DBMan.DBPrestamo.IdTotales))
        idPersona = cursorPrestamo.getInt(cursorPrestamo.getColumnIndex(DBMan.DBPrestamo.IdPersona))
        cambio = cursorPrestamo.getDouble(cursorPrestamo.getColumnIndex(DBMan.DBPrestamo.Cambio))
        comment = cursorPrestamo.getString(cursorPrestamo.getColumnIndex(DBMan.DBPrestamo.Comment))

        etCant.setText(instance.format(cant))
        etComment.setText(comment)


        cursorMoneda = Principal.getMoneda()
        cursorCuenta = Principal.getTotales()
        cursorPersona = Principal.getPersonas()
        adapterMoneda = SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursorMoneda, arrayOf("Moneda"), intArrayOf(android.R.id.text1), 0)
        adapterCuenta = SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursorCuenta, arrayOf("Cuenta"), intArrayOf(android.R.id.text1), 0)
        adapterPersona = SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursorPersona, arrayOf("Nombre"), intArrayOf(android.R.id.text1), 0)
        spMonedas.adapter = adapterMoneda
        spCuentas.adapter = adapterCuenta
        spPersonas.adapter = adapterPersona

        if(cambio != 1.0 || cambio == null || cambio == 0.0){ //TODO cambiar a revision de cuentas
            etCambio.visibility = View.VISIBLE
            etCambio.setText(instance.format(cambio))
            etCambio.isEnabled = editable
        }
        etComment.isEnabled = editable
        etCant.isEnabled = editable
        spPersonas.isEnabled = editable
        spCuentas.isEnabled = editable
        spMonedas.isEnabled = editable

        fab.setOnClickListener { view ->
            if(!editable){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    fab.setImageDrawable(resources.getDrawable(R.drawable.check, applicationContext.getTheme()))
                } else
                    fab.setImageDrawable(resources.getDrawable(R.drawable.check))
            } else {
                //TODO actualizar prestamo

            }
            editable = !editable
            etComment.isEnabled = editable
            etCant.isEnabled = editable
            spMonedas.isEnabled = editable
            spPersonas.isEnabled = editable
            spCuentas.isEnabled = editable
            if(cambio != 1.0 || cambio == null || cambio == 0.0){ //TODO cambiar a revision de cuentas
                etCambio.isEnabled = editable
            }
        }
    }

}
