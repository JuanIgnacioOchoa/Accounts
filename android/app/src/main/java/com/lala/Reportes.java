package com.lala;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Reportes extends AppCompatActivity{

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private Cursor cursorMoneda;
    private NumberFormat instance;
    private TextView TVGanancia,TVGasto, TVIngreso, TVPorcentaje;
    private Double gasto, ingreso, ganancia, porcentaje;
    private Spinner spMonth, spTimeLapse, spMoneda;
    private Calendar calendar;
    private String year;
    private String month;
    private SimpleCursorAdapter simpleCursorAdapter;
    private Fragment fragmentReportesCuentas, getFragmentReportesMotives;
    private int idMoneda = 1;
    private String[] months ;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reportes);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                //handleOnBackPress();
            }
        });

        MobileAds.initialize(this);
        mAdView = findViewById(R.id.adView);

        fragmentReportesCuentas = FragmentReportesCuentas.getInstance();
        getFragmentReportesMotives = FragmentReportesMotives.getInstance();

        // Create the adapter that will return a fragment for each of the three primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        TVGanancia   = (TextView) findViewById(R.id.CGanancia);
        TVGasto      = (TextView) findViewById(R.id.CGasto);
        TVIngreso    = (TextView) findViewById(R.id.CIngreso);
        TVPorcentaje = (TextView) findViewById(R.id.Porcentaje);
        spTimeLapse       = (Spinner)  findViewById(R.id.spYear);
        spMonth      = (Spinner)  findViewById(R.id.spMonth);
        spMoneda       = (Spinner)  findViewById(R.id.spMoneda);

        months = new String[]{
                getString(R.string.jan),
                getString(R.string.feb),
                getString(R.string.mar),
                getString(R.string.apr),
                getString(R.string.may),
                getString(R.string.jun),
                getString(R.string.jul),
                getString(R.string.aug),
                getString(R.string.sep),
                getString(R.string.oct),
                getString(R.string.nov),
                getString(R.string.dec)};

        instance = NumberFormat.getInstance();
        instance.setMinimumFractionDigits(2);
        calendar = Calendar.getInstance();


        ArrayAdapter<String> spAdapterTimeLapse = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,
                new String[]{
                        getString(R.string.weekly),
                        getString(R.string.monthly),
                        getString(R.string.yearly)
        });
        Calendar focus = Principal.getFirstDate();

        ArrayList<String> monthsYearList = new ArrayList<>();
        while(focus.get(Calendar.YEAR) != calendar.get(Calendar.YEAR) || focus.get(Calendar.MONTH) != calendar.get(Calendar.MONTH)){
            monthsYearList.add(months[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.YEAR));
            if(calendar.get(Calendar.MONTH)<=0){
                calendar.set(Calendar.MONTH,Calendar.DECEMBER);
                calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) -1 );
            }
            else calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) -1);
        }
        monthsYearList.add(months[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.YEAR));
        final String[] monthsYear = monthsYearList.toArray(new String[monthsYearList.size()]);
        final ArrayAdapter<String> spAdapteMonth = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1, monthsYear);
        Date date = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        int yearI = calendar.get(calendar.YEAR);
        int yearArraySize = yearI - 2016;
        String[] years = new String[yearArraySize + 1];
        for(int i = 0, y = yearI; y >= 2016; i++, y--)
        {
            years[i] = Integer.toString(y);
        }
        final ArrayAdapter<String> spAdapterYear= new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1, years);

        cursorMoneda = Principal.getMoneda();
        int[] to = new int[] {android.R.id.text1};
        String[] from = new String[]{"Moneda"};
        simpleCursorAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,cursorMoneda,from,to,0);
        //calendar.get(Calendar.MONTH) + 1;
        //calendar.get(Calendar.YEAR);
        spMonth.setAdapter(spAdapteMonth);
        spTimeLapse.setAdapter(spAdapterTimeLapse);
        spMoneda.setAdapter(simpleCursorAdapter);
        spMonth.setSelection(0);
        spTimeLapse.setSelection(1);

        gasto = Principal.getGastoTotal(idMoneda);
        ingreso = Principal.getIngresoTotal(idMoneda);
        ganancia = ingreso + gasto;

        TVGasto.setText(instance.format(Principal.round(gasto, 2)));
        TVGasto.setTextColor(Color.RED);
        TVIngreso.setText(instance.format(Principal.round(ingreso,2)));
        TVIngreso.setTextColor(Color.rgb(11,79,34));
        TVGanancia.setText(instance.format(Principal.round(ganancia,2)));
        if(ganancia  <= 0){
            porcentaje = (ganancia / -gasto) * 100;
            TVGanancia.setTextColor(Color.RED );
            TVPorcentaje.setTextColor(Color.RED );
        }else{
            porcentaje = (ganancia / ingreso) * 100;
            TVGanancia.setTextColor(Color.rgb(11,79,34));
            TVPorcentaje.setTextColor(Color.rgb(11,79,34));
        }
        TVPorcentaje.setText("     " + instance.format(Principal.round(porcentaje,2)) + "%");

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                setEnabledSpinner(i == 0);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        spMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (spTimeLapse.getSelectedItemPosition()) {
                    case 0:
                        break;
                    case 1:
                        String s = spAdapteMonth.getItem(position);
                        year = s.substring(4,8);
                        month = s.substring(0,3);
                        String x = months[0];
                        int monthCount = 0;
                        Toast.makeText(getApplicationContext(), month, Toast.LENGTH_LONG).show();
                        while(!(month.equals(x)) || monthCount >= 12){
                            monthCount++;
                            x = months[monthCount];
                        }
                        Toast.makeText(getApplicationContext(), " " + monthCount, Toast.LENGTH_LONG).show();
                        switch (monthCount){
                            case 0:
                                month = "01";
                                break;
                            case 1:
                                month = "02";
                                break;
                            case 2:
                                month = "03";
                                break;
                            case 3:
                                month = "04";
                                break;
                            case 4:
                                month = "05";
                                break;
                            case 5:
                                month = "06";
                                break;
                            case 6:
                                month = "07";
                                break;
                            case 7:
                                month = "08";
                                break;
                            case 8:
                                month = "09";
                                break;
                            case 9:
                                month = "10";
                                break;
                            case 10:
                                month = "11";
                                break;
                            case 11:
                                month = "12";
                                break;
                        }
                        gasto    = Principal.round(Principal.getGastoTotalMonthly(idMoneda,month, year),2);
                        ingreso  = Principal.round(Principal.getIngresoTotalMonthly(idMoneda, month, year),2);
                        ganancia = ingreso + gasto;
                        if(ganancia  <= 0){
                            porcentaje = (ganancia / -gasto) * 100;
                            TVGanancia.setTextColor(Color.RED );
                            TVPorcentaje.setTextColor(Color.RED );
                        }else{
                            porcentaje = (ganancia / ingreso) * 100;
                            TVGanancia.setTextColor(Color.rgb(11,79,34));
                            TVPorcentaje.setTextColor(Color.rgb(11,79,34));
                        }
                        TVPorcentaje.setText("     " + instance.format(Principal.round(porcentaje,2)) + "%");
                        TVIngreso.setText(instance.format(ingreso));
                        TVGasto.setText(instance.format(gasto));
                        TVGanancia.setText(instance.format(ganancia));
                        ((FragmentReportesCuentas)fragmentReportesCuentas).updateAdapter(month,year);
                        ((FragmentReportesMotives)getFragmentReportesMotives).updateAdapter(idMoneda, month,year);
                        break;
                    case 2:
                        year = spAdapterYear.getItem(position);
                        month = null;
                        gasto    = Principal.round(Principal.getGastoTotalYearly(idMoneda, year),2);
                        ingreso  = Principal.round(Principal.getIngresoTotalYearly(idMoneda, year),2);
                        ganancia = ingreso + gasto;
                        if(ganancia  <= 0){
                            porcentaje = (ganancia / -gasto) * 100;
                            TVGanancia.setTextColor(Color.RED );
                            TVPorcentaje.setTextColor(Color.RED );
                        }else{
                            porcentaje = (ganancia / ingreso) * 100;
                            TVGanancia.setTextColor(Color.rgb(11,79,34));
                            TVPorcentaje.setTextColor(Color.rgb(11,79,34));
                        }
                        TVPorcentaje.setText("     " + instance.format(Principal.round(porcentaje,2)) + "%");
                        TVIngreso.setText(instance.format(ingreso));
                        TVGasto.setText(instance.format(gasto));
                        TVGanancia.setText(instance.format(ganancia));
                        ((FragmentReportesCuentas)fragmentReportesCuentas).updateAdapter(null,year);
                        ((FragmentReportesMotives)getFragmentReportesMotives).updateAdapter(idMoneda, null,year);
                        break;
                    default:
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spTimeLapse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        break;
                    case 1:
                        spMonth.setAdapter(spAdapteMonth);
                        break;
                    case 2:
                        spMonth.setAdapter(spAdapterYear);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spMoneda.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                idMoneda = (int) id;
                if(month == null){
                    gasto = Principal.round(Principal.getGastoTotalYearly(idMoneda, year), 2);
                    ingreso = Principal.round(Principal.getIngresoTotalYearly(idMoneda, year), 2);
                } else {
                    gasto = Principal.round(Principal.getGastoTotalMonthly(idMoneda, month, year), 2);
                    ingreso = Principal.round(Principal.getIngresoTotalMonthly(idMoneda, month, year), 2);
                }
                ganancia = ingreso + gasto;
                if(ganancia  <= 0){
                    porcentaje = (ganancia / -gasto) * 100;
                    TVGanancia.setTextColor(Color.RED );
                    TVPorcentaje.setTextColor(Color.RED );
                }else{
                    porcentaje = (ganancia / ingreso) * 100;
                    TVGanancia.setTextColor(Color.rgb(11,79,34));
                    TVPorcentaje.setTextColor(Color.rgb(11,79,34));
                }
                TVPorcentaje.setText("     " + instance.format(Principal.round(porcentaje,2)) + "%");
                TVIngreso.setText(instance.format(ingreso));
                TVGasto.setText(instance.format(gasto));
                TVGanancia.setText(instance.format(ganancia));

                ((FragmentReportesMotives)getFragmentReportesMotives).updateAdapter(idMoneda, month,year); // TODO colocar moneda
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    public void setEnabledSpinner(boolean e){
        spMoneda.setEnabled(e);
    }
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private int[] imageResId = {
                R.drawable.cuentas,
                R.drawable.profile_icon
        };

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case 0: {
                    return getFragmentReportesMotives;
                }
                default: {
                    return fragmentReportesCuentas;
                }
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            //Puts an image in the tabs.
            Drawable image = ContextCompat.getDrawable(Reportes.this, imageResId[position]);
            image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
            SpannableString sb = new SpannableString(" ");
            ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BASELINE);
            sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            return sb;
        }

    }
}
