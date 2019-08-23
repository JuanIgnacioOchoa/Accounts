package com.lala;

import android.Manifest;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.signin.SignIn;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
    private Menu menu;
    private Boolean signedIn = false;
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    int RC_SIGN_IN = 9;
    public final Context cont = this;
    private File AppDir;
    private final int version = 1;
    private Principal principal;
    private SQLiteDatabase db;
    private GoogleSignInClient mGoogleSignInClient;

    private static final String APPLICATION_NAME = "Account";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();


    private static Fragment fragmentTotals;
    private static Fragment fragmentMoves;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fragmentTotals = FragmentTotals.getInstance();
        fragmentMoves = FragmentMoves.getInstance();

        verifyStoragePermissions(this);

        // Create the adapter that will return a fragment for each of the three primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        AccountManager accountManager = AccountManager.get(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
                .requestServerAuthCode(getString(R.string.server_client_id))
                .requestIdToken(getString(R.string.server_client_id))
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
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
                        final CharSequence colors[] = new CharSequence[] {
                                getResources().getString(R.string.outcome),
                                getResources().getString(R.string.income),
                                getResources().getString(R.string.transfer),
                                getResources().getString(R.string.withrawal)
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(cont);
                        builder.setTitle(getResources().getString(R.string.choose_option));
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
    protected void onStart(){
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null && GoogleSignIn.hasPermissions(account, new Scope(Scopes.DRIVE_APPFOLDER))){
            Toast.makeText(cont, "Welcome " + account.getDisplayName(), Toast.LENGTH_LONG).show();
            signedIn = true;
            if(account.getServerAuthCode() == null){
                signIn();
            } else {
                DriveFilesTask task = new DriveFilesTask();
                task.execute(account.getServerAuthCode());
            }
        } else {
            Toast.makeText(cont, "Not signed in already", Toast.LENGTH_LONG).show();
            signedIn = false;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        if(signedIn){
            menu.findItem(R.id.login).setTitle(R.string.logout);
        } else {
            menu.findItem(R.id.login).setTitle(R.string.login);
        }
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
        else if(id == R.id.action_prestamo){
            Intent i = new Intent(this, PrestamoActivity.class);
            startActivity(i);
        }
        else if(id == R.id.login){
            if(signedIn){
                Toast.makeText(cont, "Log out", Toast.LENGTH_LONG).show();
                signOut();
            } else {
                signIn();
            }
        }
        else if(id == R.id.new_moneda){
            AlertDialog.Builder builder = new AlertDialog.Builder(cont);
            builder.setTitle(getString(R.string.currency));
// Set up the input
            final EditText input = new EditText(cont);
// Specify the type of input expected;
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
            builder.setView(input);
// Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(!EditTextError.checkError(input, getString(R.string.required_field))){
                        String moneda = input.getText().toString();
                        if(moneda.length()==3){
                            Principal.guardarMoneda(moneda);
                        }else Toast.makeText(cont,getString(R.string.chars_currency),Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
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
//TODO change
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
                    return fragmentTotals;
                }
                default: {
                    return fragmentMoves;
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
    public static void updateFragments(){
        ((FragmentMoves) fragmentMoves).updateAdapter();
        ((FragmentTotals) fragmentTotals).updateAdapter();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
            Log.d("Accoun", "1");
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask){
        try {
            GoogleSignInAccount account =  completedTask.getResult(ApiException.class);
            Toast.makeText(cont, "Welcome Back " + account.getDisplayName(), Toast.LENGTH_LONG).show();
            signedIn = true;
            menu.findItem(R.id.login).setTitle(R.string.logout);
            DriveFilesTask task = new DriveFilesTask();
            task.execute(account.getServerAuthCode());
        } catch (ApiException e){
            Toast.makeText(cont, "Error on Log in", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            Log.d("Accoun","Api " + e.getStackTrace());
            signedIn = false;
            menu.findItem(R.id.login).setTitle(R.string.login);
        }
    }
    private void signIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    private void signOut(){
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task){
                      Toast.makeText(cont, "Signed out", Toast.LENGTH_SHORT).show();
                        signedIn = false;
                        menu.findItem(R.id.login).setTitle(R.string.login);
                    }
                });
    }

    private class DriveFilesTask extends AsyncTask<String, Void, Integer> {
        DriveFilesTask(){
            super();
        }
        @Override
        protected Integer doInBackground(String ... pParams) {
            Log.d("Accoun", "Do in BackGround " + pParams[0] + " " + pParams[0]);
            String s = DriveMan.getAccessToken(pParams[0], getString(R.string.server_client_id));
            if(s == "" || s == null){
                return 1;
            }
            final NetHttpTransport HTTP_TRANSPORT = new com.google.api.client.http.javanet.NetHttpTransport();

            GoogleCredential googleCredential = new GoogleCredential().setAccessToken(s);

            new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, googleCredential);
            Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, googleCredential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();


            DriveFilesTask task = new DriveFilesTask();
            try{
                new DriveMan(service);
                //DriveMan.deleteAllAndCreate();
                String configId = DriveMan.getFileByName(DBMan.DBConfig.TABLE_NAME+".json");

                if(configId == null || configId == ""){
                    //TODO Create all files
                    Log.d("Account ", "5 Create all files");
                    DriveMan.deleteAllAndCreate();
                }else {
                    Log.d("Accoun", "5 " + configId);
                    JSONArray jsonArray = DriveMan.getDataFileByID(configId);
                    if(jsonArray == null){
                        //TODO data corrupted delete and create again
                        return 678;
                    } else {
                        String driveLastSyn =  jsonArray.getJSONObject(DBMan.DBConfig.LastSync - 1).getString(DBMan.DBConfig.Value);
                        String localLastSync = Principal.getLastSync();
                        Log.d("Accoun Sync", "drive " + driveLastSyn + " local: " +localLastSync);
                        if(localLastSync == null){
                            Principal.deleteTables();
                            DriveMan.downloadFiles();
                        } else {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                            Date parsedDateDrive = dateFormat.parse(driveLastSyn);
                            Date parsedDateLocal = dateFormat.parse(localLastSync);
                            Timestamp timestampDrive = new java.sql.Timestamp(parsedDateDrive.getTime());
                            Timestamp timestampLocal = new java.sql.Timestamp(parsedDateLocal.getTime());
                            Log.d("Accoun", "5a " + timestampDrive + " - " + timestampLocal);
                            if (timestampLocal.before(timestampDrive)) {
                                Principal.deleteTables();
                                DriveMan.downloadFiles();
                            } else if (timestampLocal.after(timestampDrive)) {
                                DriveMan.deleteAllAndCreate();
                            }
                        }
                        //Principal.deleteTables();
                        return 0;
                    }
                }
                return 0;
            } catch (JSONException e){
                //TODO ask permision to delete and create again
                //TODO data corrupted delete and create again
                /*
                try {
                    DriveMan.deleteAllAndCreate();
                } catch (IOException e1){
                    Log.d("Accoun", "6 error1 " + e1);
                    return 2;
                } catch (JSONException e1){
                    Log.d("Accoun", "6 error1 " + e1);
                    return 2;
                }*/
                Log.d("Accoun", "6 " + e);
                return 2;
            } catch (IOException e){
                Log.d("Accoun", "6 error " + e);
                return 3;
            } catch (ParseException e){
                Log.d("Accoun", "6 error " + e);
                return 4;
            }
            //return 1;
        }

        protected void onProgressUpdate() {
        }

        protected void onPostExecute(Integer result) {
            if(result == 0){
                MainActivity.updateFragments();
                Toast.makeText(cont, "Google Drive connection was a succeess", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(cont, "Google Drive connection Error " + result, Toast.LENGTH_SHORT).show();
            }
        }

    }
}

