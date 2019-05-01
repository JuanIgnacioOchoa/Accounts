package com.lala

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.widget.CursorAdapter
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_trips_main.*
import java.text.NumberFormat

class TripsMainActivity : AppCompatActivity() {

    private var instance: NumberFormat = NumberFormat.getInstance()
    private var cursorTrips: Cursor = Principal.getTrips()
    private var adapter: myAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trips_main)
        setSupportActionBar(toolbar)
        setTitle("Viajes")
        fab.setOnClickListener { view ->
            val intent = Intent(this, CreateTrip::class.java)
            startActivity(intent)
        }
        val listView = findViewById<ListView>(R.id.listView_Trips)
        instance.minimumFractionDigits = 2
        adapter = myAdapter(applicationContext, cursorTrips)
        listView.setAdapter(adapter)
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val i = Intent(applicationContext, SeeTripMainActivity::class.java)
            i.putExtra("_id", cursorTrips.getInt(cursorTrips.getColumnIndex("_id")))
            i.putExtra("title", cursorTrips.getString(cursorTrips.getColumnIndex(DBMan.DBViaje.Nombre)))
            startActivity(i)
        }
    }

    inner class myAdapter(context: Context, cursor: Cursor) : CursorAdapter(context, cursor, 0) {
        override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
            return LayoutInflater.from(context).inflate(R.layout.list_trips, parent, false)
        }

        override fun bindView(view: View, context: Context, cursor: Cursor) {
            // Find fields to populate in inflated template
            //TextView tvId = (TextView) view.findViewById(R.id.lv_see_cuentas_id);
            val tvCantidad = view.findViewById(R.id.LT_Cantidad) as TextView
            val tvMoneda = view.findViewById(R.id.LT_Moneda) as TextView
            val tvNombre = view.findViewById(R.id.LT_Nombre) as TextView
            val tvFecha = view.findViewById(R.id.LT_Fecha) as TextView
            // Extract properties from cursor
            var cantidad: Double? = cursor.getDouble(cursor.getColumnIndex(DBMan.DBViaje.CantTotal))
            val fechaInic: String? = cursor.getString(cursor.getColumnIndex(DBMan.DBViaje.FechaInicio))
            val fechaFin: String? = cursor.getString(cursor.getColumnIndex(DBMan.DBViaje.FechaFin))
            val nombre = cursor.getString(cursor.getColumnIndex(DBMan.DBViaje.Nombre))
            val moneda = Principal.getIdMoneda(cursor.getInt(cursor.getColumnIndex(DBMan.DBViaje.IdMoneda)))
            //val descripcion = cursor.getColumnIndex(cursor.getString(cursor.getColumnIndex(DBMan.DBViaje.Descripcion)))
            //set
            var fecha = ""
            if(fechaInic == null && fechaFin == null){
                fecha = "Not Defined - Not Defined"
            } else if(fechaFin == null){
                fecha = "$fechaInic - Not Defined"
            } else if(fechaInic == null){
                fecha = "Not Defined - $fechaFin"
            } else{
                fecha = "$fechaInic - $fechaFin"
            }
            tvFecha.text = fecha
            tvMoneda.text = moneda
            tvNombre.text = nombre
            tvCantidad.text = "$" + instance.format(cantidad)
        }
    }
}
