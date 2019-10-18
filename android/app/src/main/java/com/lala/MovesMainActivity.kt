package com.lala

import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SimpleCursorAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import java.text.NumberFormat
import java.util.*

class MovesMainActivity : Fragment() {

    private lateinit var mSectionsPagerAdapter: SectionsPagerAdapter
    private lateinit var mViewPager: ViewPager
    private lateinit var fragmentMoves: Fragment
    private lateinit var analisis: Fragment
    private lateinit var spMonth: Spinner
    private lateinit var spTimeLapse:Spinner
    private lateinit var spMoneda:Spinner
    private var calendar = Calendar.getInstance()
    private val instance: NumberFormat = NumberFormat.getInstance()
    private lateinit var simpleCursorAdapter: SimpleCursorAdapter
    private lateinit var cursorMoneda:Cursor
    private var idMoneda = 1
    private lateinit var year: String
    private var month: String? = null

    companion object {
        fun newInstance(): MovesMainActivity{
            return MovesMainActivity()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        calendar = Calendar.getInstance()
        instance.setMinimumFractionDigits(2)
        cursorMoneda = Principal.getMoneda()
        //val cal = Calendar.getInstance()
        val m = calendar.get(Calendar.MONTH) + 1
        var sm = ""
        if (m < 10) {
            sm = "0$m"
        } else {
            sm = "" + m
        }
        val ys = calendar.get(Calendar.YEAR).toString()

        year = ys
        month = sm

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.activity_moves_main, container, false)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Create the adapter that will return a fragment for each of the three primary sections of the activity.
        calendar = Calendar.getInstance()
        fragmentMoves = FragmentMoves.getInstance()
        analisis = FragmentReportesMotives.getInstance()
        mSectionsPagerAdapter = SectionsPagerAdapter(childFragmentManager)

        // Set up the ViewPager with the sections adapter.
        mViewPager = view.findViewById(R.id.container1) as ViewPager
        mViewPager.adapter = mSectionsPagerAdapter
        mViewPager.offscreenPageLimit = 2

        val tabLayout = view.findViewById(R.id.tabs) as TabLayout
        tabLayout.setTabTextColors(Color.BLACK, resources.getColor(R.color.colorPrimaryDark))
        tabLayout.setSelectedTabIndicatorColor(resources.getColor(R.color.colorPrimaryDark))
        tabLayout.setupWithViewPager(mViewPager)
        val months = arrayOf(getString(R.string.jan), getString(R.string.feb), getString(R.string.mar), getString(R.string.apr), getString(R.string.may), getString(R.string.jun), getString(R.string.jul), getString(R.string.aug), getString(R.string.sep), getString(R.string.oct), getString(R.string.nov), getString(R.string.dec))
        val focus = Principal.getFirstDate()
        val monthsYearList = ArrayList<String>()
        while (focus.get(Calendar.YEAR) != calendar.get(Calendar.YEAR) || focus.get(Calendar.MONTH) != calendar.get(Calendar.MONTH)) {
            monthsYearList.add(months[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.YEAR))
            if (calendar.get(Calendar.MONTH) <= 0) {
                calendar.set(Calendar.MONTH, Calendar.DECEMBER)
                calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 1)
            } else
                calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1)
        }
        monthsYearList.add(months[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.YEAR))
        val monthsYear = monthsYearList.toTypedArray()
        val spAdapteMonth = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, monthsYear)

        spTimeLapse = view.findViewById(R.id.spYear)
        spMonth = view.findViewById(R.id.spMonth)
        spMoneda = view.findViewById(R.id.spMoneda)
        val spAdapterTimeLapse = ArrayAdapter(context!!, android.R.layout.simple_list_item_1,
                arrayOf(getString(R.string.weekly), getString(R.string.monthly), getString(R.string.yearly)))

        val to = intArrayOf(android.R.id.text1)
        val from = arrayOf("Moneda")
        simpleCursorAdapter = SimpleCursorAdapter(context, android.R.layout.simple_list_item_1, cursorMoneda, from, to, 0)

        spMonth.adapter = spAdapteMonth
        spTimeLapse.adapter = spAdapterTimeLapse
        spMoneda.adapter = simpleCursorAdapter
        spMonth.setSelection(0, false)
        spTimeLapse.setSelection(1, false)
        spMoneda.setSelection(0, false)

        idMoneda = cursorMoneda.getInt(cursorMoneda.getColumnIndex("_id"))


        val date = Date()
        val calendar = GregorianCalendar()
        calendar.time = date
        val yearI = calendar.get(Calendar.YEAR)
        val yearArraySize = yearI - 2016
        val years = arrayOfNulls<String>(yearArraySize + 1)
        var i = 0
        var y = yearI
        while (y >= 2016) {
            years[i] = Integer.toString(y)
            i++
            y--
        }
        val spAdapterYear = ArrayAdapter<String>(context!!, android.R.layout.simple_list_item_1, years)

        (analisis as FragmentReportesMotives).updateData(idMoneda, month, year)
        spMoneda.isEnabled = false
        mViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {

            }

            override fun onPageSelected(i: Int) {
                setEnabledSpinner(i == 1)
            }

            override fun onPageScrollStateChanged(i: Int) {

            }
        })
        spMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

                when (spTimeLapse.selectedItemPosition) {
                    0 -> {
                    }
                    1 -> {
                        val s = spAdapteMonth.getItem(position)
                        year = s.substring(4, 8)
                        month = s.substring(0, 3)
                        var x = months[0]
                        var monthCount = 0
                        //Toast.makeText(getApplicationContext(), month, Toast.LENGTH_LONG).show();
                        while (month != x || monthCount >= 12) {
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
                (analisis as FragmentReportesMotives).updateData(idMoneda, month, year)
                (analisis as FragmentReportesMotives).updateAdapter()
                (fragmentMoves as FragmentMoves).updateData(month, year)
                (fragmentMoves as FragmentMoves).updateAdapter()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        spTimeLapse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                when (position) {
                    0 -> {
                    }
                    1 -> spMonth.adapter = spAdapteMonth
                    2 -> spMonth.adapter = spAdapterYear
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        spMoneda.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                idMoneda = id.toInt()
                (analisis as FragmentReportesMotives).updateData(idMoneda, month, year)
                (analisis as FragmentReportesMotives).updateAdapter()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }

    fun setEnabledSpinner(e: Boolean) {
        spMoneda.isEnabled = e
    }
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        private val title = arrayOf("Motives", "Analisis")

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            when (position) {
                0 -> {
                    return fragmentMoves
                }
                else -> {
                    return analisis
                }
            }
        }



        override fun getCount(): Int {
            // Show 2 total pages.
            return 2
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
