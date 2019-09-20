package com.lala

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import androidx.cursoradapter.widget.CursorAdapter
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import kotlinx.android.synthetic.main.activity_see_trip_main.*
import java.text.DateFormatSymbols
import java.text.NumberFormat
import java.util.*

class SeeTripMainActivity : AppCompatActivity() {

    private var title:String = "????"
    private var _id:Int = 0
    private lateinit var cont:Context
    private lateinit var etNombre:EditText
    private lateinit var etDesc:EditText
    private lateinit var tvFechaInic:TextView
    private lateinit var tvFechaFin:TextView
    private lateinit var spMoneda:Spinner
    private lateinit var lvMovimientos:ListView
    private var editable:Boolean = false;
    private var instance: NumberFormat = NumberFormat.getInstance()
    private val calendar = Calendar.getInstance()
    private lateinit var cursorTrip:Cursor
    private lateinit var cursorMoves:Cursor
    private lateinit var adapterMoves:myAdapter
    private lateinit var cursorMoneda:Cursor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_see_trip_main)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material)

        toolbar.setNavigationOnClickListener {
            finish()
            //handleOnBackPress();
        }

        _id = intent.getIntExtra("_id", 0)
        cursorTrip = Principal.getTrip(_id)
        val nombre = cursorTrip.getString(cursorTrip.getColumnIndex(DBMan.DBViaje.Nombre))
        cont = applicationContext
        setTitle("$nombre($_id)")
        etNombre = findViewById<EditText>(R.id.ET_TR_Nombre)
        etDesc = findViewById<EditText>(R.id.ET_TR_Desc)
        tvFechaInic = findViewById<TextView>(R.id.TV_TR_FechaInic)
        tvFechaFin = findViewById<TextView>(R.id.TV_TR_FechaFin)
        spMoneda = findViewById<Spinner>(R.id.SP_TR_Moneda)
        lvMovimientos = findViewById<ListView>(R.id.LV_Movimiento)
        var fechaInic: String? = null
        var fechaFin: String? = null
        etNombre.isEnabled = false
        etDesc.isEnabled = false
        spMoneda.isEnabled = false
        cursorMoves = Principal.getMovesByTrips(_id)
        adapterMoves = myAdapter(applicationContext, cursorMoves)
        lvMovimientos.setAdapter(adapterMoves)
        lvMovimientos.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val i = Intent(applicationContext, seeMove::class.java)
            val id = cursorMoves.getInt(cursorMoves.getColumnIndex("_id"))
            i.putExtra("id", id)
            i.putExtra("idTrip", _id)
            startActivity(i)
        }
        cursorMoneda = Principal.getMoneda();
        spMoneda.adapter = SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursorMoneda, arrayOf("Moneda"), intArrayOf(android.R.id.text1), 0)
        etNombre.setText(cursorTrip.getString(cursorTrip.getColumnIndex(DBMan.DBViaje.Nombre)))
        etDesc.setText(cursorTrip.getString(cursorTrip.getColumnIndex(DBMan.DBViaje.Descripcion)))
        tvFechaFin.text = cursorTrip.getString(cursorTrip.getColumnIndex(DBMan.DBViaje.FechaFin))
        tvFechaInic.text = cursorTrip.getString(cursorTrip.getColumnIndex(DBMan.DBViaje.FechaInicio))
        var j = 0
        while (j < spMoneda.adapter.count) {
            val value = spMoneda.getItemAtPosition(j) as Cursor
            val id = value.getInt(value.getColumnIndex("_id"))
            val idMoneda = cursorTrip.getInt(cursorTrip.getColumnIndex(DBMan.DBViaje.IdMoneda))
            if (id == idMoneda) {
                spMoneda.setSelection(j)
                j = spMoneda.adapter.count + 1
            }
            j++
        }
        //TODO hide keyboard
        //SetOnClickListeners
        fab.setOnClickListener {
            val colors = arrayOf<CharSequence>(
                    getString(R.string.outcome),
                    getString(R.string.income),
                    getString(R.string.transfer),
                    getString(R.string.withrawal)
            )

            // Initialize a new instance of
            val builder = AlertDialog.Builder(this@SeeTripMainActivity)

            // Set the alert dialog title
            builder.setTitle(getString(R.string.choose_option))

            builder.setItems(colors) { dialog, which ->
                val s = which.toString()
                Toast.makeText(cont, s, Toast.LENGTH_LONG).show()
                when (which) {
                    0 -> {
                        val i = Intent(cont, Gasto::class.java)
                        i.putExtra("Gasto", true)
                        i.putExtra("IdViaje", _id)
                        startActivity(i)
                    }
                    1 -> {
                        val i = Intent(cont, Gasto::class.java)
                        i.putExtra("Gasto", false)
                        i.putExtra("IdViaje", _id)
                        startActivity(i)
                    }
                    2 -> {
                        val i = Intent(cont, Traspaso::class.java)
                        i.putExtra("Retiro", false)
                        i.putExtra("IdViaje", _id)
                        startActivity(i)
                    }
                    else -> {
                        val i = Intent(cont, Traspaso::class.java)
                        i.putExtra("Retiro", true)
                        i.putExtra("IdViaje", _id)
                        startActivity(i)
                    }
                }
                // the user clicked on colors[which]
            }

            // Finally, make the alert dialog using builder
            val dialog: AlertDialog = builder.create()

            // Display the alert dialog on app interface
            val alertDialog = builder.show()
            alertDialog.setCanceledOnTouchOutside(false)
        }
        editFab.setOnClickListener{
            if(!editable) {
                etNombre.isEnabled = true
                etDesc.isEnabled = true
                spMoneda.isEnabled = true
                editable = true
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    editFab.setImageDrawable(resources.getDrawable(R.drawable.check, cont.getTheme()))
                } else
                    editFab.setImageDrawable(resources.getDrawable(R.drawable.check))
            } else{
                etNombre.isEnabled = false
                etDesc.isEnabled = false
                spMoneda.isEnabled = false
                editable = false
                //TODO
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    editFab.setImageDrawable(resources.getDrawable(R.drawable.check, cont.getTheme()))
                } else
                    editFab.setImageDrawable(resources.getDrawable(R.drawable.check))
                val moneda = cursorMoneda.getInt(cursorMoneda.getColumnIndex("_id"))
                Principal.updateTrip(_id, etNombre.text.toString(), etDesc.text.toString(), fechaInic, fechaFin, moneda)
            }
        }
        tvFechaInic.setOnClickListener{
            if(!editable){
                return@setOnClickListener
            }
            val alertDialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val dfs = DateFormatSymbols()
                val months = dfs.months
                val date = months[monthOfYear].substring(0, 3) + "-" + dayOfMonth.toString() + "-" + year.toString()
                tvFechaInic.setText(date)
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
                fechaInic = "$year-$m-$d"
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), DialogInterface.OnClickListener { alertDialog, which ->
                if (which == DialogInterface.BUTTON_NEGATIVE) {
                    tvFechaInic.text = null
                    fechaInic = null
                }
            })
            alertDialog.show()
            alertDialog.setCanceledOnTouchOutside(false)
        }
        tvFechaFin.setOnClickListener(View.OnClickListener {
            if(!editable){
                return@OnClickListener
            }
            val alertDialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val dfs = DateFormatSymbols()
                val months = dfs.months
                val date = months[monthOfYear].substring(0, 3) + "-" + dayOfMonth.toString() + "-" + year.toString()
                tvFechaFin.setText(date)
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
                fechaFin = "$year-$m-$d"
            },calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), DialogInterface.OnClickListener { alertDialog, which ->
                if (which == DialogInterface.BUTTON_NEGATIVE) {
                    tvFechaFin.text = null
                    fechaFin = null
                }
            })
            alertDialog.show()
            alertDialog.setCanceledOnTouchOutside(false)
        })

    }

    override fun onResume() {
        super.onResume()
    }
    inner class myAdapter(context: Context, cursor: Cursor) : CursorAdapter(context, cursor, 0) {
        override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
            return LayoutInflater.from(context).inflate(R.layout.list_movimientos, parent, false)
            cursorMoves = Principal.getMovesByTrips(_id)
            adapterMoves = myAdapter(applicationContext, cursorMoves)
        }

        override fun bindView(view: View, context: Context, cursor: Cursor) {
            // Find fields to populate in inflated template
            val tvCantidad = view.findViewById(R.id.LM_Cantidad) as TextView
            val tvMoneda = view.findViewById(R.id.LM_Moneda) as TextView
            val tvMotivo = view.findViewById(R.id.LM_Motivo) as TextView
            val tvFecha = view.findViewById(R.id.LM_Fecha) as TextView
            val tvCuenta = view.findViewById(R.id.LM_Cuenta) as TextView
            // Extract properties from cursor
            var cantidad: Double? = cursor.getDouble(cursor.getColumnIndex(DBMan.DBMovimientos.Cantidad))
            val fecha = cursor.getString(cursor.getColumnIndex(DBMan.DBMovimientos.Fecha))
            val cuenta = Principal.getCuentaTotales(cursor.getInt(cursor.getColumnIndex(DBMan.DBMovimientos.IdTotales)))
            val moneda = Principal.getIdMoneda(cursor.getInt(cursor.getColumnIndex(DBMan.DBMovimientos.IdMoneda)))
            val motivo = Principal.getMotiveId(cursor.getInt(cursor.getColumnIndex(DBMan.DBMovimientos.IdMotivo)))
            //val descripcion = cursor.getColumnIndex(cursor.getString(cursor.getColumnIndex(DBMan.DBViaje.Descripcion)))
            //set
            tvFecha.text = fecha
            tvMoneda.text = moneda
            tvMotivo.text = motivo
            tvCantidad.text = "$" + instance.format(cantidad)
            tvCuenta.text = cuenta
        }
    }
}
