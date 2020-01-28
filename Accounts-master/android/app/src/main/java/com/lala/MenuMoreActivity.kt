package com.lala

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.mynameismidori.currencypicker.CurrencyPicker







class MenuMoreActivity : Fragment(), AdapterView.OnItemClickListener {


    private lateinit var listMenu: ListView
    data class MenuItem(val text: String, val resId: Int)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_menu_more, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listMenu = view.findViewById(R.id.listMenu)
        val arrayList = arrayListOf<MenuItem>(
                MenuItem("Viajes", R.drawable.airplane),
                MenuItem("Deudas/Prestamos", R.drawable.contract),
                MenuItem("Motivos", R.drawable.motives),
                MenuItem("Moneda", R.drawable.currency),
                MenuItem("Configuracion", R.drawable.settings))
        val adapter = MenuAdapter(context!!, arrayList)
        listMenu.adapter = adapter
        listMenu.setOnItemClickListener(this)
    }
    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when(position){
            0 -> {
                val i = Intent(context, TripsMainActivity::class.java)
                startActivity(i)
            }
            1 -> {
                val i = Intent(context, PrestamoActivity::class.java)
                startActivity(i)
            }
            2 -> {
                val i = Intent(context, NewMotive::class.java)
                startActivity(i)
            }
            3 -> {
                val i = Intent(context, CurrencyActivity::class.java)
                startActivity(i)
                /*
                val builder = AlertDialog.Builder(context!!)
                builder.setTitle(getString(R.string.currency))
                // Set up the input
                val input = EditText(context)
                // Specify the type of input expected;
                input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                builder.setView(input)
                // Set up the buttons
                builder.setPositiveButton("OK") { dialog, which ->
                    if (!EditTextError.checkError(input, getString(R.string.required_field))) {
                        val moneda = input.text.toString()
                        if (moneda.length == 3) {
                            Principal.guardarMoneda(moneda)
                        } else
                            Toast.makeText(context, getString(R.string.chars_currency), Toast.LENGTH_SHORT).show()
                    }
                }
                builder.setNegativeButton(getString(R.string.cancel)) { dialog, which -> dialog.cancel() }
                val alertDialog = builder.show()
                alertDialog.setCanceledOnTouchOutside(false)


                val picker = CurrencyPicker.newInstance("Select Currency")  // dialog title
                picker.setListener { name, code, symbol, flagDrawableResID ->
                      val h = code
                }
                picker.show(childFragmentManager, "CURRENCY_PICKER")
                 */
            }
            4 -> {
                val i = Intent(context, Settings::class.java)
                startActivity(i)
            }
        }
    }
    companion object {
        fun newInstance(): MenuMoreActivity{
            return MenuMoreActivity()
        }
    }

    inner class MenuAdapter(private val context: Context,
                              private var dataSource: ArrayList<MenuItem>) : BaseAdapter() {

        private val inflater: LayoutInflater
                = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getCount(): Int {
            return dataSource.size
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
            val rowView = inflater.inflate(R.layout.inflate_list_menu, parent, false)

            val text = rowView.findViewById(R.id.textMenu) as TextView
            val image = rowView.findViewById(R.id.imgMenu) as ImageView

            image.setImageResource(dataSource[position].resId)
            text.setText(dataSource[position].text)

            return rowView
        }
    }
}