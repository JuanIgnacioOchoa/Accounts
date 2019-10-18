package com.lala

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import java.text.NumberFormat

class MainTotals : Fragment() {

    private lateinit var mSectionsPagerAdapter: SectionsPagerAdapter
    private val instance: NumberFormat = NumberFormat.getInstance()
    private lateinit var mViewPager: ViewPager
    private lateinit var fragmentTotals: Fragment
    private lateinit var analisis: Fragment

    companion object {
        fun newInstance(): MainTotals{
            return MainTotals()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance.minimumFractionDigits = 2
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.activity_moves_main, container, false)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mViewPager = view.findViewById(R.id.container1) as ViewPager


        fragmentTotals = FragmentTotals.getInstance()
        analisis = FragmentReportesCuentas.getInstance()
        mSectionsPagerAdapter = SectionsPagerAdapter(childFragmentManager)

        mViewPager.adapter = mSectionsPagerAdapter
        mViewPager.offscreenPageLimit = 2

        mSectionsPagerAdapter = SectionsPagerAdapter(childFragmentManager)

        val tabLayout = view.findViewById(R.id.tabs) as TabLayout
        tabLayout.setTabTextColors(Color.BLACK, resources.getColor(R.color.colorPrimaryDark))
        tabLayout.setSelectedTabIndicatorColor(resources.getColor(R.color.colorPrimaryDark))
        tabLayout.setupWithViewPager(mViewPager)

    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        private val title = arrayOf("Cuentas", "Analisis")

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            when (position) {
                0 -> {
                    return fragmentTotals
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
