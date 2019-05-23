package com.lala

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
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
import android.view.*
import android.widget.CheckBox
import android.widget.SimpleCursorAdapter
import android.widget.Spinner
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_prestamo.*

class PrestamoActivity : AppCompatActivity() {
    private lateinit var  mSectionsPagerAdapter: SectionsPagerAdapter
    private lateinit var mViewPager: ViewPager
    private lateinit var fragmentPrestamosPlus:FragmentPrestamosPlus
    private lateinit var fragmentPrestamosMinus: FragmentPrestamosMinus
    private lateinit var fragmentPrestamosPeople:FragmentPrestamoPeople
    private lateinit var cbCeros:CheckBox
    private lateinit var spMoneda: Spinner
    private val cursorMoneda = Principal.getMoneda()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prestamo)
        setSupportActionBar(toolbar)
        title = ("Deudas/Prestamos")
        fragmentPrestamosPlus = FragmentPrestamosPlus.newInstance()
        fragmentPrestamosPeople = FragmentPrestamoPeople.newInstance()
        fragmentPrestamosMinus = FragmentPrestamosMinus.newInstance()
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById<View>(R.id.container) as ViewPager
        mViewPager.adapter = mSectionsPagerAdapter
        mViewPager.offscreenPageLimit = 3

        cbCeros = findViewById(R.id.CBceros)
        spMoneda = findViewById(R.id.spMoneda)

        val adapterMoneda = SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursorMoneda, arrayOf("Moneda"), intArrayOf(android.R.id.text1), 0)
        spMoneda.adapter = adapterMoneda
        val tabLayout = findViewById<View>(R.id.tabs) as TabLayout
        tabLayout.setupWithViewPager(mViewPager)

        fab.setOnClickListener {
            val colors = arrayOf<CharSequence>("Prestar")

            // Initialize a new instance of
            val builder = AlertDialog.Builder(this@PrestamoActivity)

            // Set the alert dialog title
            builder.setTitle("Choose an option")

            builder.setItems(colors) { dialog, which ->
                when (which) {
                    0 -> {
                        val i = Intent(applicationContext, NewPrestamoActivity::class.java)
                        i.putExtra("Prestado", true)
                        startActivity(i)
                    }
                }
                // the user clicked on colors[which]
            }
            // Finally, make the alert dialog using builder
            val dialog: AlertDialog = builder.create()
            // Display the alert dialog on app interface
            dialog.show()
        }
        cbCeros.setOnCheckedChangeListener { buttonView, isChecked ->
            fragmentPrestamosPlus.actualizar(!cbCeros.isChecked)
            fragmentPrestamosPeople.actualizar(!cbCeros.isChecked)
            fragmentPrestamosMinus.actualizar(!cbCeros.isChecked)
        }

    }

    override fun onResume() {
        super.onResume()
        fragmentPrestamosPlus.actualizar(!cbCeros.isChecked)
        fragmentPrestamosPeople.actualizar(!cbCeros.isChecked)
        fragmentPrestamosMinus.actualizar(!cbCeros.isChecked)
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

        private val imageResId = intArrayOf(R.drawable.people2, R.drawable.plus2, R.drawable.minus2)

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            when (position) {
                0 -> return fragmentPrestamosPeople
                1 -> return  fragmentPrestamosPlus
                else -> return fragmentPrestamosMinus
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
