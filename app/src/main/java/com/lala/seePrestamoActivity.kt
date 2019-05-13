package com.lala

import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_see_prestamo.fab
import kotlinx.android.synthetic.main.activity_see_prestamo.toolbar
import java.lang.Exception
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
    private var comment:String? = ""
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

        val idMon = cursorMoneda.getInt(cursorMoneda.getColumnIndex("_id"))
        val idMonCuenta = Principal.getIdMonedaTotales(cursorCuenta.getInt(cursorCuenta.getColumnIndex("_id")))
        if(idMon != idMonCuenta){
            etCambio.isEnabled = editable
            etCambio.visibility = View.VISIBLE
        } else {
            etCambio.isEnabled = false
            etCambio.visibility = View.GONE
        }
        etComment.isEnabled = editable
        etCant.isEnabled = editable
        spPersonas.isEnabled = editable
        spCuentas.isEnabled = editable
        spMonedas.isEnabled = editable


        fab.setOnClickListener {
            if(!editable){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    fab.setImageDrawable(resources.getDrawable(R.drawable.check, applicationContext.theme))
                }
            } else {
                //TODO actualizar prestamo, agregar verificacion de datos
                val cantidad = etCant.text.toString().toDouble()
                val comment = etComment.text.toString()
                val idCuenta = cursorCuenta.getInt(cursorCuenta.getColumnIndex("_id"))
                var idPersona = cursorPersona.getInt(cursorPersona.getColumnIndex("_id"))
                val idMoneda = cursorMoneda.getInt(cursorMoneda.getColumnIndex("_id"))
                var cam = 1.0
                val idMonedaCuenta = Principal.getIdMonedaTotales(idCuenta)
                if(idMoneda != idMonedaCuenta){ //TODO cambiar a revision de cuentas
                    try {
                        cam = etCambio.text.toString().toDouble()
                    } catch (e:Exception){
                        Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
                        cam = 1.0
                    }
                    Principal.actualizarTipoDeCambio(idMoneda, idMonedaCuenta, cam)
                }
                if(idPersona == -1){
                    idPersona = Principal.insertPersona(etPersona.text.toString()).toInt()
                }
                Principal.deshacesPrestamo(id)
                Principal.newMoveCuenta(cantidad * cam, idCuenta)
                Principal.updatePrestamo(id, cantidad, cam, idCuenta, idMoneda, idPersona, comment)
            }
            editable = !editable
            etComment.isEnabled = editable
            etCant.isEnabled = editable
            spMonedas.isEnabled = editable
            spPersonas.isEnabled = editable
            spCuentas.isEnabled = editable
            val idMon = cursorMoneda.getInt(cursorMoneda.getColumnIndex("_id"))
            val idMonCuenta = Principal.getIdMonedaTotales(cursorCuenta.getInt(cursorCuenta.getColumnIndex("_id")))
            if(idMon != idMonCuenta){
                etCambio.isEnabled = editable
                etCambio.visibility = View.VISIBLE
            } else {
                etCambio.isEnabled = false
                etCambio.visibility = View.GONE
            }
        }
        spMonedas.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val idMon = cursorMoneda.getInt(cursorMoneda.getColumnIndex("_id"))
                val idMonCuenta = Principal.getIdMonedaTotales(cursorCuenta.getInt(cursorCuenta.getColumnIndex("_id")))
                if (idMon != idMonCuenta) {
                    etCambio.isEnabled = editable
                    etCambio.visibility = View.VISIBLE
                } else {
                    etCambio.isEnabled = false
                    etCambio.visibility = View.GONE
                }
            }
        })
        spCuentas.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val idMon = cursorMoneda.getInt(cursorMoneda.getColumnIndex("_id"))
                val idMonCuenta = Principal.getIdMonedaTotales(cursorCuenta.getInt(cursorCuenta.getColumnIndex("_id")))
                if (idMon != idMonCuenta) {
                    etCambio.isEnabled = editable
                    etCambio.visibility = View.VISIBLE
                } else {
                    etCambio.isEnabled = false
                    etCambio.visibility = View.GONE
                }
            }
        })
        spPersonas.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if(id.toInt() == -1) {
                    etPersona.visibility = View.VISIBLE
                    spPersonas.visibility = View.INVISIBLE
                    if(cursorPersona.count > 1) {
                        etPersona.requestFocus()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        })
        etPersona.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val s = etPersona.text.toString()
                if (!s.isEmpty()) {
                } else {
                    if (cursorPersona.count > 1) {
                        etPersona.visibility = View.INVISIBLE
                        spPersonas.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

}
