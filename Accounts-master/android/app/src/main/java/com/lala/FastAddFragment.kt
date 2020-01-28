package com.lala

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.RelativeLayout
import androidx.cursoradapter.widget.CursorAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.NumberFormat

class FastAddFragment : Fragment(), View.OnClickListener{

    private lateinit var cursor: Cursor
    private var button: Button? = null
    private var button1: Button? = null
    private var button2: Button? = null
    private var button3: Button? = null
    private var buttonMore: Button? = null
    private var lvActive = false
    private lateinit var navigationView: BottomNavigationView
    private lateinit var rl: RelativeLayout

    private var instance: NumberFormat = NumberFormat.getInstance()
    var btnArray:Array<Button?> = arrayOfNulls(5)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fast_add, container, false)
    }

    companion object {
        fun newInstance(): FastAddFragment{
            return FastAddFragment()
        }
    }

    override fun onResume() {
        super.onResume()
        cursor = Principal.getTotales(false)
        when(cursor.count){
            0 -> {
                rl = view!!.findViewById(R.id.rl1)
                val btn = view!!.findViewById<Button>(R.id.button1_0)
                btn.setOnClickListener {
                    val i = Intent(context, NewAccount::class.java)
                    startActivity(i)
                }
            }
            1 -> {
                rl = view!!.findViewById(R.id.rl1)
                button = view!!.findViewById(R.id.button1_0)
                btnArray = arrayOf(button as Button)
            }
            2 -> {
                rl = view!!.findViewById(R.id.rl2)
                button = view!!.findViewById(R.id.button2_0)
                button1 = view!!.findViewById(R.id.button2_1)
                btnArray = arrayOf(button as Button, button1 as Button)
            }
            3 -> {
                rl = view!!.findViewById(R.id.rl3)
                button = view!!.findViewById(R.id.button3_0)
                button1 = view!!.findViewById(R.id.button3_1)
                button2 = view!!.findViewById(R.id.button3_2)
                btnArray = arrayOf(button as Button, button1 as Button, button2 as Button)
            }
            4 -> {
                rl = view!!.findViewById(R.id.rl4)
                button = view!!.findViewById(R.id.button4_0)
                button1 = view!!.findViewById(R.id.button4_1)
                button2 = view!!.findViewById(R.id.button4_2)
                button3 = view!!.findViewById(R.id.button4_3)
                buttonMore = view!!.findViewById(R.id.button4_4)
                (buttonMore as Button).visibility = View.INVISIBLE
                btnArray = arrayOf(button as Button, button1 as Button, button2 as Button, button3 as Button)
            }
            else -> {
                rl = view!!.findViewById(R.id.rl4)
                button = view!!.findViewById(R.id.button4_0)
                button1 = view!!.findViewById(R.id.button4_1)
                button2 = view!!.findViewById(R.id.button4_2)
                button3 = view!!.findViewById(R.id.button4_3)
                buttonMore = view!!.findViewById(R.id.button4_4)
                if (!lvActive){
                    (buttonMore as Button).visibility = View.VISIBLE
                }
                buttonMore?.setOnClickListener(this)
                btnArray = arrayOf(button as Button, button1 as Button, button2 as Button, button3 as Button, buttonMore as Button)
            }
        }
        var i = 0

        while (i < cursor.count && i < 4 && btnArray[i] != null){
            cursor.moveToNext()
            btnArray[i]?.setSingleLine(false)
            var cuenta = cursor.getString(cursor.getColumnIndex(DBMan.DBTotales.Cuenta))
            var cantidad = cursor.getString(cursor.getColumnIndex(DBMan.DBTotales.CantidadActual))
            var idCuenta = cursor.getInt(cursor.getColumnIndex("_id"))
            var moneda = cursor.getString(cursor.getColumnIndex(DBMan.DBMoneda.Moneda))
            btnArray[i]?.text = "$cuenta\n$cantidad"
            btnArray[i]?.setOnClickListener(View.OnClickListener {
                val i = Intent(context, Gasto::class.java)
                i.putExtra("Totales", idCuenta)
                i.putExtra("FastAdd", true)
                i.getBooleanExtra("Gasto", true)
                startActivity(i)
            })
            i++
        }
        rl.visibility = View.VISIBLE

    }

    override fun onClick(v: View?) {
        if (v?.id == buttonMore?.id) {
            print("more\n")
            // Create Button Dynamically

            var i = 0
            while (i < btnArray.count() && btnArray[i] != null){
                btnArray[i]?.visibility = View.GONE
                i++
            }
            var a1 = ArrayList<CuentaBtn>()
            var a2 = ArrayList<CuentaBtn>()
            i = 0
            cursor = Principal.getTotales(false)
            while (cursor.moveToNext()){
                val cuenta = cursor.getString(cursor.getColumnIndex(DBMan.DBTotales.Cuenta))
                val cantidad = cursor.getDouble(cursor.getColumnIndex(DBMan.DBTotales.CantidadActual))
                val id = cursor.getInt(cursor.getColumnIndex("_id"))
                if(i % 2 == 0){
                    a1.add(CuentaBtn(cuenta, cantidad, id, false))
                } else {
                    a2.add(CuentaBtn(cuenta, cantidad, id, false))
                }
                i++
            }
            var arrayList: ArrayList<ArrayList<CuentaBtn>> = arrayListOf(a1, a2)
            val lv = view!!.findViewById(R.id.lv_btn_cuentas) as ListView
            val adapter = RecipeAdapter(context!!, arrayList)
            lv.visibility = View.VISIBLE
            lv.adapter = adapter
            lvActive = true
        }
    }
    data class CuentaBtn(val cuenta: String, val cantidad: Double, val id: Int, var scroll:Boolean) {
    }
    inner class RecipeAdapter(private val context: Context,
                              private val dataSource: ArrayList<ArrayList<CuentaBtn>>) : BaseAdapter() {

        private val inflater: LayoutInflater
                = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getCount(): Int {
            return dataSource[0].size
        }

        //2
        override fun getItem(position: Int): Any {
            return dataSource[position]
        }

        //3
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        //4
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            // Get view for row item
            val rowView = inflater.inflate(R.layout.inflate_list_prueba, parent, false)

            val btn1 = rowView.findViewById(R.id.buttonlv1) as Button
            val btn2 = rowView.findViewById(R.id.buttonlv2) as Button

            if(dataSource.count() > 1){
                val a2 = dataSource[1]
                if(position < a2.count()){
                    val cuenta = dataSource[1][position]
                    btn2.text = "${cuenta.cuenta}\n${instance.format(cuenta.cantidad)}"
                    btn2.setOnClickListener(View.OnClickListener {
                        val i = Intent(context, Gasto::class.java)
                        i.putExtra("Totales", cuenta.id)
                        i.getBooleanExtra("Gasto", true)
                        startActivity(i)
                    })
                }
            }
            val cuenta = dataSource[0][position]
            btn1.text = "${cuenta.cuenta}\n${instance.format(cuenta.cantidad)}"
            btn1.setOnClickListener(View.OnClickListener {
                val i = Intent(context, Gasto::class.java)
                i.putExtra("Totales", cuenta.id)
                i.putExtra("Gasto", true)
                startActivity(i)
            })



            if(cuenta.scroll){
                val animation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
                rowView.startAnimation(animation)
            } else {
                if(position==2){
                    val animation = AnimationUtils.loadAnimation(context, R.anim.slide_uo)
                    rowView.startAnimation(animation)
                } else if (position > 2){
                    val animation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
                    rowView.startAnimation(animation)
                }
                cuenta.scroll = true
            }
            return rowView
        }
    }
    inner class MyAdapter(context: Context, cursor: Cursor) : CursorAdapter(context, cursor, 0) {
        override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
            return LayoutInflater.from(context).inflate(R.layout.inflate_list_prueba, parent, false)
        }

        override fun bindView(view: View, context: Context, cursor: Cursor) {
            // Find fields to populate in inflated template
            val btn1 = view.findViewById(R.id.buttonlv1) as Button
            val btn2 = view.findViewById(R.id.buttonlv1) as Button

            // Extract properties from cursor
            val cuenta = cursor.getString(cursor.getColumnIndex(DBMan.DBTotales.Cuenta))
            val cantidad = cursor.getString(cursor.getColumnIndex(DBMan.DBTotales.CantidadActual))
            // Populate fields with extracted properties
            btn1.setSingleLine(false)
            btn2.setSingleLine(false)
            btn1.text = "$cuenta\n$cantidad"
            btn2.text = "$cuenta\n$cantidad"
        }
    }
}
