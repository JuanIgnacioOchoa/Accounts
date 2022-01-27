package com.lala

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.cursoradapter.widget.CursorAdapter
import androidx.appcompat.app.AppCompatActivity;
import android.widget.*
import androidx.core.content.ContextCompat
import com.mynameismidori.currencypicker.ExtendedCurrency

import kotlinx.android.synthetic.main.activity_see_trip_main.*
import java.text.DateFormatSymbols
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
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
    private lateinit var spMonedaTotals:Spinner
    private lateinit var lvMovimientos:ListView
    private lateinit var listViewTotales: ListView
    private var editable:Boolean = false;
    private var instance: NumberFormat = NumberFormat.getInstance()
    private val calendar = Calendar.getInstance()
    private lateinit var cursorTrip:Cursor
    private lateinit var cursorMoves:Cursor
    //private lateinit var adapterMoves:myAdapter
    private lateinit var adapterMoves:myAdapterFecha
    private lateinit var cursorMoneda:Cursor
    private lateinit var adapterTotals:myAdapterCurrncies
    private lateinit var cursorTotals:Cursor

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
        val moneda = cursorTrip.getInt(cursorTrip.getColumnIndex(DBMan.DBViaje.IdMoneda))
        cont = applicationContext
        setTitle("$nombre")
        etNombre = findViewById<EditText>(R.id.ET_TR_Nombre)
        etDesc = findViewById<EditText>(R.id.ET_TR_Desc)
        tvFechaInic = findViewById<TextView>(R.id.TV_TR_FechaInic)
        tvFechaFin = findViewById<TextView>(R.id.TV_TR_FechaFin)
        spMoneda = findViewById<Spinner>(R.id.SP_TR_Moneda)
        spMonedaTotals = findViewById(R.id.sp_ll_trip_moneda)
        lvMovimientos = findViewById<ListView>(R.id.LV_Movimiento)
        listViewTotales = findViewById<ListView>(R.id.lv_total_currency)
        var fechaInic: String? = null
        var fechaFin: String? = null
        etNombre.isEnabled = false
        etDesc.isEnabled = false
        spMoneda.isEnabled = false
        spMonedaTotals.isEnabled = true
        cursorMoves = Principal.getMovesByTrips(_id)
        adapterMoves = myAdapterFecha(applicationContext, cursorMoves)
        lvMovimientos.setAdapter(adapterMoves)
        lvMovimientos.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val i = Intent(applicationContext, seeMove::class.java)
            val id = cursorMoves.getInt(cursorMoves.getColumnIndex("_id"))
            i.putExtra("id", id)
            i.putExtra("idTrip", _id)
            startActivity(i)
        }
        fechaFin = cursorTrip.getString(cursorTrip.getColumnIndex(DBMan.DBViaje.FechaFin))
        fechaInic = cursorTrip.getString(cursorTrip.getColumnIndex(DBMan.DBViaje.FechaInicio))
        cursorMoneda = Principal.getMonedaWith(moneda);
        spMoneda.adapter = SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursorMoneda, arrayOf("Moneda"), intArrayOf(android.R.id.text1), 0)
        spMonedaTotals.adapter = SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursorMoneda, arrayOf("Moneda"), intArrayOf(android.R.id.text1), 0)
        etNombre.setText(cursorTrip.getString(cursorTrip.getColumnIndex(DBMan.DBViaje.Nombre)))
        etDesc.setText(cursorTrip.getString(cursorTrip.getColumnIndex(DBMan.DBViaje.Descripcion)))
        tvFechaFin.text = fechaFin
        tvFechaInic.text = fechaInic
        var idMoneda = -1
        var j = 0
        while (j < spMoneda.adapter.count) {
            val value = spMoneda.getItemAtPosition(j) as Cursor
            val id = value.getInt(value.getColumnIndex("_id"))
            idMoneda = cursorTrip.getInt(cursorTrip.getColumnIndex(DBMan.DBViaje.IdMoneda))
            if (id == idMoneda) {
                spMoneda.setSelection(j)
                spMonedaTotals.setSelection(j)
                j = spMoneda.adapter.count + 1
            }
            j++
        }
        cursorTotals = Principal.getTripTotalByCurrency(idMoneda, _id)
        adapterTotals = myAdapterCurrncies(applicationContext, cursorTotals)
        listViewTotales.setAdapter(adapterTotals)

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
        spMonedaTotals.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                var idMoneda = id.toInt()
                cursorTotals = Principal.getTripTotalByCurrency(idMoneda, _id)
                while(cursorTotals.moveToNext()){
                    var moneda = cursorTotals.getString(cursorTotals.getColumnIndex("Moneda"))
                    var ihlsd = 0
                }

                cursorTotals = Principal.getTripTotalByCurrency(idMoneda, _id)
                adapterTotals = myAdapterCurrncies(applicationContext, cursorTotals)
                listViewTotales.adapter = adapterTotals
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    inner class myAdapterFecha(context: Context, cursor: Cursor) : CursorAdapter(context, cursor, 0) {
        override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
            return LayoutInflater.from(context).inflate(R.layout.inflate_dates, parent, false)
        }

        override fun bindView(view: View, context: Context, cursor: Cursor) {
            // Find fields to populate in inflated template
            val tvFecha = view.findViewById(R.id.tv_inflate_date) as TextView
            val linearLayout = view.findViewById(R.id.linear_inflate_dates) as LinearLayout
            linearLayout.removeAllViewsInLayout()
            // Extract properties from cursor
            //int id = 0;
            //id = 0;
            var fecha = cursor.getString(cursor.getColumnIndex(DBMan.DBMovimientos.Fecha))
            val c = Principal.getTripMovesByFecha(fecha, _id)
            val x = c.count
            //c.moveToNext();
            while (c.moveToNext()) {
                val linear = LinearLayout(context)
                linear.weightSum = 3f
                val lp1 = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
                val lp2 = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                val cuentaTV = TextView(context)
                val motivoTV = TextView(context)
                val cantidadTV = TextView(context)
                linear.layoutParams = lp1
                cuentaTV.layoutParams = lp2
                motivoTV.layoutParams = lp2
                cantidadTV.layoutParams = lp2
                val cuenta = Principal.getCuentaTotales(c.getInt(c.getColumnIndex("IdTotales")))
                val cantidad = c.getDouble(c.getColumnIndex(DBMan.DBMovimientos.Cantidad))
                val idMoneda = c.getInt(c.getColumnIndex("IdMoneda"))
                var moneda = Principal.getIdMoneda(idMoneda)
                var motivo = Principal.getMotiveId(c.getInt(c.getColumnIndex(DBMan.DBMovimientos.IdMotivo)))
                val idTraspaso = c.getInt(c.getColumnIndex("Traspaso"))
                val id = c.getInt(c.getColumnIndex("_id"))
                if (idTraspaso != 0) {
                    motivo += " " + Principal.getCuentaTotales(idTraspaso)
                    moneda = ""
                    cuentaTV.setTextColor(ContextCompat.getColor(context, R.color.neutral_yellow))
                    motivoTV.setTextColor(ContextCompat.getColor(context, R.color.neutral_yellow))
                    cantidadTV.setTextColor(ContextCompat.getColor(context, R.color.neutral_yellow))
                } else if (cantidad < 0) {
                    cuentaTV.setTextColor(Color.RED)
                    motivoTV.setTextColor(Color.RED)
                    cantidadTV.setTextColor(Color.RED)
                } else {
                    cuentaTV.setTextColor(ContextCompat.getColor(context, R.color.positive_green))
                    motivoTV.setTextColor(ContextCompat.getColor(context, R.color.positive_green))
                    cantidadTV.setTextColor(ContextCompat.getColor(context, R.color.positive_green))
                }
                cuentaTV.text = cuenta
                motivoTV.text = motivo
                cantidadTV.text = instance.format(cantidad) + " " + moneda
                cantidadTV.gravity = Gravity.RIGHT
                linear.addView(cuentaTV)
                linear.addView(motivoTV)
                linear.addView(cantidadTV)
                linearLayout.addView(linear)

                linear.setOnTouchListener { v, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> v.setBackgroundColor(Color.LTGRAY)
                        MotionEvent.ACTION_UP -> {
                            val i: Intent
                            if (idTraspaso == 0) {
                                i = Intent(context, Gasto::class.java)
                                i.putExtra("id", id)
                            } else {
                                i = Intent(context, Traspaso::class.java)
                                i.putExtra("_id", id)
                            }
                            startActivity(i)
                        }
                        MotionEvent.ACTION_MOVE -> v.setBackgroundColor(Color.TRANSPARENT)
                    }
                    true
                }
            }

            // Populate fields with extracted properties
            val calendar = Calendar.getInstance()
            try {
                var sdf = SimpleDateFormat("yyyy-MM-dd")
                calendar.time = sdf.parse(fecha)// all done
                sdf = SimpleDateFormat("EEEE, d MMMM, yyyy")
                fecha = sdf.format(calendar.time)
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            tvFecha.text = fecha
        }
    }

    inner class myAdapterCurrncies(context: Context, cursor: Cursor) : CursorAdapter(context, cursor, 0) {
        override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
            return LayoutInflater.from(context).inflate(R.layout.inflate_currency_totals, parent, false)
        }

        override fun bindView(view: View, context: Context, cursor: Cursor) {
            // Find fields to populate in inflated template
            val tvTotal = view.findViewById(R.id.total) as TextView
            val ivFlag = view.findViewById<ImageView>(R.id.flag)


            var moneda = cursor.getString(cursor.getColumnIndex("Moneda"))
            var total = cursor.getDouble(cursor.getColumnIndex("Total"))
            val extendedCurrency = ExtendedCurrency.getCurrencyByISO(moneda)
            var i = 0;
            if (total == 0.0) {
                tvTotal.setTextColor(ContextCompat.getColor(context, R.color.neutral_yellow))
            } else if (total < 0) {
                tvTotal.setTextColor(Color.RED)
            } else {
                tvTotal.setTextColor(ContextCompat.getColor(context, R.color.positive_green))
            }
            tvTotal.text = "$" + instance.format(total) + " " + moneda

            if(extendedCurrency != null) {
                ivFlag.setImageResource(extendedCurrency.flag)
            }
        }
    }

}
