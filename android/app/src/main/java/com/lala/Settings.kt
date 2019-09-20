package com.lala

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import kotlinx.android.synthetic.main.activity_trips_main.*

class Settings : AppCompatActivity() {

    private lateinit var cbWifi: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)

        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material)

        toolbar.setNavigationOnClickListener(View.OnClickListener {
            finish()
            //handleOnBackPress();
        })

        cbWifi = findViewById(R.id.cbWifi)

        cbWifi.isChecked = Principal.getOnlyWifi()

        cbWifi.setOnClickListener {
            Principal.setOnlyWifi(cbWifi.isChecked)
        }

    }
}