package com.lala

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import android.view.View
import android.widget.*

import kotlinx.android.synthetic.main.activity_create_trip.*
import java.text.DateFormatSymbols
import java.util.*

class CreateTrip : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_trip)
        setSupportActionBar(toolbar)
        setTitle(getString(R.string.title_activity_create_trip))
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material)

        toolbar.setNavigationOnClickListener {
            finish()
            //handleOnBackPress();
        }
        val calendar = Calendar.getInstance()
        val etNombre = findViewById<EditText>(R.id.ET_TR_Nombre)
        val etDesc = findViewById<EditText>(R.id.ET_TR_Desc)
        val tvFechaInic = findViewById<TextView>(R.id.TV_TR_FechaInic)
        val tvFechaFin = findViewById<TextView>(R.id.TV_TR_FechaFin)
        val spMoneda = findViewById<Spinner>(R.id.SP_TR_Moneda)
        val cursorMoneda = Principal.getMoneda();
        val from = arrayOf("Moneda")
        val to = intArrayOf(android.R.id.text1)
        val cursorAdapterMoneda = SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursorMoneda, from, to, 0)
        var fechaInic: String? = null
        var fechaFin: String? = null
        spMoneda.adapter = cursorAdapterMoneda
        fab.setOnClickListener { view ->
            val moneda = cursorMoneda.getInt(cursorMoneda.getColumnIndex("_id"))
            val nombre = etNombre.text.toString()
            val descr = etDesc.text.toString()
            if(Principal.createTrip(nombre, fechaInic, fechaFin, moneda, descr)){
                finish()
            }
        }
        tvFechaInic.setOnClickListener{
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
        if(cursorMoneda.count == 0){
            val builder = AlertDialog.Builder(this@CreateTrip)
            builder.setTitle(getString(R.string.alert_info_data))
            builder.setMessage(getString(R.string.alert_info_data_msg_tri))

// Set up the buttons
            builder.setPositiveButton("OK") { dialog, which -> finish() }
            val alertDialog = builder.show()
            alertDialog.setCanceledOnTouchOutside(false)
        }
    }

}
