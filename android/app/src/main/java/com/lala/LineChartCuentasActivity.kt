package com.lala

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.database.Cursor
import android.graphics.Color
import android.graphics.DashPathEffect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import android.widget.AbsListView.*
import android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.Utils
import kotlinx.android.synthetic.main.activity_graphs.*
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class LineChartCuentasActivity : Fragment(), OnChartValueSelectedListener {


    private var graphInstance: Fragment? = null
    private var lineChart:LineChart? = null
    private var instance: NumberFormat = NumberFormat.getInstance()
    private var month:String? = "01"
    private var startMonth:String = "01"
    private var year = "2019"
    private var startYear = "2019"
    private var startDay = 31
    val hashMap = HashMap<Int, String>()
    //val namesMap = HashMap<Int, String>()
    //val valuesMap = HashMap<Int, ArrayList<Entry>>()
    val indexMap = HashMap<String, CuentaData>()
    val arrayList = ArrayList<String>()
    private lateinit var listView:ListView
    private lateinit var adapter: RecipeAdapter
    private lateinit var cTotales:Cursor
    companion object {
        fun newInstance(): LineChartCuentasActivity{
            return LineChartCuentasActivity().getInstance() as LineChartCuentasActivity
        }
    }
    fun getInstance(): Fragment {
        if (graphInstance == null) {
            graphInstance = LineChartCuentasActivity()
        }
        return graphInstance as Fragment
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.activity_line_chart_cuentas, container, false)
        val cal:Calendar = Calendar.getInstance()
        val m = cal.get(Calendar.MONTH) + 1
        var sm = ""
        if (m < 10) {
            sm = "0$m"
        } else {
            sm = "" + m
        }
        val ys = cal.get(Calendar.YEAR).toString()

        startYear = ys
        startMonth = sm
        startDay = cal.get(Calendar.DAY_OF_MONTH)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        instance.minimumFractionDigits = 2
        lineChart = view.findViewById(R.id.chart)
        listView = view.findViewById(R.id.lvGrapsGasto)
        lineChart!!.setTouchEnabled(true)
        lineChart!!.setPinchZoom(true)
        val desc = Description()
        desc.text = ""
        desc.textSize = 20f
        adapter = RecipeAdapter(context!!, ArrayList())
        listView.adapter = adapter
        cTotales = Principal.getTotalesLineGraph()
        var x = 0
        while(cTotales.moveToNext()){
            val id = cTotales.getString(cTotales.getColumnIndex("_id"))
            if(indexMap[id] == null){
                val cantdidad = cTotales.getDouble(cTotales.getColumnIndex(DBMan.DBTotales.CantidadActual))
                val cuenta = cTotales.getString(cTotales.getColumnIndex(DBMan.DBTotales.Cuenta))
                var sel = false
                if(x < 5){
                    sel = true
                }
                indexMap[id] = CuentaData(cuenta, id, sel, ArrayList(), cantdidad, cantdidad,
                        Principal.colors[x % Principal.colors.size], false, 0, 32, cantdidad)
                arrayList.add(id)
                x++
            }
        }

        updateAdapter(month, year)
    }
    fun updateData(month: String?, year: String){
        this.month = month
        this.year = year
    }
    fun updateAdapter(month: String?, year: String) {
        if(lineChart == null){
            return
        }
        //var m:String? = null
        //var d:String? = null
        //var y:String? = null
        var c:Cursor
        cTotales.moveToFirst()
        cTotales.moveToPrevious()
        if (month == null) {
            var mdf = 0
            var tmpYear = year.toInt()
            while(startYear.toInt() != tmpYear){
                tmpYear++
                mdf++
            }
            c = Principal.getTotalsHistoryByMonth("$mdf")
            //var m:String? = null


            //val cTotales = Principal.getTotales(true)

            //c.moveToFirst()
            //var x = 0
            for((key, value ) in indexMap){
                value.entries.clear()
                value.cantidad = value.cantidadActual
                if(year == startYear){
                    value.entries.add(Entry(startMonth.toFloat()+1, value.cantidadActual.toFloat()))
                }
                //value.end = false
                //value.last = lastDay
                //value.first = firstDay
            }
            //c.moveToPrevious()
            while(c .moveToNext()){
                val id = c.getString(c.getColumnIndex("_id"))
                val cantdidad = c.getDouble(c.getColumnIndex("Cantidad"))
                val y = c.getString(c.getColumnIndex("y"))
                val m = c.getString(c.getColumnIndex("mo"))
                if(indexMap[id] == null){
                    continue
                }
                indexMap[id]!!.cantidad = cantdidad + indexMap[id]!!.cantidad
                if(y.toInt() == year.toInt()){
                    indexMap[id]!!.entries.add(Entry(m.toFloat(), indexMap[id]!!.cantidad.toFloat()))
                }
            }

        } else {
            var mdf = 0
            var tmpMonth = month.toInt()
            var tmpYear = year.toInt()
            while(startYear.toInt() != tmpYear || startMonth.toInt() != tmpMonth){
                if(tmpMonth >= 12){
                    tmpMonth = 0
                    tmpYear++
                }
                tmpMonth++
                mdf++
            }
            //mdf++
            c = Principal.getTotalsHistoryByDay("$mdf")



            //c.moveToFirst()

            var firstDay = 32
            var lastDay = 0
            var x = 0
            for((key, value ) in indexMap){
                value.entries.clear()
                value.cantidad = value.cantidadActual
                value.end = false
                value.last = lastDay
                value.first = firstDay
            }
            //c.moveToPrevious()
            while(c .moveToNext()){
                val id = c.getString(c.getColumnIndex("_id"))
                val cantdidad = c.getDouble(c.getColumnIndex("Cantidad"))
                val y = c.getString(c.getColumnIndex("y"))
                val mo = c.getString(c.getColumnIndex("mo"))
                val d = c.getString(c.getColumnIndex("dd"))
                if(indexMap[id] == null){
                    continue
                }
                indexMap[id]!!.cantidad = cantdidad + indexMap[id]!!.cantidad
                if(y.toInt() == year.toInt() && mo.toInt() == month.toInt()){
                    if(firstDay > d.toInt())
                        firstDay = d.toInt()
                    if(lastDay < d.toInt()){
                        lastDay = d.toInt()
                    }
                    if(indexMap[id]!!.first > d.toInt()){
                        indexMap[id]!!.first = d.toInt()
                    }
                    if(!indexMap[id]!!.end){
                        indexMap[id]!!.end = true
                        if(mdf != 0){
                            indexMap[id]!!.entries.add(Entry(lastDay.toFloat()+1, indexMap[id]!!.cantidad.toFloat() - cantdidad.toFloat()))
                        } else {
                            if(d.toInt() == startDay){
                                indexMap[id]!!.entries.add(Entry(lastDay.toFloat() + 1, indexMap[id]!!.cantidadActual.toFloat()))
                            } else {
                                indexMap[id]!!.entries.add(Entry(lastDay.toFloat() + 1, indexMap[id]!!.cantidad.toFloat() - cantdidad.toFloat()))
                            }
                        }

                    }
                    indexMap[id]!!.entries.add(Entry(d.toFloat(), indexMap[id]!!.cantidad.toFloat()))
                }
            }
            for ((key, value) in indexMap){
                if(!value.end){
                    if(mdf != 0){
                        value.entries.add(Entry(lastDay.toFloat() + 1, value.cantidad.toFloat()))
                    } else {
                        value.entries.add(Entry(lastDay.toFloat() + 1, value.cantidad.toFloat()))
                    }

                }
                if(value.first != firstDay){
                    value.entries.add(Entry(firstDay.toFloat(), value.cantidad.toFloat()))
                }
            }

        }



        updateDataSets()

    }

    fun updateDataSets(){
        lineChart!!.invalidate()
        lineChart!!.clear()
        var index = 0
        val dataSets = ArrayList<ILineDataSet>()
        for ((key, value) in indexMap){
            val col = value.color
            val set1 = LineDataSet(value.entries.reversed(), value.cuenta)
            set1.setDrawIcons(false);
            set1.enableDashedLine(10f, 5f, 0f);
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(col);
            set1.setCircleColor(col);
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(true);
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(DashPathEffect(floatArrayOf(10f, 5f), 0f));
            set1.setFormSize(15f);
            set1.setFillColor(col)
            Log.d("Accoun", "$index $key ${value.cuenta}")
            if(value.selected){
                dataSets.add(set1)
                hashMap[index] = key
                index++
            }
        }
        val data = LineData(dataSets)
        lineChart!!.setData(data)
        val mv = CustomMarkerView(context, R.layout.custom_marker_view_layout);
        lineChart!!.marker = mv
        adapter.updateData(arrayList)
        adapter.updateData(arrayList)
    }
    override fun onNothingSelected() {
        //var row = listView.getChildAt(selectedItem - listView.firstVisiblePosition)
        //row?.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        if (h == null)
            return

    }


    data class CuentaData(val cuenta: String, val id: String, var selected: Boolean, val entries:ArrayList<Entry>,
                          val cantidadActual:Double, var cantidad:Double, val color:Int, var end:Boolean, var last:Int, var first:Int, var cantLast:Double)

    inner class RecipeAdapter(private val context: Context,
                              private var dataSource: ArrayList<String>) : BaseAdapter() {

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
            val rowView = inflater.inflate(R.layout.inflate_line_graph_cuentas, parent, false)

            val key = dataSource[position]
            val TVMotivo = rowView.findViewById(R.id.textView6) as TextView
            val TV7 = rowView.findViewById(R.id.textView7) as TextView
            val chkBox = rowView.findViewById<CheckBox>(R.id.colorTV)
            rowView.setOnClickListener {
                indexMap[key]!!.selected = !indexMap[key]!!.selected
                chkBox.isChecked = indexMap[key]!!.selected
            }
            chkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                indexMap[key]!!.selected = isChecked
                updateDataSets()
            }
            //val states = {{android.R.attr.state_checked}; {}}
            TV7.text = "${indexMap[key]!!.cuenta}"
            val states = arrayOf(intArrayOf(android.R.attr.state_checked, 0))
            val col:IntArray = intArrayOf(indexMap[key]!!.color, Principal.colors[1])
            chkBox.buttonTintList = ColorStateList(states, col)
            chkBox.isChecked = indexMap[key]!!.selected
            TVMotivo.setText(indexMap[key]!!.cuenta)

            return rowView
        }

        fun updateData(data: ArrayList<String>){
            dataSource = data
            notifyDataSetChanged()
        }
    }

    inner class CustomMarkerView(context: Context?, layoutResource: Int) : MarkerView(context, layoutResource) {
        private var tvContent: TextView = findViewById(R.id.tvContent)

        override fun refreshContent(e: Entry?, highlight: Highlight?) {
            super.refreshContent(e, highlight)
            if(highlight != null) {
                val cuenta = indexMap[hashMap[highlight.dataSetIndex]]

                tvContent.text = "${highlight.x.toInt()}"
            }
        }

        override fun getX(): Float {
            return -(width.toFloat()/2f)
        }

        override fun getY(): Float {
            return -height.toFloat()
        }
    }
}
