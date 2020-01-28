package com.lala

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.android.synthetic.main.activity_graphs.*
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList


class BalanceGraphActivity : Fragment(), OnChartValueSelectedListener {

    private var graphInstance: Fragment? = null
    private var pieChart:PieChart? = null
    private lateinit var listView: ListView
    private lateinit var colorsGastoIn: ArrayList<Int>
    private var instance: NumberFormat = NumberFormat.getInstance()
    private var idMoneda = 1
    private var month:String? = "01"
    private var year = "2019"
    private val arrayList = ArrayList<GastoData>()
    private lateinit var adapter:RecipeAdapter
    private var selectedItem = -1
    companion object {
        fun newInstance(): BalanceGraphActivity{
            return BalanceGraphActivity().getInstance() as BalanceGraphActivity
        }
    }
    fun getInstance(): Fragment {
        if (graphInstance == null) {
            graphInstance = BalanceGraphActivity()
        }
        return graphInstance as Fragment
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.activity_graphs, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        colorsGastoIn = arrayListOf(ContextCompat.getColor(context!!, R.color.positive_green), Color.RED)
        instance.minimumFractionDigits = 2
        pieChart = view.findViewById(R.id.piechart)
        listView = view.findViewById(R.id.lvGrapsGasto)
        pieChart!!.setUsePercentValues(true)
        val desc = Description()
        desc.text = ""
        desc.textSize = 20f

        pieChart!!.description = desc
        pieChart!!.holeRadius = 50f
        pieChart!!.setCenterTextSize(20f)
        pieChart!!.transparentCircleRadius = 40f

        pieChart!!.setOnChartValueSelectedListener(this)
        adapter = RecipeAdapter(context!!, arrayList)
        listView.adapter = adapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            if(selectedItem == position){
            } else{
                val highlight = arrayOf(Highlight(position.toFloat(), arrayList[position].por, 0))
                pieChart!!.highlightValues(highlight)
                var row = listView.getChildAt(selectedItem - listView.firstVisiblePosition)
                row?.setBackgroundColor(Color.TRANSPARENT)
                view.setBackgroundColor(Color.rgb(181, 226, 255))
            }
            selectedItem = position
        }
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE)
        updateAdapter(idMoneda, month, year)
    }
    fun updateData(idMoneda: Int, month: String?, year: String){
        this.idMoneda = idMoneda
        this.month = month
        this.year = year
    }
    fun updateAdapter(idMoneda: Int, month: String?, year: String) {
        if(pieChart == null){
            return
        }
        var gasto:Double
        var ingreso:Double
        if (month == null) {
            gasto = Principal.getGastoTotalYearly(idMoneda, year)*-1
            ingreso = Principal.getIngresoTotalYearly(idMoneda, year)
        } else {
            gasto = Principal.round(Principal.getGastoTotalMonthly(idMoneda, month, year), 2) * -1
            ingreso = Principal.round(Principal.getIngresoTotalMonthly(idMoneda, month, year), 2)
        }
        val ganancia = ingreso - gasto
        var porcentaje = 0.0
        if (ganancia <= 0) {
            porcentaje = ganancia / gasto * 100
            pieChart!!.setCenterTextColor(colorsGastoIn[1])
        } else {
            porcentaje = ganancia / ingreso * 100
            pieChart!!.setCenterTextColor(colorsGastoIn[0])
        }

        pieChart!!.centerText = "Ahorro\n${instance.format(Principal.round(ganancia, 2))}\n${instance.format(Principal.round(porcentaje, 2)) + "%"}"

        val value = ArrayList<PieEntry>()
        arrayList.clear()
        arrayList.add(GastoData("Ingreso", ingreso, 0, (ingreso/(gasto+ingreso)).toFloat()))
        arrayList.add(GastoData("Gasto", gasto, 1, (gasto/(ingreso+gasto)).toFloat()))
        value.add(PieEntry(ingreso.toFloat(), "Ingreso"))
        value.add(PieEntry(gasto.toFloat(), "Gasto"))

        val pieDataSet = PieDataSet(value, "")

        val pieData = PieData(pieDataSet)

        pieChart!!.data = pieData

        pieDataSet.colors = colorsGastoIn
        pieDataSet.setDrawValues(false)

        pieChart!!.animateXY(700, 700)

        adapter.updateData(arrayList)
        //val mv = CustomMarkerView(context, R.layout.custom_marker_view_layout);
        //piechart.marker = mv
        //val xs = arrayOf(Highlight(1.0f, 0.111013226f, 0))
        //pieChart!!.highlightValues(xs)
    }

    override fun onNothingSelected() {
        var row = listView.getChildAt(selectedItem - listView.firstVisiblePosition)
        row?.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        if (h == null)
            return
        val id = h!!.x.toInt()
        Log.d("Accoun", "id " + id)

        var scrolling = !(listView.firstVisiblePosition <= id && listView.lastVisiblePosition >= id)
        if(scrolling) {
            listView.setOnScrollListener(object : AbsListView.OnScrollListener {
                override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {

                }

                override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
                    scrolling = true
                    if (scrollState == SCROLL_STATE_IDLE) {
                        var row = listView.getChildAt(selectedItem - listView.firstVisiblePosition)
                        row?.setBackgroundColor(Color.TRANSPARENT)
                        selectedItem = id
                        row = listView.getChildAt(selectedItem - listView.firstVisiblePosition)
                        row?.setBackgroundColor(Color.rgb(181, 226, 255))
                        listView.setOnScrollListener(null)
                    }
                }
            })
        } else {
            var row = listView.getChildAt(selectedItem - listView.firstVisiblePosition)
            row?.setBackgroundColor(Color.TRANSPARENT)
            selectedItem = id
            row = listView.getChildAt(selectedItem - listView.firstVisiblePosition)
            row?.setBackgroundColor(Color.rgb(181, 226, 255))
        }
        listView.smoothScrollToPosition(id)

    }


    data class GastoData(val motivo: String, val gasto: Double, val id: Int, val por:Float)

    inner class RecipeAdapter(private val context: Context,
                              private var dataSource: ArrayList<GastoData>) : BaseAdapter() {

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
            val rowView = inflater.inflate(R.layout.inflate_reportes_motive, parent, false)
            val TVCantidadI = rowView.findViewById(R.id.textView8) as TextView
            val TVCantidadG = rowView.findViewById(R.id.textView7) as TextView
            val TVMotivo = rowView.findViewById(R.id.textView6) as TextView
            TVCantidadG.setTextColor(colorsGastoIn[position%2])
            TVCantidadI.setTextColor(colorsGastoIn[position%2])
            TVMotivo.setTextColor(colorsGastoIn[position%2])
            var gasto = dataSource[position].gasto
            var porcentaje = dataSource[position].por
            if (gasto == null) gasto = 0.0
            gasto = Principal.round(gasto, 2)

            TVCantidadI.text = instance.format(Principal.round((porcentaje*100).toDouble(), 2)) + "%"
            TVCantidadG.text = "$" + instance.format(gasto)
            TVMotivo.setText(dataSource[position].motivo)

            return rowView
        }

        fun updateData(data: ArrayList<GastoData>){
            dataSource = data
            notifyDataSetChanged()
        }
    }

    inner class CustomMarkerView(context: Context?, layoutResource: Int) : MarkerView(context, layoutResource) {
        private var tvContent: TextView = findViewById(R.id.tvContent)

        override fun refreshContent(e: Entry?, highlight: Highlight?) {
            super.refreshContent(e, highlight)
            if(highlight != null) {
                var m = arrayList[highlight!!.x.toInt()].motivo
                tvContent.text = m
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
