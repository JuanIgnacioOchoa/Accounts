package com.lala;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
/*
import com.google.android.gms.common.api.Batch;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;
*/
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public Button BtnVer, BtnGasto, BtnIngreso, BtnMov;
    public final Context cont = this;
    private File AppDir;
    private final int version = 1;
    private Principal principal;
    private SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        verifyStoragePermissions(this);

        // Create the adapter that will return a fragment for each of the three primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

       final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (mViewPager.getCurrentItem()){
                    case 0:{
                        Intent i = new Intent(cont,NewAccount.class);
                        startActivity(i);
                        break;
                    }
                    default:{
                        final CharSequence colors[] = new CharSequence[] {"Gasto", "Ingreso", "Traspaso","Retiro"};

                        AlertDialog.Builder builder = new AlertDialog.Builder(cont);
                        builder.setTitle("Choose an option");
                        builder.setItems(colors, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String s = String.valueOf(which);
                                Toast.makeText(cont,s,Toast.LENGTH_LONG).show();
                                switch (which){
                                    case 0:{
                                        Intent i = new Intent(cont,Gasto.class);
                                        i.putExtra("Gasto", true);
                                        startActivity(i);
                                        break;
                                    }
                                    case 1:{
                                        Intent i = new Intent(cont,Gasto.class);
                                        i.putExtra("Gasto", false);
                                        startActivity(i);
                                        break;
                                    }
                                    case 2:{
                                        Intent i = new Intent(cont,Traspaso.class);
                                        i.putExtra("Retiro",false);
                                        startActivity(i);
                                        break;
                                    }
                                    default:{
                                        Intent i = new Intent(cont,Traspaso.class);
                                        i.putExtra("Retiro",true);
                                        startActivity(i);
                                        break;
                                    }

                                }
                                // the user clicked on colors[which]
                            }
                        });
                        builder.show();
                    }
                }


            }
        });

        this.createAndInitAppDir();
        DataBase helper = new DataBase(this.getApplicationContext(), AppDir, version);
        db = helper.getWritableDatabase();
        helper.saveDB(db);
        principal = new Principal(db, this);

        //ManagerMov managerMov = new ManagerMov(this);
        //ManagerTotal managerTotal = new ManagerTotal(this);
        //MyDate prueba = new MyDate("22-2-2015");



        BtnVer = (Button) findViewById(R.id.MVer);
        BtnGasto = (Button) findViewById(R.id.MGasto);
        BtnIngreso = (Button) findViewById(R.id.MIngreso);
        BtnMov = (Button) findViewById(R.id.MMovimiemto);
        //BtnVer.setOnClickListener(this);
        //BtnGasto.setOnClickListener(this);
        //BtnIngreso.setOnClickListener(this);
        //BtnMov.setOnClickListener(this);

        //upload("temp.db", AppDir, "application/x-sqlite3");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //upload("temp.db", AppDir, "application/x-sqlite3");
    }

    @Override
    protected void onStop(){
        super.onStop();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == R.id.action_motivo){
            Intent i = new Intent(this,NewMotive.class);
            startActivity(i);
        }
        else if(id == R.id.Reportes){
            Intent i = new Intent(this, Reportes.class);
            startActivity(i);
        }
        else if(id == R.id.action_trips){
            Intent i = new Intent(this, TripsMainActivity.class);
            startActivity(i);
        }
        else if(id == R.id.new_moneda){
            AlertDialog.Builder builder = new AlertDialog.Builder(cont);
            builder.setTitle("Moneda");
// Set up the input
            final EditText input = new EditText(cont);
// Specify the type of input expected;
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
            builder.setView(input);
// Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(!EditTextError.checkError(input)){
                        String moneda = input.getText().toString();
                        if(moneda.length()==3){
                            Principal.guardarMoneda(moneda);
                        }else Toast.makeText(cont,"La moneda tiene que ser de tres caracteres",Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }


        return super.onOptionsItemSelected(item);
    }

    public boolean doesSDCardAccessible(){
        try {
            return(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED));

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return false;
    }

    //Step2 : create directory on SD Card
//APP_DIR : your PackageName
    public void createAndInitAppDir(){
        try {
            if(doesSDCardAccessible()){

                AppDir = new File(Environment.getExternalStorageDirectory(),"cuentas"+"/");
                if(!AppDir.exists()){
                    AppDir.mkdirs();
                }

            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
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
                    return FragmentTotals.getInstance();
                }
                default: {
                    return FragmentMoves.getInstance();
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
            Drawable image = ContextCompat.getDrawable(MainActivity.this, imageResId[position]);
            image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
            SpannableString sb = new SpannableString(" ");
            ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BASELINE);
            sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            return sb;
        }

    }
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
    /*
    com.google.android.gms.common.api.GoogleApiClient GAC;
    ///...
    void upload(final String titl, final File file, final String mime) {
        Log.d(Principal.TAG, "Start Uploading " + (GAC != null) + (titl != null) +(file != null));
        if (GAC != null && GAC.isConnected() && titl != null && file != null) try {
            Log.d(Principal.TAG, "IF 1");
            Drive.DriveApi.newDriveContents(GAC).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(@NonNull DriveApi.DriveContentsResult contRslt) {
                    if (contRslt.getStatus().isSuccess()){
                        Log.d(Principal.TAG, "IF 2");
                        DriveContents cont = contRslt.getDriveContents();
                        if (cont != null && file2Os(cont.getOutputStream(), file)) {
                            Log.d(Principal.TAG, "IF 3");
                            MetadataChangeSet meta = new MetadataChangeSet.Builder().setMimeType(mime).build();
                            Drive.DriveApi.getRootFolder(GAC).createFile(GAC, meta, cont).setResultCallback(
                                    new ResultCallback<DriveFolder.DriveFileResult>() {
                                        @Override
                                        public void onResult(@NonNull DriveFolder.DriveFileResult fileRslt) {
                                            if (fileRslt.getStatus().isSuccess()) {
                                                // fileRslt.getDriveFile();   BINGO !!!
                                            }
                                        }
                                    }
                            );
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(Principal.TAG, "Exception, " + e.toString());
        }
    }

    static boolean file2Os(OutputStream os, File file) {
        Log.d(Principal.TAG, "file2os");
        boolean bOK = false;
        InputStream is = null;
        if (file != null && os != null) try {
            byte[] buf = new byte[4096];
            is = new FileInputStream(file);
            int c;
            while ((c = is.read(buf, 0, buf.length)) > 0)
                os.write(buf, 0, c);
            bOK = true;
        } catch (Exception e) {e.printStackTrace();}
        finally {
            try {
                os.flush(); os.close();
                if (is != null )is.close();
            } catch (Exception e) {e.printStackTrace();}
        }
        return  bOK;
    }
    */
}

