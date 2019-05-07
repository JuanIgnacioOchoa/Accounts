package com.lala;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
    private Spinner spMonth, spYear, spMoneda;
    private Calendar calendar;
    private String year;
    private String month;
    private SimpleCursorAdapter simpleCursorAdapter;
    private Fragment fragmentReportesCuentas, getFragmentReportesMotives;
    private int idMoneda = 1;
    private final String[] months = new String[]{"Ene","Feb","Mar", "Abr", "May", "Jun", "Jul", "Ago" ,"Sep", "Oct","Nov", "Dic"};


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reportes);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        spYear       = (Spinner)  findViewById(R.id.spYear);
        spMonth      = (Spinner)  findViewById(R.id.spMonth);
        spMoneda       = (Spinner)  findViewById(R.id.spMoneda);

        instance = NumberFormat.getInstance();
        instance.setMinimumFractionDigits(2);
        calendar = Calendar.getInstance();


        ArrayAdapter<String> spAdapterTimeLapse = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, new String[]{"Weekly", "Monthly", "Yearly"});
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
        String[] monthsYear = monthsYearList.toArray(new String[monthsYearList.size()]);
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
        spYear.setAdapter(spAdapterTimeLapse);
        spMoneda.setAdapter(simpleCursorAdapter);
        spMonth.setSelection(0);
        spYear.setSelection(1);

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

        spMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (spYear.getSelectedItemPosition()) {
                    case 0:
                        break;
                    case 1:
                        String s = spAdapteMonth.getItem(position);
                        year = s.substring(4,8);
                        month = s.substring(0,3);
                        switch (month){
                            case "Ene":
                                month = "01";
                                break;
                            case "Feb":
                                month = "02";
                                break;
                            case "Mar":
                                month = "03";
                                break;
                            case "Abr":
                                month = "04";
                                break;
                            case "May":
                                month = "05";
                                break;
                            case "Jun":
                                month = "06";
                                break;
                            case "Jul":
                                month = "07";
                                break;
                            case "Ago":
                                month = "08";
                                break;
                            case "Sep":
                                month = "09";
                                break;
                            case "Oct":
                                month = "10";
                                break;
                            case "Nov":
                                month = "11";
                                break;
                            case "Dic":
                                month = "12";
                                break;
                        }
                        gasto    = Principal.round(Principal.getGastoTotalMonthly(1,month, year),2);
                        ingreso  = Principal.round(Principal.getIngresoTotalMonthly(1, month, year),2);
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
                        gasto    = Principal.round(Principal.getGastoTotalYearly(1, year),2);
                        ingreso  = Principal.round(Principal.getIngresoTotalYearly(1, year),2);
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

        spYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        break;
                    case 1:
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
                //((FragmentReportesCuentas)fragmentReportesCuentas).updateAdapter(month,year);
                ((FragmentReportesMotives)getFragmentReportesMotives).updateAdapter(idMoneda, month,year); // TODO colocar moneda
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
