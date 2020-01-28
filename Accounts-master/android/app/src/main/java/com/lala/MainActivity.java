package com.lala;

import android.Manifest;
import android.accounts.AccountManager;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
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

    private BottomNavigationView navigationView;

    private static Fragment fragmentTotals;
    private static Fragment fragmentMoves;

    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private ImageButton ibSync;
    private boolean driveRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Accoun Lifecycle", "onCreate");
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this);
        mAdView = findViewById(R.id.adView);


        mInterstitialAd = new InterstitialAd(this);
        //PROD ca-app-pub-5443791090032110/3043271632
        //TEST ca-app-pub-3940256099942544/1033173712
        mInterstitialAd.setAdUnitId("ca-app-pub-5443791090032110/3043271632");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                //Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        fragmentTotals = FragmentTotals.getInstance();
        fragmentMoves = FragmentMoves.getInstance();

        ibSync = findViewById(R.id.ibSync);
        verifyStoragePermissions(this);

        // Create the adapter that will return a fragment for each of the three primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        ibSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int res = updateDrive(false);
                switch (res){
                    case 1:
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(getString(R.string.conexion_err));

                        builder.setMessage(getString(R.string.no_wifi));
                        // Set up the buttons
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int res2 = updateDrive(true);
                                if(res2 == 2){
                                    Toast.makeText(getApplicationContext(), getString(R.string.sync_err), Toast.LENGTH_SHORT).show();
                                } else {
                                    //mInterstitialAd.show();
                                    Toast.makeText(getApplicationContext(), getString(R.string.sync_success), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        AlertDialog alertDialog = builder.show();
                        alertDialog.setCanceledOnTouchOutside(false);
                        break;
                    case 2:
                        Toast.makeText(getApplicationContext(), getString(R.string.sync_err), Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        mInterstitialAd.show();
                        Toast.makeText(getApplicationContext(), getString(R.string.sync_success), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
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
                        AlertDialog alertDialog = builder.show();
                        alertDialog.setCanceledOnTouchOutside(false);
                    }
                }


            }
        });

        //this.createAndInitAppDir();
        //DataBase helper = new DataBase(this.getApplicationContext(), AppDir, version);
        //Log.d("Accoun", getCacheDir()+"");
        DataBase helper = new DataBase(this.getApplicationContext(), getCacheDir());
        helper.getWritableDatabase();
        db = helper.getWritableDatabase();

        helper.saveDB(db);
        principal = new Principal(db, this);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null && GoogleSignIn.hasPermissions(account, new Scope(Scopes.DRIVE_APPFOLDER))){
            Toast.makeText(cont, getString(R.string.wel_back) + " " + account.getDisplayName(), Toast.LENGTH_LONG).show();
            signedIn = true;
        } else {
            Toast.makeText(cont, getString(R.string.not_logged), Toast.LENGTH_LONG).show();
            signedIn = false;
        }
        if(signedIn){
            ibSync.setVisibility(View.VISIBLE);
        } else {
            ibSync.setVisibility(View.GONE);
        }
        updateDrive(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Accoun Lifecycle", "onDestroy");
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.d("Accoun Lifecycle", "onStop");
        updateDrive(false);
    }
    @Override
    protected void onStart(){
        super.onStart();
        Log.d("Accoun Lifecycle", "onStart");
    }
    @Override
    protected void onResume(){
        super.onResume();
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        Log.d("Accoun Lifecycle", "onResume");
        Intent i = new Intent(this,prueba.class);
        startActivity(i);
    }
    @Override
    protected void onPause(){
        super.onPause();
        Log.d("Accoun Lifecycle", "onPause");
    }
    @Override
    protected void onRestart(){
        super.onRestart();
        Log.d("Accoun Lifecycle", "onRestart");
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
        if(id == R.id.login){
            if(signedIn){
                signOut();
            } else {
                signIn();
            }
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

        //private int[] imageResId = {
        //        R.drawable.cuentas,
        //        R.drawable.profile_icon
        //};

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
            /*/Puts an image in the tabs.
                Drawable image = ContextCompat.getDrawable(MainActivity.this, imageResId[position]);
                image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
                SpannableString sb = new SpannableString(" ");
                ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BASELINE);
                sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
             */
            return "abcdefgh";
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

    private int updateDrive(boolean usrApprove){
        if((!usrApprove) && Principal.getOnlyWifi() && !Principal.isWifiAvailable(getApplicationContext())){
            Log.d("Accoun", "Only Wifi and no wifi available");
            return 1;
        }
        if(!Principal.haveNetworkConnection(getApplicationContext())){
            Log.d("Accoun", "Can use Mobile data but there is no internet access");
            return 2;
        }
        Log.d("Accoun", "Update drive true");
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null && GoogleSignIn.hasPermissions(account, new Scope(Scopes.DRIVE_APPFOLDER))){
            signedIn = true;
            if(account.getServerAuthCode() == null){
                signIn();
            } else {
                DriveFilesTask task = new DriveFilesTask();
                ((FragmentMoves) fragmentMoves).loading();
                ((FragmentTotals) fragmentTotals).loading();
                if(!driveRunning)
                    task.execute(account.getServerAuthCode());
            }
        } else {
            signedIn = false;
        }
        if(signedIn){
            ibSync.setVisibility(View.VISIBLE);
        } else {
            ibSync.setVisibility(View.GONE);
        }
        return 0;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            ((FragmentMoves) fragmentMoves).loading();
            ((FragmentTotals) fragmentTotals).loading();
            Log.d("Accoun", "-1");
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
            Log.d("Accoun", "1");
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask){
        try {
            GoogleSignInAccount account =  completedTask.getResult(ApiException.class);
            signedIn = true;
            menu.findItem(R.id.login).setTitle(R.string.logout);
            DriveFilesTask task = new DriveFilesTask();
            ((FragmentMoves) fragmentMoves).loading();
            ((FragmentTotals) fragmentTotals).loading();
            if(!driveRunning)
                task.execute(account.getServerAuthCode());
        } catch (ApiException e){
            e.printStackTrace();
            Log.d("Accoun","Api error" + e.getStackTrace());
            Log.d("Accoun","Api error" + e);
            signedIn = false;
            menu.findItem(R.id.login).setTitle(R.string.login);
        }
        if(signedIn){
            ibSync.setVisibility(View.VISIBLE);
        } else {
            ibSync.setVisibility(View.GONE);
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
                      Toast.makeText(cont, getString(R.string.signed_out), Toast.LENGTH_SHORT).show();
                        signedIn = false;
                        menu.findItem(R.id.login).setTitle(R.string.login);
                    }
                });
        if(signedIn){
            ibSync.setVisibility(View.VISIBLE);
        } else {
            ibSync.setVisibility(View.GONE);
        }
    }

    private class DriveFilesTask extends AsyncTask<String, Void, Integer> {
        DriveFilesTask(){
            super();

        }
        @Override
        protected Integer doInBackground(String ... pParams) {
            driveRunning = true;
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
            try{
                new DriveMan(service);
                //DriveMan.deleteAllAndCreate();
                String configId = DriveMan.getFileByName(DBMan.DBConfig.TABLE_NAME);
                Log.d("Account ", " Config Id " + configId);
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
                            } else {
                                return 1;
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
            driveRunning = false;
            MainActivity.updateFragments();
            if(result == 0){
                Toast.makeText(cont, getString(R.string.sync_success), Toast.LENGTH_SHORT).show();
            } else if(result == 1) {
                Log.d("Accoun", "No changes to commit");
            }else {
                //Toast.makeText(cont, "Google Drive connection Error " + result, Toast.LENGTH_SHORT).show();
                signIn();
            }
        }

    }
}

