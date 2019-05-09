package com.lala

import android.content.Context
import android.database.Cursor
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.widget.CursorAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

class NewPersonasActivity : AppCompatActivity() {
    private lateinit var bAgregar: Button
    private lateinit var adapter: MyAdapter
    private lateinit var etPersona: EditText
    private lateinit var listView: ListView
    private lateinit var c: Cursor
    private lateinit var context: Context
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_personas)
        context = applicationContext
        c = Principal.getPeopleAll()
        bAgregar = findViewById(R.id.new_motivo_bAgregar)
        etPersona = findViewById(R.id.new_motivo_etMotivo)
        listView = findViewById(R.id.new_motive_list)

        bAgregar.setOnClickListener {
            if (!EditTextError.checkError(etPersona)) {
                Principal.insertPersona(etPersona.text.toString())
                c = Principal.getPeopleAll()
                adapter = MyAdapter(context, c)
                listView.adapter = adapter
                etPersona.setText("")
                finish()
            }
        }
        adapter = MyAdapter(context, c)
        listView.adapter = adapter
    }
    inner class MyAdapter(context: Context, cursor: Cursor) : CursorAdapter(context, cursor, 0) {
        override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
            return LayoutInflater.from(context).inflate(R.layout.list_new_motive, parent, false)
        }

        override fun bindView(view: View, context: Context, cursor: Cursor) {
            // Find fields to populate in inflated template
            val cbActivo = view.findViewById(R.id.new_motive_cbMotivo) as CheckBox
            val etPersona = view.findViewById(R.id.new_motive_etMotive) as EditText
            val id = cursor.getInt(cursor.getColumnIndex("_id"))
            cbActivo.setOnClickListener {
                val act: Int
                if (cbActivo.isChecked)
                    act = 1
                else
                    act = 0
                Principal.updateActivePeople(act, id)
                adapter.changeCursor(Principal.getPeopleAll())

                //Toast.makeText(context, "act: "+ act + " _id: " + id,Toast.LENGTH_SHORT).show();
            }
            etPersona.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    val m = etPersona.text.toString()
                    Principal.updateNamePeople(m, id)
                    adapter.changeCursor(Principal.getPeopleAll())
                }
            }
            // Extract properties from cursor
            //int id = 0;
            //id = 0;
            val nombre = cursor.getString(cursor.getColumnIndex(DBMan.DBPersona.Nombre))
            val active = cursor.getInt(cursor.getColumnIndex("Active"))
            // Populate fields with extracted properties
            //Toast.makeText(context,fecha + " aa",Toast.LENGTH_SHORT).show();
            etPersona.setText(nombre)
            cbActivo.isChecked = (active == 1)
        }
    }
}
