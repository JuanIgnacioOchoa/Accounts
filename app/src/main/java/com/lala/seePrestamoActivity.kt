package com.lala

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_see_prestamo.fab
import kotlinx.android.synthetic.main.activity_see_prestamo.toolbar
import java.lang.Exception
import java.text.NumberFormat
import android.widget.RelativeLayout



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
            etCambio.setText(cursorPrestamo.getDouble(cursorPrestamo.getColumnIndex(DBMan.DBPrestamo.Cambio)).toString())
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
                val idMove = cursorPrestamo.getInt(cursorPrestamo.getColumnIndex(DBMan.DBPrestamo.IdMovimiento))
                if(idMove != 0){
                    Principal.updateMoveFromPrestamo(idMove, cantidad, cam, idMoneda)
                }
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
        var j = 0
        while (j < spMonedas.adapter.count) {
            val value = spMonedas.getItemAtPosition(j) as Cursor
            val id = value.getInt(value.getColumnIndex("_id"))
            val idMoneda = cursorPrestamo.getInt(cursorPrestamo.getColumnIndex(DBMan.DBPrestamo.IdMoneda))
            if (id == idMoneda) {
                spMonedas.setSelection(j)
                j = spMonedas.adapter.count + 1
            }
            j++
        }
        j = 0
        while (j < spCuentas.adapter.count) {
            val value = spCuentas.getItemAtPosition(j) as Cursor
            val id = value.getInt(value.getColumnIndex("_id"))
            val idMoneda = cursorPrestamo.getInt(cursorPrestamo.getColumnIndex(DBMan.DBPrestamo.IdTotales))
            if (id == idMoneda) {
                spCuentas.setSelection(j)
                j = spCuentas.adapter.count + 1
            }
            j++
        }
        j = 0
        while (j < spPersonas.adapter.count) {
            val value = spPersonas.getItemAtPosition(j) as Cursor
            val id = value.getInt(value.getColumnIndex("_id"))
            val idMoneda = cursorPrestamo.getInt(cursorPrestamo.getColumnIndex(DBMan.DBPrestamo.IdPersona))
            if (id == idMoneda) {
                spPersonas.setSelection(j)
                j = spPersonas.adapter.count + 1
            }
            j++
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_see_prestamo, menu)
        return true
    }

    @SuppressLint("ResourceType")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if(id == R.id.action_pago){
            val builder = AlertDialog.Builder(this@seePrestamoActivity)

            // Set the alert dialog title
            builder.setTitle("Choose an option")
            val spinnerMon = Spinner(applicationContext)
            val spCuenta = Spinner(applicationContext)
            val cursorMoneda = Principal.getMoneda()
            val cursorCuenta = Principal.getTotales()
            val adapterMoneda = SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursorMoneda, arrayOf("Moneda"), intArrayOf(android.R.id.text1), 0)
            val adapterCuenta = SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursorCuenta, arrayOf("Cuenta"), intArrayOf(android.R.id.text1), 0)
            spinnerMon.adapter = adapterMoneda
            spCuenta.adapter = adapterCuenta
            val etCantidad = EditText(applicationContext)
            val layout = LinearLayout(applicationContext)
            val relativeLayout = RelativeLayout(applicationContext)
            layout.weightSum = 2f
            val lp2 = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,1f)
            val rl = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT)
            val r2 = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT)
            relativeLayout.layoutParams = rl
            spCuenta.layoutParams = lp2
            spinnerMon.layoutParams = lp2
            layout.layoutParams = rl
            layout.id = 1
            etCantidad.layoutParams = r2
            r2.addRule(RelativeLayout.BELOW, 1)
            etCantidad.inputType = InputType.TYPE_CLASS_NUMBER
            etCantidad.setText((cant *-1).toString())
            var j = 0
            while (j < spinnerMon.adapter.count) {
                val value = spinnerMon.getItemAtPosition(j) as Cursor
                val id = value.getInt(value.getColumnIndex("_id"))
                val idMoneda = cursorPrestamo.getInt(cursorPrestamo.getColumnIndex(DBMan.DBPrestamo.IdMoneda))
                if (id == idMoneda) {
                    spinnerMon.setSelection(j)
                    j = spinnerMon.adapter.count + 1
                }
                j++
            }
            j = 0
            while (j < spCuenta.adapter.count) {
                val value = spCuenta.getItemAtPosition(j) as Cursor
                val id = value.getInt(value.getColumnIndex("_id"))
                val idMoneda = cursorPrestamo.getInt(cursorPrestamo.getColumnIndex(DBMan.DBPrestamo.IdTotales))
                if (id == idMoneda) {
                    spCuenta.setSelection(j)
                    j = spCuenta.adapter.count + 1
                }
                j++
            }
            builder.setPositiveButton("OK") { dialog, which ->
                val cant = etCantidad.text.toString().toDouble()
                val idMon = cursorMoneda.getInt(cursorMoneda.getColumnIndex("_id"))
                val idCuenta = cursorCuenta.getInt(cursorCuenta.getColumnIndex("_id"))
                Principal.updateTotalesFromPrestamo(cant, idCuenta, idMon)
                Principal.insertPrestamoDetalle(cant, idCuenta, idMon, id)
            }
            builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }
            layout.addView(spCuenta)
            layout.addView(spinnerMon)
            relativeLayout.addView(etCantidad)
            relativeLayout.addView(layout)
            builder.setView(relativeLayout)
            // Finally, make the alert dialog using builder
            val dialog: AlertDialog = builder.create()
            // Display the alert dialog on app interface
            dialog.show()
        }
        return super.onOptionsItemSelected(item)
    }
}
