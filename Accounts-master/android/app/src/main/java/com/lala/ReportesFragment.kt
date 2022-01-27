package com.lala


import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.ads.MobileAds
import com.google.android.material.tabs.TabLayout
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList


class ReportesFragment : Fragment(), AdapterView.OnItemSelectedListener {


    private lateinit var mSectionsPagerAdapter: SectionsPagerAdapter
    private lateinit var mViewPager: ViewPager
    private lateinit var graphCuentas: LineChartCuentasActivity
    private lateinit var graphsGasto: Graphs
    private lateinit var graphsIngreso: IngresoGraph
    private lateinit var balance: BalanceGraphActivity
    private lateinit var spTimeLapse: Spinner
    private lateinit var spMonth: Spinner
    private lateinit var spYears: Spinner
    private lateinit var spMonths: Spinner
    private lateinit var spMoneda: Spinner
    private lateinit var cbAccount: CheckBox
    private lateinit var cbTrips: CheckBox
    private lateinit var months: Array<String>
    private lateinit var instance: NumberFormat
    private lateinit var calendar: Calendar
    private lateinit var cursorMoneda: Cursor
    private lateinit var simpleCursorAdapter: SimpleCursorAdapter
    private lateinit var reportFilter2: LinearLayout
    private var init = false
    private var idMoneda: Int = 1
    private lateinit var startDate: Date
    private lateinit var endDate: Date
    //private lateinit var year: String
    //private var month: String? = null
    private lateinit var spAdapteMonth: ArrayAdapter<String>
    private lateinit var spAdapterYear: ArrayAdapter<String>
    private lateinit var spAdapterMonths: ArrayAdapter<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        graphsGasto = Graphs.newInstance()
        graphsIngreso = IngresoGraph.newInstance()
        balance = BalanceGraphActivity.newInstance()
        graphCuentas = LineChartCuentasActivity.newInstance()
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.activity_reportes, container, false)

        spTimeLapse = view.findViewById(R.id.spYear);
        spMonth = view.findViewById(R.id.spMonth);
        spYears = view.findViewById(R.id.spYears);
        spMonths = view.findViewById(R.id.spMonths);
        spMoneda = view.findViewById(R.id.spMoneda);
        cbAccount = view.findViewById(R.id.CBAccounts);
        cbTrips = view.findViewById(R.id.CBTrips);
        reportFilter2 = view.findViewById(R.id.reportFilter2)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        MobileAds.initialize(context)

        reportFilter2.visibility = View.GONE
        // Create the adapter that will return a fragment for each of the three primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(childFragmentManager)

        // Set up the ViewPager with the sections adapter.
        mViewPager = view.findViewById(R.id.container) as ViewPager
        mViewPager.adapter = mSectionsPagerAdapter
        mViewPager.offscreenPageLimit = 4

        val tabLayout = view.findViewById(R.id.tabs) as TabLayout
        tabLayout.setTabTextColors(Color.BLACK, resources.getColor(R.color.colorPrimaryDark))
        tabLayout.setSelectedTabIndicatorColor(resources.getColor(R.color.colorPrimaryDark))
        tabLayout.setupWithViewPager(mViewPager)


        months = arrayOf(getString(R.string.jan), getString(R.string.feb), getString(R.string.mar), getString(R.string.apr), getString(R.string.may), getString(R.string.jun), getString(R.string.jul), getString(R.string.aug), getString(R.string.sep), getString(R.string.oct), getString(R.string.nov), getString(R.string.dec))

        instance = NumberFormat.getInstance()
        instance.setMinimumFractionDigits(2)
        calendar = Calendar.getInstance()


        val spAdapterTimeLapse = ArrayAdapter(context, android.R.layout.simple_list_item_1,
                arrayOf(getString(R.string.weekly), getString(R.string.monthly), getString(R.string.yearly)))
        val focus = Principal.getFirstDate()

        val monthsYearList = ArrayList<String>()
        while (focus.get(Calendar.YEAR) !== calendar.get(Calendar.YEAR) || focus.get(Calendar.MONTH) !== calendar.get(Calendar.MONTH)) {
            monthsYearList.add(months[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.YEAR))
            if (calendar.get(Calendar.MONTH) <= 0) {
                calendar.set(Calendar.MONTH, Calendar.DECEMBER)
                calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 1)
            } else
                calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1)
        }
        monthsYearList.add(months[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.YEAR))
        val monthsYear = monthsYearList.toArray(arrayOfNulls<String>(monthsYearList.count()))
        spAdapteMonth = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, monthsYear)
        val date = Date()
        val calendar = GregorianCalendar()
        calendar.setTime(date)
        val yearI = calendar.get(Calendar.YEAR)
        val yearArraySize = yearI - focus.get(Calendar.YEAR)
        val years = arrayOfNulls<String>(yearArraySize + 1)
        var i = 0
        var y = yearI
        while (y >= focus.get(Calendar.YEAR)) {
            years[i] = Integer.toString(y)
            i++
            y--
        }
        spAdapterYear = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, years)
        spAdapterMonths = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, months)

        cursorMoneda = Principal.getMoneda()
        val to = intArrayOf(android.R.id.text1)
        val from = arrayOf("Moneda")
        simpleCursorAdapter = SimpleCursorAdapter(context, android.R.layout.simple_list_item_1, cursorMoneda, from, to, 0)
        //calendar.get(Calendar.MONTH) + 1;
        //calendar.get(Calendar.YEAR);
        spMonth.setAdapter(spAdapteMonth)
        spYears.setAdapter(spAdapterYear)
        spMonths.setAdapter(spAdapterMonths)
        spTimeLapse.setAdapter(spAdapterTimeLapse)
        spMoneda.setAdapter(simpleCursorAdapter)


        mViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {

            }

            override fun onPageSelected(i: Int) {
                setEnabledSpinner(i != 3)
            }

            override fun onPageScrollStateChanged(i: Int) {

            }
        })

        if(cursorMoneda.count <= 0){
            idMoneda = 0
        } else {
            idMoneda = cursorMoneda.getInt(cursorMoneda.getColumnIndex("_id"))
        }


        spMonth.setSelection(0, false)
        spTimeLapse.setSelection(1, false)
        spMoneda.setSelection(0, false)
        if(cursorMoneda.count <= 0){
            idMoneda = 0
        } else {
            idMoneda = cursorMoneda.getInt(cursorMoneda.getColumnIndex("_id"))
        }
        val cal = Calendar.getInstance()
        endDate = cal.time
        cal.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH))
        startDate = cal.time
        update()

        spMonth.setOnItemSelectedListener(this)
        spTimeLapse.setOnItemSelectedListener(this)
        spMoneda.setOnItemSelectedListener(this)
        spYears.setOnItemSelectedListener(this)
        spMonths.setOnItemSelectedListener(this)
        cbAccount.setOnClickListener(View.OnClickListener {
            update()
        })
        cbTrips.setOnClickListener(View.OnClickListener {
            update()
        })
    }

    fun setEnabledSpinner(e: Boolean) {
        spMoneda.setEnabled(e);
        if(e){
            cbAccount.visibility = View.VISIBLE
            cbTrips.visibility = View.VISIBLE
        } else {
            cbAccount.visibility = View.GONE
            cbTrips.visibility = View.GONE
        }
        cbAccount.isEnabled = e
        cbTrips.isEnabled = e
    }

    companion object {
        fun newInstance(): ReportesFragment {
            return ReportesFragment()
        }
    }

    override fun onPause() {
        super.onPause()
        init = false
    }

    fun update(){
        graphsGasto.updateData(idMoneda, startDate, endDate, cbAccount.isChecked, cbTrips.isChecked) // TODO colocar moneda
        graphsIngreso.updateData(idMoneda, startDate, endDate, cbAccount.isChecked, cbTrips.isChecked) // TODO colocar moneda
        balance.updateData(idMoneda, startDate, endDate, cbAccount.isChecked, cbTrips.isChecked) // TODO colocar moneda
        graphCuentas.updateData(startDate, endDate, spTimeLapse.selectedItemPosition) // TODO colocar moneda

        if(cursorMoneda.count > 0) {
            graphsGasto.updateAdapter(idMoneda, startDate, endDate, cbAccount.isChecked, cbTrips.isChecked)
            graphsIngreso.updateAdapter(idMoneda, startDate, endDate, cbAccount.isChecked, cbTrips.isChecked)
            balance.updateAdapter(idMoneda, startDate, endDate, cbAccount.isChecked, cbTrips.isChecked)
            graphCuentas.updateAdapter(startDate, endDate, spTimeLapse.selectedItemPosition)
        }
    }
    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val sp = parent as Spinner
        val i = sp.id
        (parent.getChildAt(0) as TextView).setTextColor(Color.BLACK)
        if (sp.id == spMonth.id) {
            when (spTimeLapse.getSelectedItemPosition()) {
                0 -> {

                    val week = spMonth.selectedItem as WeekDays
                    startDate = week.start
                    endDate = week.end
                }
                1 -> {
                    val s = spAdapteMonth.getItem(position)
                    val year = s!!.substring(4, 8)
                    var month = s!!.substring(0, 3)
                    var x = months[0]
                    var monthCount = 0
                    //Toast.makeText(getApplicationContext(), month, Toast.LENGTH_LONG).show();
                    while (!month.equals(x) || monthCount >= 12) {
                        monthCount++
                        x = months[monthCount]
                    }
                    //Toast.makeText(getApplicationContext(), " " + monthCount, Toast.LENGTH_LONG).show();
                    when (monthCount) {
                        0 -> month = "01"
                        1 -> month = "02"
                        2 -> month = "03"
                        3 -> month = "04"
                        4 -> month = "05"
                        5 -> month = "06"
                        6 -> month = "07"
                        7 -> month = "08"
                        8 -> month = "09"
                        9 -> month = "10"
                        10 -> month = "11"
                        11 -> month = "12"
                    }
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.MONTH, monthCount)
                    calendar.set(Calendar.YEAR, year.toInt())
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH))
                    startDate = calendar.time
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                    endDate = calendar.time
                }
                2 -> {
                    val year = spAdapterYear.getItem(position)
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.MONTH, 0)
                    calendar.set(Calendar.YEAR, year.toInt())
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH))
                    startDate = calendar.time
                    calendar.set(Calendar.MONTH, calendar.getActualMaximum(Calendar.MONTH))
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                    endDate = calendar.time
                }
            }
        } else if (sp.id == spTimeLapse.id) {
            reportFilter2.visibility = View.GONE
            when (position) {
                0 -> {
                    reportFilter2.visibility = View.VISIBLE

                }
                1 -> spMonth.setAdapter(spAdapteMonth)
                2 -> spMonth.setAdapter(spAdapterYear)
            }
        } else if (sp.id == spMoneda.id) {
            idMoneda = id.toInt()

        } else if(sp.id == spYears.id){
            val calendar = Calendar.getInstance()
            var month = 0
            if(spAdapterYear.getItem(position) == calendar.get(Calendar.YEAR).toString()){
                var m = mutableListOf<String>()
                var c = 0
                while(calendar.get(Calendar.MONTH) >= c){
                    m.add(months[c])
                    c++
                }
                spAdapterMonths = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, m)
                spMonths.setAdapter(spAdapterMonths)
            } else {
                spAdapterMonths = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, months)
                spMonths.setAdapter(spAdapterMonths)
            }
            month = spMonths.selectedItemPosition
            spMonth.setAdapter(WeekAdapter(context, getWeekDays(month, spAdapterYear.getItem(position).toInt())))
        } else if(sp.id == spMonths.id){
            val month = spMonths.selectedItemPosition
            val year = (spYears.selectedItem as String).toInt()
            val weekDays = getWeekDays(month, year)
            spMonth.setAdapter(WeekAdapter(context, weekDays))
            val now = Calendar.getInstance().time
            for(i in 0 until weekDays.size){
                val focus = spMonth.getItemAtPosition(i) as WeekDays
                if(now.after(focus.start) && now.before(focus.end)){
                    spMonth.setSelection(i)
                }
            }
        }

        update()
    }

    fun getWeekDays(month: Int, year: Int) : MutableList<WeekDays>{
        var result = mutableListOf<WeekDays>()
        var calendar = Calendar.getInstance()
        var calendar2 = Calendar.getInstance()
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar2.set(Calendar.MONTH, month)
        calendar2.set(Calendar.YEAR, year)
        calendar2.set(Calendar.DAY_OF_MONTH, 1)
        calendar2.add(Calendar.MONTH, 1)
        Log.d("Accoun", calendar.time.toString())
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY){
            calendar.add(Calendar.DATE, -1)
        }
        Log.d("Accoun", calendar.time.toString())
        while(calendar < calendar2){
            val monday = calendar.time
            calendar.add(Calendar.DATE, 6)
            val sunday = calendar.time
            result.add(WeekDays(monday, sunday))

            calendar.add(Calendar.DATE, 1)
        }
        return result;
    }

    inner class WeekDays(startDate: Date, endDate: Date){
        val start: Date
        val end: Date

        init {
            start = startDate
            end = endDate
        }

        fun getLabel():String{
            val calendarS = Calendar.getInstance()
            val calendarE = Calendar.getInstance()
            calendarS.time = this.start
            calendarE.time = this.end
            return (calendarS.get(Calendar.DAY_OF_MONTH).toString() + " - " +
                    calendarE.get(Calendar.DAY_OF_MONTH).toString() + " " +
                    months[calendarE.get(Calendar.MONTH)] + " " + calendarE.get(Calendar.YEAR))
        }
    }

    inner class WeekAdapter(val context: Context?, var dataSource: MutableList<WeekDays>) : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val li = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = li.inflate(android.R.layout.simple_list_item_1, null) //set layout for displaying items

            val txt = view.findViewById<View>(android.R.id.text1) as TextView //get id for Text view

            txt.setText(dataSource[position].getLabel())
            txt.setTextColor(Color.WHITE)
            return view
        }

        override fun getItem(position: Int): Any? {
            return dataSource[position];
        }

        override fun getCount(): Int {
            return dataSource.size;
        }

        override fun getItemId(position: Int): Long {
            return position.toLong();
        }
    }
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        //private val imageResId = intArrayOf(R.drawable.cuentas, R.drawable.profile_icon)
        private val title = arrayOf("Balance", "Gasto", "Ingreso", "Cuentas")



        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            when (position) {
                0 -> {
                    return balance
                }
                1 -> {
                    return graphsGasto
                }
                2 -> {
                    return graphsIngreso
                }
                else -> {
                    return graphCuentas
                }
            }
        }


        override fun getCount(): Int {
            // Show 2 total pages.
            return 4
        }

        override fun getPageTitle(position: Int): CharSequence? {
            //Puts an image in the tabs.

            /*
            val image = ContextCompat.getDrawable(context!!, imageResId[position])
            image!!.setBounds(0, 0, image.intrinsicWidth, image.intrinsicHeight)
            val sb = SpannableString(" ")
            val imageSpan = ImageSpan(image, ImageSpan.ALIGN_BASELINE)
            sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
             */

            return title[position]
        }
    }
}
