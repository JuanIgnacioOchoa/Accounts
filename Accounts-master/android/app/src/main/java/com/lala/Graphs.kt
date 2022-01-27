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
import androidx.annotation.ColorInt
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


class Graphs : Fragment(), OnChartValueSelectedListener {

    private var graphInstance: Fragment? = null
    private var pieChart:PieChart? = null
    private lateinit var listView: ListView
    private var instance: NumberFormat = NumberFormat.getInstance()
    private var idMoneda = 1
    //private var month:String? = "01"
    //private var year = "2019"
    private lateinit var startDate: Date
    private lateinit var endDate: Date
    private var account = false
    private var trips = false
    private val arrayList = ArrayList<GastoData>()
    private lateinit var adapter:RecipeAdapter
    private var selectedItem = -1
    private lateinit var labelLayout: LinearLayout
    /*private val coloras: ArrayList<Int> = arrayListOf(Color.parseColor("#e6194B"), Color.parseColor("#3cb44b"), Color.parseColor("#ffe119"),
            Color.parseColor("#4363d8"), Color.parseColor("#f58231"), Color.parseColor("#911eb4"), Color.parseColor("#42d4f4"),
            Color.parseColor("#f032e6"), Color.parseColor("#bfef45"), Color.parseColor("#fabebe"), Color.parseColor("#469990"),
            Color.parseColor("#e6beff"), Color.parseColor("#9A6324"), Color.parseColor("#fffac8"), Color.parseColor("#800000"),
            Color.parseColor("#aaffc3"), Color.parseColor("#808000"), Color.parseColor("#ffd8b1"), Color.parseColor("#000075")
    )*/
    companion object {
        fun newInstance(): Graphs{
            return Graphs().getInstance() as Graphs
        }
    }
    fun getInstance(): Fragment {
        if (graphInstance == null) {
            graphInstance = Graphs()
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
        instance.minimumFractionDigits = 2
        pieChart = view.findViewById(R.id.piechart)
        listView = view.findViewById(R.id.lvGrapsGasto)
        labelLayout = view.findViewById(R.id.label)
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
                val i = Intent(context, SeeByMotive::class.java)
                i.putExtra("_id", arrayList[position].id)
                //i.putExtra("month", month)
                //i.putExtra("year", year)
                i.putExtra("startDate", startDate)
                i.putExtra("endDate", endDate)
                i.putExtra("Gasto", arrayList[position].gasto)
                i.putExtra("Ingreso", arrayList[position].ingreso)
                i.putExtra("IdMoneda", idMoneda)
                startActivity(i)
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
        updateAdapter(idMoneda, startDate, endDate, account, trips)
        //updateAdapter(idMoneda, startDate, endDate, account, trips)
    }

    fun updateData(idMoneda: Int, startDate: Date, endDate: Date, account: Boolean, trips: Boolean){
        this.startDate = startDate
        this.endDate = endDate
        this.idMoneda = idMoneda
        this.account = account
        this.trips = trips
    }
    fun updateData(idMoneda: Int, month: String?, year: String, account: Boolean, trips: Boolean){
        this.idMoneda = idMoneda
        //this.month = month
        //this.year = year
        this.account = account
        this.trips = trips
    }
    fun updateAdapter(idMoneda: Int, startDate: Date, endDate: Date, account: Boolean, trips: Boolean) {
        if(pieChart == null){
            return
        }

        var c = Principal.getTotalesByMotive(idMoneda.toString(), startDate, endDate, account, trips, false, 1)
        var total:Double

        total = Principal.round(Principal.getGastoTotalByDate(idMoneda, startDate, endDate, account, trips), 2)
        pieChart!!.centerText = "Total\n${instance.format(Principal.round(total, 2))}"
        pieChart!!.setCenterTextColor(Color.RED)
        var gasto = 0.0
        var ingreso = 0.0;
        val value = ArrayList<PieEntry>()
        var i = 0
        val inflater = LayoutInflater.from(context);
        labelLayout.removeAllViewsInLayout()
        arrayList.clear()
        while (c.moveToNext()){
            gasto = c.getDouble(c.getColumnIndex("Gasto"))
            ingreso = c.getDouble(c.getColumnIndex("Ingreso"))
            if(c.getInt(c.getColumnIndex("isViaje")) == 1 || gasto == 0.0){
                continue
            }

            val motivo = c.getString(c.getColumnIndex("Motivo"))
            val per:Float = (gasto/total).toFloat()
            arrayList.add(GastoData(motivo, gasto, ingreso, c.getInt(c.getColumnIndex("_id")), per))
            if(per > .025){
                value.add(PieEntry(per, motivo))
            } else {
                value.add(PieEntry(per))
            }
            val rowView = inflater.inflate(R.layout.label_layout, null)
            val tv = rowView.findViewById<TextView>(R.id.label)
            val tvColor = rowView.findViewById<TextView>(R.id.color)
            tvColor.setBackgroundColor(Principal.colors[i % Principal.colors.size])
            tv.text = (motivo)
            labelLayout.addView(rowView)
            i ++
        }
        pieChart!!.centerText = "Total\n${instance.format(Principal.round(total, 2))}"
        val pieDataSet = PieDataSet(value, "Motivos")

        val pieData = PieData(pieDataSet)

        pieChart!!.data = pieData

        pieDataSet.colors = Principal.colors.toList()
        pieDataSet.setDrawValues(false)

        pieChart!!.animateXY(700, 700)

        pieChart!!.rotationAngle = 225.0f
        pieChart!!.getLegend().setEnabled(false);
        adapter.updateData(arrayList)
        val mv = CustomMarkerView(context, R.layout.custom_marker_view_layout);
        pieChart!!.marker = mv
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


    data class GastoData(val motivo: String, val gasto: Double, val ingreso: Double, val id: Int, val por:Float)

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
            val TVColor = rowView.findViewById(R.id.colorTV) as TextView
            TVColor.visibility = View.VISIBLE
            val a = Principal.colors.toList()
            TVCantidadG.setTextColor(Color.RED)
            TVColor.setBackgroundColor(a[position % Principal.colors.size])
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
