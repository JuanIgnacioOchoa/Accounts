package com.lala;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SeeByMotive extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_by_motive);
        Intent i = this.getIntent();
        int id = i.getIntExtra("id", 0);
        String month = i.getStringExtra("month");
        String year = i.getStringExtra("year");
        this.setTitle(id + " succes");
    }
}
