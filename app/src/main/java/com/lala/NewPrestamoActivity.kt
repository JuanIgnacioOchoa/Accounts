package com.lala

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.text.InputType
import android.view.View
import android.widget.*

import kotlinx.android.synthetic.main.activity_new_prestamo.*
import java.lang.Exception
import android.view.View.OnFocusChangeListener
import java.text.NumberFormat


class NewPrestamoActivity : AppCompatActivity() {

    private lateinit var etCant:EditText
    private lateinit var etPersona:EditText
    private lateinit var etComment:EditText
    private lateinit var spMonedas:Spinner
    private lateinit var spCuentas:Spinner
    private lateinit var spPersonas:Spinner
    private var personaString:String = ""
    private var prestado = false
    private var etPersonaFocus = false
    private var instance = NumberFormat.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_prestamo)
        setSupportActionBar(toolbar)
        prestado = intent.getBooleanExtra("Prestado", false)
        when (prestado){
            true -> title = "Presto a:"
            false -> title = "Me presta:"
        }
        instance.minimumFractionDigits = 2
        etCant = findViewById(R.id.ETCantidad)
        etComment = findViewById(R.id.ETDesc)
        spMonedas = findViewById(R.id.spMoneda)
        spCuentas = findViewById(R.id.SPCuenta)
        spPersonas = findViewById(R.id.SPPersona)
        etPersona = findViewById(R.id.etPersona)
        val cursorMoneda = Principal.getMoneda()
        val cursorTotales = Principal.getTotales()
        val cursorPersonas = Principal.getPersonas()
        var from = arrayOf("Moneda")
        val to = intArrayOf(android.R.id.text1)
        val cursorAdapterMoneda = SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursorMoneda, from, to, 0)
        from = arrayOf("Nombre")
        val cursorAdapterPersona = SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursorPersonas, from, to, 0)
        from = arrayOf("Cuenta")
        val cursorAdapterCuenta = SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursorTotales, from, to, 0)
        spMonedas.adapter = cursorAdapterMoneda
        spPersonas.adapter = cursorAdapterPersona
        spCuentas.adapter = cursorAdapterCuenta

        fab.setOnClickListener { view ->
            val moneda = cursorMoneda.getInt(cursorMoneda.getColumnIndex("_id"))
            var cant = 0.0
            //try {
                cant = (etCant.getText().toString().toDouble()) * -1
                var descr = etComment.text.toString()
                var persona = 0
                val cuenta = cursorTotales.getInt(cursorTotales.getColumnIndex("_id"))
                val personaSp = cursorPersonas.getInt(cursorPersonas.getColumnIndex("_id"))
                if(personaSp == -1 && etPersona.text.toString().isEmpty()){
                    persona = -1
                } else{
                    persona = Principal.insertPersona(etPersona.text.toString()).toInt()
                }
                val monedaCuenta = Principal.getIdMonedaTotales(cuenta)
                if(moneda != monedaCuenta){
                    //Monedas diferentes
                    Toast.makeText(applicationContext, "Monedas diferentes $moneda $monedaCuenta", Toast.LENGTH_SHORT).show()
                    val builder = AlertDialog.Builder(this@NewPrestamoActivity)
                    builder.setTitle("Tipo de cambio")

// Set up the input
                    val input = EditText(applicationContext)
// Specify the type of input expected;
                    input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                    val a = Principal.getTipodeCambio(moneda, monedaCuenta)
                    input.setText(a)
                    builder.setView(input)
// Set up the buttons
                    builder.setPositiveButton("OK") { dialog, which ->
                        val tipoDeCambio = java.lang.Double.parseDouble(input.text.toString())
                        if (descr == null) {
                            descr = "#-# " + cant + " x " + tipoDeCambio + " = " + cant * tipoDeCambio
                        } else
                            descr += "  #-# " + cant + " x " + tipoDeCambio + " = " + instance.format(cant * tipoDeCambio)
                        if(Principal.createPrestamo(cant, cuenta, moneda, persona, descr, tipoDeCambio)) {
                            Principal.actualizarTipoDeCambio(moneda, monedaCuenta, tipoDeCambio)
                            Principal.newMoveCuenta(cant * tipoDeCambio, cuenta)
                            Toast.makeText(applicationContext, "Guardado con exito", Toast.LENGTH_LONG).show()
                            //guardarDif()
                            finish()
                        }
                    }
                    builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

                    // Finally, make the alert dialog using builder
                    val dialog: AlertDialog = builder.create()
                    // Display the alert dialog on app interface
                    dialog.show()
                } else {
                    Principal.createPrestamo(cant, cuenta, moneda, persona, descr, 1.0)
                    Principal.newMoveCuenta(cant, cuenta)
                    Toast.makeText(applicationContext, "Guardado con exito", Toast.LENGTH_LONG).show()
                    finish()
                }

            //} catch (e: Exception){
            //    Toast.makeText(applicationContext, "Error en los datos", Toast.LENGTH_LONG).show()
            //}
        }
        spPersonas.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if(id.toInt() == -1) {
                    etPersona.visibility = View.VISIBLE
                    spPersonas.visibility = View.INVISIBLE
                    if(cursorAdapterPersona.count > 1) {
                        etPersona.requestFocus()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        })
        etPersona.onFocusChangeListener = OnFocusChangeListener{ _, hasFocus ->
            etPersonaFocus = hasFocus
            if(!hasFocus){
                val s = etPersona.text.toString()
                if(!s.isEmpty()){
                    personaString = s
                } else{
                    personaString = ""
                    if(cursorAdapterPersona.count > 1) {
                        etPersona.visibility = View.INVISIBLE
                        spPersonas.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

}
