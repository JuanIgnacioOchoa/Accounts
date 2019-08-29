package com.lala

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.CheckBox

class Settings : AppCompatActivity() {

    private lateinit var cbWifi: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        cbWifi = findViewById(R.id.cbWifi)

        cbWifi.isChecked = Principal.getOnlyWifi()

        cbWifi.setOnClickListener {
            Principal.setOnlyWifi(cbWifi.isChecked)
        }

    }
}