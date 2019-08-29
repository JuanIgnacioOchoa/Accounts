package com.lala

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.CursorAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import java.text.NumberFormat

class FragmentPrestamoPeople : Fragment() {
    private var instance: NumberFormat = NumberFormat.getInstance()
    private var cursorPrestamos = Principal.getPrestamosByPeople(false)
    private lateinit var listView:ListView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_prestamo_people, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById<ListView>(R.id.LV_Prestamo)
        instance.minimumFractionDigits = 2
        val adapter = context?.let { myAdapter(it,cursorPrestamos) }

        listView.adapter = adapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val idPersona = cursorPrestamos.getInt(cursorPrestamos.getColumnIndex("_id"))
            //TODO Create activity personasPresatmos
            /*
            val i = Intent(context, seePrestamoActivity::class.java)
            i.putExtra("_id", idPersona)
            i.putExtra("_id", cursorPrestamos.getInt(cursorPrestamos.getColumnIndex("_id")))
            i.putExtra("title", cursorPrestamos.getString(cursorPrestamos.getColumnIndex(DBMan.DBPersona.Nombre)))
            startActivity(i)
            */
        }
    }
    fun actualizar(boolean: Boolean){
        cursorPrestamos = Principal.getPrestamosByPeople(boolean)
        val adapter = context?.let { myAdapter(it,cursorPrestamos) }
        if(::listView.isInitialized) {
            listView.adapter = adapter
        }
    }
    companion object {
        fun newInstance(): FragmentPrestamoPeople{
            return FragmentPrestamoPeople()
        }
    }

    inner class myAdapter(context: Context, cursor: Cursor) : CursorAdapter(context, cursor, 0) {
        override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
            return LayoutInflater.from(context).inflate(R.layout.list_prestamo_people, parent, false)
        }

        override fun bindView(view: View, context: Context, cursor: Cursor) {
            // Find fields to populate in inflated template
            val tvFecha = view.findViewById<TextView>(R.id.TVFecha)
            val tvCantidad = view.findViewById<TextView>(R.id.TVCant)
            val tvPersona = view.findViewById<TextView>(R.id.TVPersona)

            // Extract properties from cursor
            val cantidad = cursor.getDouble(cursor.getColumnIndex(DBMan.DBPrestamo.Cantidad))
            val fecha = cursor.getString(cursor.getColumnIndex(DBMan.DBPrestamo.Fecha))
            val persona = cursor.getString(cursor.getColumnIndex(DBMan.DBPersona.Nombre))
            val moneda = cursor.getString(cursor.getColumnIndex(DBMan.DBMoneda.Moneda))

            //set

            tvCantidad.text = "$ ${instance.format(cantidad)} $moneda"
            tvFecha.text = fecha
            tvPersona.text = persona

            if(cantidad < 0){
                tvCantidad.setTextColor(Color.RED)
                tvFecha.setTextColor(Color.RED)
                tvPersona.setTextColor(Color.RED)
            }else{
                tvCantidad.setTextColor(Color.rgb(11, 79, 34))
                tvFecha.setTextColor(Color.rgb(11, 79, 34))
                tvPersona.setTextColor(Color.rgb(11, 79, 34))
            }
        }
    }
}
