package com.lala


import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.MobileAds
import androidx.viewpager.widget.ViewPager
import android.widget.*
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import androidx.fragment.app.FragmentManager
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList
import android.widget.Spinner


class ReportesFragment : Fragment(), AdapterView.OnItemSelectedListener {


    private lateinit var mSectionsPagerAdapter: SectionsPagerAdapter
    private lateinit var mViewPager: ViewPager
    private lateinit var graphCuentas: LineChartCuentasActivity
    private lateinit var graphsGasto: Graphs
    private lateinit var graphsIngreso: IngresoGraph
    private lateinit var balance: BalanceGraphActivity
    private lateinit var spTimeLapse: Spinner
    private lateinit var spMonth: Spinner
    private lateinit var spMoneda: Spinner
    private lateinit var months: Array<String>
    private lateinit var instance: NumberFormat
    private lateinit var calendar: Calendar
    private lateinit var cursorMoneda: Cursor
    private lateinit var simpleCursorAdapter: SimpleCursorAdapter
    private var init = false
    private var idMoneda: Int = 1
    private lateinit var year: String
    private var month: String? = null
    private lateinit var spAdapteMonth: ArrayAdapter<String>
    private lateinit var spAdapterYear: ArrayAdapter<String>


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
        spMoneda = view.findViewById(R.id.spMoneda);



        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        MobileAds.initialize(context)

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

        cursorMoneda = Principal.getMoneda()
        val to = intArrayOf(android.R.id.text1)
        val from = arrayOf("Moneda")
        simpleCursorAdapter = SimpleCursorAdapter(context, android.R.layout.simple_list_item_1, cursorMoneda, from, to, 0)
        //calendar.get(Calendar.MONTH) + 1;
        //calendar.get(Calendar.YEAR);
        spMonth.setAdapter(spAdapteMonth)
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
        val m = cal.get(Calendar.MONTH) + 1
        var sm = ""
        if (m < 10) {
            sm = "0$m"
        } else {
            sm = "" + m
        }
        val ys = cal.get(Calendar.YEAR).toString()

        year = ys
        month = sm

        graphsGasto.updateData(idMoneda, month, year)
        graphsIngreso.updateData(idMoneda, month, year)
        balance.updateData(idMoneda, month, year)
        graphCuentas.updateData(month, year)

        spMonth.setOnItemSelectedListener(this)
        spTimeLapse.setOnItemSelectedListener(this)
        spMoneda.setOnItemSelectedListener(this)

    }

    fun setEnabledSpinner(e: Boolean) {
        spMoneda.setEnabled(e);
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

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val sp = parent as Spinner
        val i = sp.id
        if (sp.id == spMonth.id) {
            when (spTimeLapse.getSelectedItemPosition()) {
                0 -> {
                }
                1 -> {
                    val s = spAdapteMonth.getItem(position)
                    year = s!!.substring(4, 8)
                    month = s!!.substring(0, 3)
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

                }
                2 -> {
                    year = spAdapterYear.getItem(position)
                    month = null

                }
            }
        } else if (sp.id == spTimeLapse.id) {
            when (position) {
                0 -> {
                }
                1 -> spMonth.setAdapter(spAdapteMonth)
                2 -> spMonth.setAdapter(spAdapterYear)
            }
        } else if (sp.id == spMoneda.id) {
            idMoneda = id.toInt()

        }

        graphsGasto.updateData(idMoneda, month, year) // TODO colocar moneda
        graphsIngreso.updateData(idMoneda, month, year) // TODO colocar moneda
        balance.updateData(idMoneda, month, year) // TODO colocar moneda
        graphCuentas.updateData(month, year) // TODO colocar moneda

        if(cursorMoneda.count > 0) {
            graphsGasto.updateAdapter(idMoneda, month, year)
            graphsIngreso.updateAdapter(idMoneda, month, year)
            balance.updateAdapter(idMoneda, month, year)
            graphCuentas.updateAdapter(month, year)
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
