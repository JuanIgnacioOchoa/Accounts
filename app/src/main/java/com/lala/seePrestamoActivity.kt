package com.lala

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v4.widget.CursorAdapter
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.view.*
import android.widget.*
import kotlinx.android.synthetic.main.activity_see_prestamo.fab
import kotlinx.android.synthetic.main.activity_see_prestamo.toolbar
import java.lang.Exception
import java.text.NumberFormat
import android.widget.RelativeLayout
import java.text.DateFormatSymbols
import java.util.*


class seePrestamoActivity : AppCompatActivity() {

    private lateinit var cursorPrestamo:Cursor
    private lateinit var cursorMoneda:Cursor
    private lateinit var cursorPersona:Cursor
    private lateinit var cursorCuenta:Cursor
    private lateinit var cursorDetalle:Cursor
    private lateinit var adapterList:myAdapter
    private var id = 0
    private lateinit var etCant:EditText
    private lateinit var etComment:EditText
    private lateinit var etCambio:EditText
    private lateinit var etPersona:EditText
    private lateinit var tvFecha:TextView
    private lateinit var spPersonas:Spinner
    private lateinit var spCuentas:Spinner
    private lateinit var spMonedas:Spinner
    private var cant = 0.0
    private var idMoneda = 0
    private var idMove = 0
    private var idCuenta = 0
    private var idPersona = 0
    private var cambio = 1.0
    private var comment:String? = ""
    private var fecha:String? = ""
    private val instance = NumberFormat.getInstance()
    private lateinit var adapterMoneda: SimpleCursorAdapter
    private lateinit var adapterCuenta: SimpleCursorAdapter
    private lateinit var adapterPersona: SimpleCursorAdapter
    private var editable = false
    private lateinit var listView: ListView
    private val calendar = Calendar.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_see_prestamo)
        setSupportActionBar(toolbar)

        id = intent.getIntExtra("_id", 0)
        cursorPrestamo = Principal.getCursorPrestamo(id)
        cursorDetalle = Principal.getPrestamoDetalle(id)
        instance.minimumFractionDigits = 2

        etCant = findViewById(R.id.ETCantidad)
        etComment = findViewById(R.id.ETDesc)
        spMonedas = findViewById(R.id.spMoneda)
        spCuentas = findViewById(R.id.SPCuenta)
        spPersonas = findViewById(R.id.SPPersona)
        etPersona = findViewById(R.id.etPersona)
        etCambio = findViewById(R.id.etCambio)
        tvFecha = findViewById(R.id.TVDate)
        listView = findViewById(R.id.listView)

        cant = cursorPrestamo.getDouble(cursorPrestamo.getColumnIndex(DBMan.DBPrestamo.Cantidad))
        idMoneda = cursorPrestamo.getInt(cursorPrestamo.getColumnIndex(DBMan.DBPrestamo.IdMoneda))
        idCuenta = cursorPrestamo.getInt(cursorPrestamo.getColumnIndex(DBMan.DBPrestamo.IdTotales))
        idPersona = cursorPrestamo.getInt(cursorPrestamo.getColumnIndex(DBMan.DBPrestamo.IdPersona))
        cambio = cursorPrestamo.getDouble(cursorPrestamo.getColumnIndex(DBMan.DBPrestamo.Cambio))
        comment = cursorPrestamo.getString(cursorPrestamo.getColumnIndex(DBMan.DBPrestamo.Comment))
        idMove = cursorPrestamo.getInt(cursorPrestamo.getColumnIndex(DBMan.DBPrestamo.IdMovimiento))
        fecha = cursorPrestamo.getString(cursorPrestamo.getColumnIndex(DBMan.DBPrestamo.Fecha))

        etCant.setText(instance.format(cant))
        etComment.setText(comment)
        etCambio.setText(cambio.toString())
        tvFecha.setText(fecha)

        adapterList = myAdapter(applicationContext, cursorDetalle)
        listView.adapter = adapterList

        cursorMoneda = Principal.getMoneda()
        cursorCuenta = Principal.getTotales(false)
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

        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            Toast.makeText(applicationContext, "$id", Toast.LENGTH_LONG).show()
            val oldCant = cursorDetalle.getDouble(cursorDetalle.getColumnIndex(DBMan.DBPrestamoDetalle.Cantidad))
            val oldCambio = cursorDetalle.getDouble(cursorDetalle.getColumnIndex(DBMan.DBPrestamoDetalle.Cambio))
            val oldCuenta = cursorDetalle.getInt(cursorDetalle.getColumnIndex(DBMan.DBPrestamoDetalle.IdTotales))
            val oldMoneda = cursorDetalle.getInt(cursorDetalle.getColumnIndex(DBMan.DBPrestamoDetalle.IdMoneda))
            val builder = AlertDialog.Builder(this@seePrestamoActivity)

            // Set the alert dialog title
            builder.setTitle("Agregar Pago")
            val tvMoneda = TextView(applicationContext)
            val spCuenta = Spinner(applicationContext)
            val cursorCuenta = Principal.getTotales(false)
            val adapterCuenta = SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursorCuenta, arrayOf("Cuenta"), intArrayOf(android.R.id.text1), 0)
            spCuenta.adapter = adapterCuenta
            val etCantidad = EditText(applicationContext)
            val etCambio = EditText(applicationContext)
            val layout = LinearLayout(applicationContext)
            val linear2 = LinearLayout(applicationContext)
            val relativeLayout = RelativeLayout(applicationContext)
            layout.weightSum = 2f
            linear2.weightSum = 2f
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
            tvMoneda.layoutParams = lp2
            etCantidad.layoutParams = lp2
            etCambio.layoutParams = lp2
            layout.layoutParams = rl
            layout.id = 2
            linear2.layoutParams = r2
            r2.addRule(RelativeLayout.BELOW, 2)
            etCantidad.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            etCambio.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            etCantidad.setText(oldCant.toString())
            etCambio.setText(oldCambio.toString())
            var j = 0
            while (j < spCuenta.adapter.count) {
                val value = spCuenta.getItemAtPosition(j) as Cursor
                val id = value.getInt(value.getColumnIndex("_id"))
                if (id == idCuenta) {
                    spCuenta.setSelection(j)
                    j = spCuenta.adapter.count + 1
                }
                j++
            }
            spCuenta.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    var idMon = Principal.getIdMonedaTotales(id.toInt())
                    tvMoneda.text = Principal.getIdMoneda(idMon)
                    if (idMon != idMoneda) {
                        if(idMon == oldMoneda){
                            etCambio.setText(oldCambio.toString())
                        } else {
                            etCambio.setText(Principal.getTipodeCambio(idMon, idMoneda))
                        }
                        etCambio.visibility = View.VISIBLE
                    } else {
                        etCambio.setText("1.0")
                        etCambio.visibility = View.GONE
                    }
                }
            })
            builder.setPositiveButton("OK") { dialog, which ->
                val cant = etCantidad.text.toString().toDouble()
                val idCuenta = cursorCuenta.getInt(cursorCuenta.getColumnIndex("_id"))
                val idMon = Principal.getIdMonedaTotales(idCuenta)
                if(idMon != Principal.getIdMonedaTotales(idCuenta)){
                    Toast.makeText(applicationContext, "No se puede modificar porque las monedas no coinciden",Toast.LENGTH_LONG).show()
                } else {
                    val cambio = etCambio.text.toString().toDouble()
                    Principal.updatePrestamoDetalle(cant, idCuenta, idMon, this.id, cambio, id.toInt())
                    if(idMove != null && idMove != 0){
                        Principal.updateTotalesFromPrestamo(oldCant, idCuenta)
                        Principal.updateTotalesFromPrestamo(cant * -1, idCuenta)
                    } else {
                        Principal.updateTotalesFromPrestamo(oldCant *-1, idCuenta)
                        Principal.updateTotalesFromPrestamo(cant, idCuenta)
                    }
                }
            }

            builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }
            layout.addView(spCuenta)
            layout.addView(tvMoneda)
            linear2.addView(etCantidad)
            linear2.addView(etCambio)
            relativeLayout.addView(linear2)
            relativeLayout.addView(layout)
            builder.setView(relativeLayout)
            // Finally, make the alert dialog using builder
            val dialog: AlertDialog = builder.create()
            // Display the alert dialog on app interface
            dialog.show()
        }

        fab.setOnClickListener {
            if(!editable){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    fab.setImageDrawable(resources.getDrawable(R.drawable.check, applicationContext.theme))
                }
                tvFecha.setTextColor(Color.BLACK)
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
                Principal.updatePrestamo(id, cantidad, cam, idCuenta, idMoneda, idPersona, comment, fecha)
                val idMove = cursorPrestamo.getInt(cursorPrestamo.getColumnIndex(DBMan.DBPrestamo.IdMovimiento))
                if(idMove != 0){
                    Principal.updateMoveFromPrestamo(idMove, cantidad, cam, idMoneda)
                }
                tvFecha.setTextColor(Color.GRAY)
                finish()
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
        tvFecha.setOnClickListener{
            if(!editable){
                return@setOnClickListener
            }
            val alertDialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val dfs = DateFormatSymbols()
                val months = dfs.months
                val date = months[monthOfYear].substring(0, 3) + "-" + dayOfMonth.toString() + "-" + year.toString()
                tvFecha.setText(date)
                val m: String
                val d: String
                if (dayOfMonth < 10)
                    d = "0$dayOfMonth"
                else
                    d = "" + dayOfMonth
                if (monthOfYear < 9)
                    m = "0" + (monthOfYear + 1)
                else
                    m = (monthOfYear + 1).toString() + ""
                fecha = "$year-$m-$d"
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            alertDialog.show()
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
            builder.setTitle(getString(R.string.add_payment))
            val tvMoneda = TextView(applicationContext)
            val spCuenta = Spinner(applicationContext)
            val cursorCuenta = Principal.getTotales(false)
            val adapterCuenta = SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursorCuenta, arrayOf("Cuenta"), intArrayOf(android.R.id.text1), 0)
            spCuenta.adapter = adapterCuenta
            val etCantidad = EditText(applicationContext)
            val etCambio = EditText(applicationContext)
            val layout = LinearLayout(applicationContext)
            val linear2 = LinearLayout(applicationContext)
            val relativeLayout = RelativeLayout(applicationContext)
            layout.weightSum = 2f
            linear2.weightSum = 2f
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
            tvMoneda.layoutParams = lp2
            etCantidad.layoutParams = lp2
            etCambio.layoutParams = lp2
            layout.layoutParams = rl
            layout.id = 1
            linear2.layoutParams = r2
            r2.addRule(RelativeLayout.BELOW, 1)
            etCantidad.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            etCambio.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            etCantidad.setText((cant).toString())
            var j = 0
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
            spCuenta.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    var idMon = Principal.getIdMonedaTotales(id.toInt())
                    tvMoneda.text = Principal.getIdMoneda(idMon)
                    if (idMon != idMoneda) {
                        etCambio.setText(Principal.getTipodeCambio(idMon, idMoneda))
                        etCambio.visibility = View.VISIBLE
                    } else {
                        etCambio.setText("1.0")
                        etCambio.visibility = View.GONE
                    }
                }
            })
            builder.setPositiveButton("OK") { dialog, which ->
                val cant = etCantidad.text.toString().toDouble()
                val idCuenta = cursorCuenta.getInt(cursorCuenta.getColumnIndex("_id"))
                val idMon = Principal.getIdMonedaTotales(idCuenta)
                if(idMon != Principal.getIdMonedaTotales(idCuenta)){
                    Toast.makeText(applicationContext, getString(R.string.err_add_dif_curr),Toast.LENGTH_LONG).show()
                } else {
                    val cambio = etCambio.text.toString().toDouble()
                    if(idMove != null && idMove != 0){
                        Principal.insertPrestamoDetalle(cant, idCuenta, idMon, this.id, cambio)
                        Principal.updateTotalesFromPrestamo(cant * -1, idCuenta)
                    } else {
                        Principal.insertPrestamoDetalle(cant, idCuenta, idMon, this.id, cambio)
                        Principal.updateTotalesFromPrestamo(cant, idCuenta)
                    }
                }
            }

            builder.setNegativeButton(getString(R.string.cancel)) { dialog, which -> dialog.cancel() }
            layout.addView(spCuenta)
            layout.addView(tvMoneda)
            linear2.addView(etCantidad)
            linear2.addView(etCambio)
            relativeLayout.addView(linear2)
            relativeLayout.addView(layout)
            builder.setView(relativeLayout)
            // Finally, make the alert dialog using builder
            val dialog: AlertDialog = builder.create()
            // Display the alert dialog on app interface
            dialog.show()
        }
        return super.onOptionsItemSelected(item)
    }
    inner class myAdapter(context: Context, cursor: Cursor) : CursorAdapter(context, cursor, 0) {
        override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
            return LayoutInflater.from(context).inflate(R.layout.list_prestamo_detalle, parent, false)
        }

        override fun bindView(view: View, context: Context, cursor: Cursor) {
            // Find fields to populate in inflated template
            val tvFecha = view.findViewById<TextView>(R.id.TVFecha)
            val tvCantidad = view.findViewById<TextView>(R.id.TVCant)
            val tvCuenta = view.findViewById<TextView>(R.id.TVCuenta)

            // Extract properties from cursor
            val cantidad = cursor.getDouble(cursor.getColumnIndex(DBMan.DBPrestamoDetalle.Cantidad))
            val fecha = cursor.getString(cursor.getColumnIndex(DBMan.DBPrestamoDetalle.Fecha))
            val cuenta = Principal.getCuentaTotales(cursor.getInt(cursor.getColumnIndex(DBMan.DBPrestamoDetalle.IdTotales)))
            val moneda = Principal.getIdMoneda(cursor.getInt(cursor.getColumnIndex(DBMan.DBPrestamoDetalle.IdMoneda)))

            //set
            tvCantidad.text = "$ ${instance.format(cantidad)} $moneda"
            tvFecha.text = fecha
            tvCuenta.text = cuenta
        }
    }
}
