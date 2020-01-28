package com.lala

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.cursoradapter.widget.CursorAdapter
import androidx.appcompat.app.AppCompatActivity
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
import android.widget.EditText
import android.view.LayoutInflater
import java.text.ParseException
import java.text.SimpleDateFormat


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
    private var totalPaid = 0.0
    private val instance = NumberFormat.getInstance()
    private lateinit var adapterMoneda: SimpleCursorAdapter
    private lateinit var adapterCuenta: SimpleCursorAdapter
    private lateinit var adapterPersona: SimpleCursorAdapter
    private var editable = false
    private lateinit var listView: ListView
    private var calendar = Calendar.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_see_prestamo)
        setSupportActionBar(toolbar)

        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material)

        toolbar.setNavigationOnClickListener(View.OnClickListener {
            finish()
            //handleOnBackPress();
        })
        
        id = intent.getIntExtra("_id", 0)
        cursorPrestamo = Principal.getCursorPrestamo(id)
        cursorDetalle = Principal.getPrestamoDetalle(id)
        totalPaid = Principal.getTotalPaid(id)
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
        cursorCuenta = Principal.getTotalesWithPrestamo()
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
            var gasto = false
            var fechaPayment: String? = null

            val dialogBuilder = AlertDialog.Builder(this@seePrestamoActivity)
            dialogBuilder.setTitle(getString(R.string.add_payment))

            val inflater = this.layoutInflater
            val dialogView = inflater.inflate(R.layout.dialog_add_payment, null)
            dialogBuilder.setView(dialogView)

            val tvMoneda = dialogView.findViewById(R.id.lblMoneda) as TextView
            val spCuenta = dialogView.findViewById(R.id.spCuenta) as Spinner
            val etCantidad = dialogView.findViewById(R.id.etCantidad) as EditText
            val etCambio = dialogView.findViewById(R.id.etCambio) as EditText
            val gastoBtn = dialogView.findViewById(R.id.gastoBtn) as RadioButton
            val ingresoBtn = dialogView.findViewById(R.id.ingresoBtn) as RadioButton
            val hoyBtn = dialogView.findViewById(R.id.hoyBtn) as RadioButton
            val ayerBtn = dialogView.findViewById(R.id.ayerBtn) as RadioButton
            val otroDate = dialogView.findViewById(R.id.otroBtn) as RadioButton

            val cursorCuenta = Principal.getTotales(false)
            val adapterCuenta = SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursorCuenta, arrayOf("Cuenta"), intArrayOf(android.R.id.text1), 0)
            spCuenta.adapter = adapterCuenta

            var tmpCant = oldCant
            etCantidad.setText("${(if(tmpCant < 0) tmpCant * -1 else tmpCant)}")
            etCambio.setText(oldCambio.toString())
            if(tmpCant < 0){
                gastoBtn.isChecked = true
                gasto = true
            } else {
                ingresoBtn.isChecked = true
            }
            gastoBtn.setOnClickListener {
                gasto = true
            }
            ingresoBtn.setOnClickListener {
                gasto = false
            }
            hoyBtn.setOnClickListener {
                fechaPayment = null
            }
            ayerBtn.setOnClickListener {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd")
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_MONTH, -1)
                fechaPayment = dateFormat.format(cal.time)
                val i = 0
            }
            otroDate.setOnClickListener {
                val cal = Calendar.getInstance()
                try {
                    val sdf = SimpleDateFormat("MMM-dd-yyyy")
                    if (fechaPayment != null) {
                        cal.time = sdf.parse(fechaPayment)// all done
                    }
                } catch (e: ParseException) {
                    e.printStackTrace()
                }

                val alertDialog = DatePickerDialog(this@seePrestamoActivity, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    val dfs = DateFormatSymbols()
                    val months = dfs.months
                    val date = months[monthOfYear].substring(0, 3) + "-" + dayOfMonth.toString() + "-" + year.toString()
                    otroDate.text = date
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
                    fechaPayment = "$year-$m-$d"
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                //AlertDialog alertDialog2 = alertDialog.show();
                alertDialog.show()
                alertDialog.setCanceledOnTouchOutside(false)
            }
            var j = 0
            while (j < spCuenta.adapter.count) {
                val value = spCuenta.getItemAtPosition(j) as Cursor
                val id = value.getInt(value.getColumnIndex("_id"))
                if (id == oldCuenta) {
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
                            etCambio.setText(Principal.getTipodeCambio(idMon, idMoneda).toString())
                        }
                        etCambio.visibility = View.VISIBLE
                    } else {
                        etCambio.setText("1.0")
                        etCambio.visibility = View.GONE
                    }
                }
            })

            dialogBuilder.setPositiveButton("OK") { dialog, which ->
                var cant = etCantidad.text.toString().toDouble()
                val idCuenta = cursorCuenta.getInt(cursorCuenta.getColumnIndex("_id"))
                val idMon = Principal.getIdMonedaTotales(idCuenta)
                if(gasto) cant = cant*-1
                if(idMon != Principal.getIdMonedaTotales(idCuenta)){
                    Toast.makeText(applicationContext, "No se puede modificar porque las monedas no coinciden",Toast.LENGTH_LONG).show()
                } else {
                    val cambio = etCambio.text.toString().toDouble()
                    Principal.updatePrestamoDetalle(cant, idCuenta, idMon, this.id, cambio, id.toInt(), fechaPayment)
                    if(idMove != null && idMove != 0){
                        Principal.updateTotalesFromPrestamo(oldCant, oldCuenta)
                        Principal.updateTotalesFromPrestamo(cant * -1, idCuenta)
                    } else {
                        Principal.updateTotalesFromPrestamo(oldCant *-1, oldCuenta)
                        Principal.updateTotalesFromPrestamo(cant, idCuenta)
                    }
                }
            }

            dialogBuilder.setNegativeButton(getString(R.string.cancel)) { dialog, which -> dialog.cancel() }
            val alertDialog = dialogBuilder.create()
            alertDialog.show()
            alertDialog.setCanceledOnTouchOutside(false)



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
            alertDialog.setCanceledOnTouchOutside(false)
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

            var gasto = false
            var fechaPayment: String? = null
            val dialogBuilder = AlertDialog.Builder(this@seePrestamoActivity)
            dialogBuilder.setTitle(getString(R.string.add_payment))

            val inflater = this.layoutInflater
            val dialogView = inflater.inflate(R.layout.dialog_add_payment, null)
            dialogBuilder.setView(dialogView)

            val tvMoneda = dialogView.findViewById(R.id.lblMoneda) as TextView
            val spCuenta = dialogView.findViewById(R.id.spCuenta) as Spinner
            val etCantidad = dialogView.findViewById(R.id.etCantidad) as EditText
            val etCambio = dialogView.findViewById(R.id.etCambio) as EditText
            val gastoBtn = dialogView.findViewById(R.id.gastoBtn) as RadioButton
            val ingresoBtn = dialogView.findViewById(R.id.ingresoBtn) as RadioButton
            val hoyBtn = dialogView.findViewById(R.id.hoyBtn) as RadioButton
            val ayerBtn = dialogView.findViewById(R.id.ayerBtn) as RadioButton
            val otroDate = dialogView.findViewById(R.id.otroBtn) as RadioButton

            val cursorCuenta = Principal.getTotales(false)
            val adapterCuenta = SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursorCuenta, arrayOf("Cuenta"), intArrayOf(android.R.id.text1), 0)
            spCuenta.adapter = adapterCuenta
            var tmpCant = cant - totalPaid
            etCantidad.setText("${(if(tmpCant < 0) tmpCant * -1 else tmpCant)}")
            if(tmpCant < 0){
                gastoBtn.isChecked = true
                gasto = true
            } else {
                ingresoBtn.isChecked = true
            }
            gastoBtn.setOnClickListener {
                gasto = true
            }
            ingresoBtn.setOnClickListener {
                gasto = false
            }
            hoyBtn.setOnClickListener {
                fechaPayment = null
            }
            ayerBtn.setOnClickListener {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd")
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_MONTH, -1)
                fechaPayment = dateFormat.format(cal.time)
                val i = 0
            }
            otroDate.setOnClickListener {
                val cal = Calendar.getInstance()
                try {
                    val sdf = SimpleDateFormat("MMM-dd-yyyy")
                   if (fechaPayment != null) {
                        cal.time = sdf.parse(fechaPayment)// all done
                   }
                } catch (e: ParseException) {
                    e.printStackTrace()
                }

                val alertDialog = DatePickerDialog(this@seePrestamoActivity, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    val dfs = DateFormatSymbols()
                    val months = dfs.months
                    val date = months[monthOfYear].substring(0, 3) + "-" + dayOfMonth.toString() + "-" + year.toString()
                    otroDate.text = date
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
                    fechaPayment = "$year-$m-$d"
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                //AlertDialog alertDialog2 = alertDialog.show();
                alertDialog.show()
                alertDialog.setCanceledOnTouchOutside(false)
            }
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
                        etCambio.setText(Principal.getTipodeCambio(idMon, idMoneda).toString())
                        etCambio.visibility = View.VISIBLE
                    } else {
                        etCambio.setText("1.0")
                        etCambio.visibility = View.GONE
                    }
                }
            })

            dialogBuilder.setPositiveButton("OK") { dialog, which ->
                var cant = etCantidad.text.toString().toDouble()
                val idCuenta = cursorCuenta.getInt(cursorCuenta.getColumnIndex("_id"))
                val idMon = Principal.getIdMonedaTotales(idCuenta)
                if(gasto) cant = cant*-1
                if(idMon != Principal.getIdMonedaTotales(idCuenta)){
                    Toast.makeText(applicationContext, getString(R.string.err_add_dif_curr),Toast.LENGTH_LONG).show()
                } else {
                    val cambio = etCambio.text.toString().toDouble()
                    //if(idMove != null && idMove != 0){
                        Principal.insertPrestamoDetalle(cant, idCuenta, idMon, this.id, cambio, fechaPayment)
                        Principal.updateTotalesFromPrestamo(cant, idCuenta)
                    //} else {
                    //    Principal.insertPrestamoDetalle(cant, idCuenta, idMon, this.id, cambio)
                    //    Principal.updateTotalesFromPrestamo(cant, idCuenta)
                    //}
                }
            }
            dialogBuilder.setNegativeButton(getString(R.string.cancel)) { dialog, which -> dialog.cancel() }
            val alertDialog = dialogBuilder.create()
            alertDialog.show()
            alertDialog.setCanceledOnTouchOutside(false)

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
