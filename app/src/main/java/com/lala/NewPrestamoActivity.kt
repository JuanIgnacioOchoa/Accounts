package com.lala

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import android.text.InputType
import android.view.View
import android.widget.*

import kotlinx.android.synthetic.main.activity_new_prestamo.*
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
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material)

        toolbar.setNavigationOnClickListener {
            finish()
            //handleOnBackPress();
        }
        prestado = intent.getBooleanExtra("Prestado", false)
        when (prestado){
            true -> title = getString(R.string.loans_by)
            false -> title = getString(R.string.lend_to)
        }
        instance.minimumFractionDigits = 2
        etCant = findViewById(R.id.ETCantidad)
        etComment = findViewById(R.id.ETDesc)
        spMonedas = findViewById(R.id.spMoneda)
        spCuentas = findViewById(R.id.SPCuenta)
        spPersonas = findViewById(R.id.SPPersona)
        etPersona = findViewById(R.id.etPersona)
        val cursorMoneda = Principal.getMoneda()
        val cursorTotales = Principal.getTotales(false)
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
                cant = (etCant.getText().toString().toDouble())
                var descr = etComment.text.toString()
                var persona = 0
                val cuenta = cursorTotales.getInt(cursorTotales.getColumnIndex("_id"))
                persona = cursorPersonas.getInt(cursorPersonas.getColumnIndex("_id"))
                if(persona == -1){
                    persona = Principal.insertPersona(etPersona.text.toString()).toInt()
                }
                val monedaCuenta = Principal.getIdMonedaTotales(cuenta)
                if(moneda != monedaCuenta){
                    //Monedas diferentes
                    Toast.makeText(applicationContext, "${getString(R.string.dif_curr)} $moneda $monedaCuenta", Toast.LENGTH_SHORT).show()
                    val builder = AlertDialog.Builder(this@NewPrestamoActivity)
                    builder.setTitle(getString(R.string.currency_change))
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
                            descr += "  #-# " + cant + " x " + tipoDeCambio + " = " + (cant * tipoDeCambio)
                        if(Principal.createPrestamo(cant, cuenta, moneda, persona, descr, tipoDeCambio, 0)) {
                            Principal.actualizarTipoDeCambio(moneda, monedaCuenta, tipoDeCambio)
                            Principal.newMoveCuenta(cant * tipoDeCambio * -1, cuenta)
                            Toast.makeText(applicationContext, getString(R.string.succes_saved), Toast.LENGTH_LONG).show()
                            //guardarDif()
                            finish()
                        }
                    }
                    builder.setNegativeButton(getString(R.string.cancel)) { dialog, which -> dialog.cancel() }

                    // Finally, make the alert dialog using builder
                    val dialog: AlertDialog = builder.create()
                    // Display the alert dialog on app interface
                    val alertDialog = builder.show()
                    alertDialog.setCanceledOnTouchOutside(false)
                } else {
                    Principal.createPrestamo(cant, cuenta, moneda, persona, descr, 1.0, 0)
                    Principal.newMoveCuenta(cant * -1, cuenta)
                    Toast.makeText(applicationContext, getString(R.string.succes_saved), Toast.LENGTH_LONG).show()
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
        if(cursorAdapterCuenta.count == 0 || cursorAdapterMoneda.count == 0 || cursorAdapterPersona.count == 0){
            val builder = AlertDialog.Builder(this@NewPrestamoActivity)
            builder.setTitle(getString(R.string.alert_info_data))
            builder.setMessage(getString(R.string.alert_info_data_msg_pre))

// Set up the buttons
            builder.setPositiveButton("OK") { dialog, which -> finish() }
            val alertDialog = builder.show()
            alertDialog.setCanceledOnTouchOutside(false)
        }
    }

}
