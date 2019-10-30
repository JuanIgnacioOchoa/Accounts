package com.lala

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cursoradapter.widget.CursorAdapter
import com.github.mikephil.charting.highlight.Highlight
import com.mynameismidori.currencypicker.CurrencyPicker
import com.mynameismidori.currencypicker.ExtendedCurrency
import kotlinx.android.synthetic.main.activity_trips_main.*


class CurrencyActivity : AppCompatActivity() {

    private lateinit var currencylv:ListView
    private lateinit var cursorMoneda: Cursor
    private lateinit var btn:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency)

        setSupportActionBar(toolbar)

        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material)

        toolbar.setNavigationOnClickListener(View.OnClickListener {
            finish()
            //handleOnBackPress();
        })

        currencylv = findViewById(R.id.listCurrencies)
        btn = findViewById(R.id.add_curr)
        cursorMoneda = Principal.getMonedas()
        val adapter = myAdapter(applicationContext, cursorMoneda)
        currencylv.adapter = adapter
        currencylv.divider = null
        currencylv.isEnabled = true
        btn.setOnClickListener(View.OnClickListener {
            val picker = CurrencyPicker.newInstance("Select Currency")  // dialog title
            picker.setListener { name, code, symbol, flagDrawableResID ->
                // Implement your code here
                Principal.guardarMoneda(code)
                cursorMoneda = Principal.getMoneda()
                adapter.changeCursor(cursorMoneda)
                picker.dismiss()
            }

            picker.show(supportFragmentManager, "CURRENCY_PICKER")
        })
    }

    inner class myAdapter(context: Context, cursor: Cursor) : CursorAdapter(context, cursor, 0) {
        override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
            return LayoutInflater.from(context).inflate(R.layout.inflate_list_currencies, parent, false)
        }

        override fun bindView(view: View, context: Context, cursor: Cursor) {
            // Find fields to populate in inflated template
            val ivFlag = view.findViewById<ImageView>(R.id.curr_flag)
            //val ivFlag2 = view.findViewById<ImageView>(R.id.curr_flag2)
            //val ivFlag3 = view.findViewById<ImageView>(R.id.curr_flag3)
            val tvCode = view.findViewById<TextView>(R.id.curr_code)
            val tvName = view.findViewById<TextView>(R.id.curr_name)
            val etCode = view.findViewById<EditText>(R.id.curr_code_et)
            val checkBox = view.findViewById<CheckBox>(R.id.chb_curr)

            // Extract properties from cursor
            val code = cursor.getString(cursor.getColumnIndex(DBMan.DBMoneda.Moneda))
            val active = cursor.getInt(cursor.getColumnIndex(DBMan.DBMoneda.Activo))
            val id = cursor.getInt(cursor.getColumnIndex("_id"))
            val extendedCurrency = ExtendedCurrency.getCurrencyByISO(code)

            //set
            if(extendedCurrency != null) {
                ivFlag.setImageResource(extendedCurrency.flag)
                //ivFlag2.setImageResource(extendedCurrency.flag)
                //ivFlag3.setImageResource(extendedCurrency.flag)
                tvCode.text = code
                tvName.text = extendedCurrency.name
            } else {
                tvCode.text = code
                tvName.text = code
                //etCode.visibility = View.VISIBLE
                //tvCode.visibility = View.GONE
                //etCode.setText(code)
            }
            checkBox.isChecked = (active == 1)
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                Principal.setActiveMoneda(id, checkBox.isChecked)
                if(!checkBox.isChecked){
                    Principal.deleteMoneda(code)
                }
            }
            view.setOnClickListener(View.OnClickListener {
                checkBox.isChecked = !checkBox.isChecked
            })

        }
    }

}
