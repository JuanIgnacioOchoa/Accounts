package com.lala

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_prestamo.*

class PrestamoActivity : AppCompatActivity() {
    private lateinit var  mSectionsPagerAdapter: SectionsPagerAdapter
    private lateinit var mViewPager: ViewPager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prestamo)
        setSupportActionBar(toolbar)
        title = ("Deudas/Prestamos")

        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById<View>(R.id.container) as ViewPager
        mViewPager.adapter = mSectionsPagerAdapter
        mViewPager.offscreenPageLimit = 3

        val tabLayout = findViewById<View>(R.id.tabs) as TabLayout
        tabLayout.setupWithViewPager(mViewPager)

        fab.setOnClickListener { view ->
            when(mViewPager.currentItem){
                0 -> Toast.makeText(applicationContext, "0", Toast.LENGTH_LONG).show()
                1 -> Toast.makeText(applicationContext, "1", Toast.LENGTH_LONG).show()
                else -> Toast.makeText(applicationContext, "2", Toast.LENGTH_LONG).show()
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_prestamo, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if(id == R.id.action_personas){
            val i = Intent(applicationContext, NewPersonasActivity::class.java)
            startActivity(i)
        }
        return super.onOptionsItemSelected(item)
    }
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        private val imageResId = intArrayOf(R.drawable.plus2, R.drawable.minus2, R.drawable.plus_minus2)

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            when (position) {
                0 -> return FragmentPrestamosPlus.newInstance()
                1 -> return  FragmentPrestamosPlus.newInstance()
                else -> return FragmentPrestamosPlus.newInstance()
            }
        }

        override fun getCount(): Int {
            // Show 2 total pages.
            return 3
        }

        override fun getPageTitle(position: Int): CharSequence? {
            //Puts an image in the tabs.
            val image = ContextCompat.getDrawable(this@PrestamoActivity, imageResId[position])
            image!!.setBounds(0, 0, image.intrinsicWidth, image.intrinsicHeight)
            val sb = SpannableString(" ")
            val imageSpan = ImageSpan(image, ImageSpan.ALIGN_BASELINE)
            sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            return sb
        }

    }
}