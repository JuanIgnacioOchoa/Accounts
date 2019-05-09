package com.lala

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
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


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

class FragmentPrestamosPlus : Fragment() {
    private var instance: NumberFormat = NumberFormat.getInstance()
    private val cursorPrestamos = Principal.getPrestamos()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fragment_prestamos_plus, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listView = view.findViewById<ListView>(R.id.LV_Prestamo)
        instance.minimumFractionDigits = 2
        val adapter = context?.let { myAdapter(it,cursorPrestamos) }

        listView.adapter = adapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val idPres = cursorPrestamos.getInt(cursorPrestamos.getColumnIndex("_id"))
            val i = Intent(context, seePrestamoActivity::class.java)
            i.putExtra("_id", idPres)
            //i.putExtra("_id", cursorPrestamos.getInt(cursorPrestamos.getColumnIndex("_id")))
            //i.putExtra("title", cursorPrestamos.getString(cursorPrestamos.getColumnIndex(DBMan.DBPersona.Nombre)))
            startActivity(i)
        }
    }
    companion object {
        fun newInstance(): FragmentPrestamosPlus{
            return FragmentPrestamosPlus()
        }
    }
    inner class myAdapter(context: Context, cursor: Cursor) : CursorAdapter(context, cursor, 0) {
        override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
            return LayoutInflater.from(context).inflate(R.layout.list_prestamo, parent, false)
        }

        override fun bindView(view: View, context: Context, cursor: Cursor) {
            // Find fields to populate in inflated template
            val tvFecha = view.findViewById<TextView>(R.id.TVFecha)
            val tvCantidad = view.findViewById<TextView>(R.id.TVCant)
            val tvCuenta = view.findViewById<TextView>(R.id.TVCuenta)
            val tvPersona = view.findViewById<TextView>(R.id.TVPersona)

            // Extract properties from cursor
            val cantidad = cursor.getDouble(cursor.getColumnIndex(DBMan.DBPrestamo.Cantidad))
            val fecha = cursor.getString(cursor.getColumnIndex(DBMan.DBPrestamo.Fecha))
            val a = cursor.getInt(cursor.getColumnIndex(DBMan.DBPrestamo.IdPersona))
            val persona = Principal.getNombrePrestamoById(cursor.getInt(cursor.getColumnIndex(DBMan.DBPrestamo.IdPersona)))
            val cuenta = Principal.getCuentaTotales(cursor.getInt(cursor.getColumnIndex(DBMan.DBPrestamo.IdTotales)))
            val moneda = Principal.getIdMoneda(cursor.getInt(cursor.getColumnIndex(DBMan.DBPrestamo.IdMoneda)))

            //set
            tvCantidad.text = "$ ${instance.format(cantidad)} $moneda"
            tvFecha.text = fecha
            tvPersona.text = persona
            tvCuenta.text = cuenta
        }
    }
}
