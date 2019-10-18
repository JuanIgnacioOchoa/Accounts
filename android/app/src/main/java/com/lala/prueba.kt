package com.lala


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.text.InputType
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.cursoradapter.widget.CursorAdapter
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import org.json.JSONException
import java.io.File
import java.io.IOException
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat


class prueba : AppCompatActivity()/*, View.OnClickListener*/ {

    private var menu: Menu? = null
    private lateinit var navigationView:BottomNavigationView
    private var instance: NumberFormat = NumberFormat.getInstance()
    private lateinit var reportesFragment :ReportesFragment
    private lateinit var movesFragment :Fragment
    private lateinit var cuentasFragment :Fragment
    private var principal: Principal? = null
    private var db: SQLiteDatabase? = null
    private var AppDir: File? = null

    private var mGoogleSignInClient: GoogleSignInClient? = null

    private val APPLICATION_NAME = "Account"
    private val JSON_FACTORY = JacksonFactory.getDefaultInstance()
    private var ibSync: ImageButton? = null
    private var driveRunning = false
    private var signedIn: Boolean = false
    // Storage Permissions
    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    internal var RC_SIGN_IN = 9

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.prueba)
        super.onCreate(savedInstanceState)
        val helper = DataBase(this.applicationContext, cacheDir)
        helper.writableDatabase
        db = helper.writableDatabase

        helper.saveDB(db)
        principal = Principal(db, this)
        instance.minimumFractionDigits = 2
        navigationView = findViewById(R.id.navigation_view)
        reportesFragment = ReportesFragment.newInstance()
        movesFragment = MovesMainActivity.newInstance()
        cuentasFragment = MainTotals.newInstance()
        ibSync = findViewById(R.id.ibSync)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)


        supportActionBar!!.setIcon(R.mipmap.ic_launcher)

        val fastAddFragment = FastAddFragment.newInstance()
        openFragment(fastAddFragment)

        verifyStoragePermissions(this)


        ibSync!!.setOnClickListener(View.OnClickListener {
            val res = updateDrive(false)
            when (res) {
                1 -> {
                    val builder = AlertDialog.Builder(this@prueba)
                    builder.setTitle(getString(R.string.conexion_err))

                    builder.setMessage(getString(R.string.no_wifi))
                    // Set up the buttons
                    builder.setPositiveButton("OK") { dialog, which ->
                        val res2 = updateDrive(true)
                        if (res2 == 2) {
                            Toast.makeText(applicationContext, getString(R.string.sync_err), Toast.LENGTH_SHORT).show()
                        } else {
                            //mInterstitialAd.show();
                            Toast.makeText(applicationContext, getString(R.string.sync_success), Toast.LENGTH_SHORT).show()
                        }
                    }
                    builder.setNegativeButton(getString(R.string.cancel)) { dialog, which -> dialog.cancel() }
                    val alertDialog = builder.show()
                    alertDialog.setCanceledOnTouchOutside(false)
                }
                2 -> Toast.makeText(applicationContext, getString(R.string.sync_err), Toast.LENGTH_SHORT).show()
                else -> {
                    //mInterstitialAd.show()
                    Toast.makeText(applicationContext, getString(R.string.sync_success), Toast.LENGTH_SHORT).show()
                }
            }
        })

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(Scopes.DRIVE_APPFOLDER))
                .requestServerAuthCode(getString(R.string.server_client_id))
                .requestIdToken(getString(R.string.server_client_id))
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)


        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null && GoogleSignIn.hasPermissions(account, Scope(Scopes.DRIVE_APPFOLDER))) {
            Toast.makeText(applicationContext, getString(R.string.wel_back) + " " + account.displayName, Toast.LENGTH_LONG).show()
            signedIn = true
        } else {
            Toast.makeText(applicationContext, getString(R.string.not_logged), Toast.LENGTH_LONG).show()
            signedIn = false
        }
        if (signedIn) {
            ibSync!!.setVisibility(View.VISIBLE)
        } else {
            ibSync!!.setVisibility(View.GONE)
        }
        updateDrive(false)
        navigationView.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_start -> {
                    val fastAddFragment = FastAddFragment.newInstance()
                    openFragment(fastAddFragment)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_movimientos -> {
                    openFragment(movesFragment)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_cuentas -> {
                    openFragment(cuentasFragment)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_reportes -> {
                    openFragment(reportesFragment)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_settings -> {
                    val menuMore = MenuMoreActivity.newInstance()
                    openFragment(menuMore)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
    override fun onResume() {
        super.onResume()
/*
        cursor = Principal.getTotales(false)
        when(cursor.count){
            0 -> {
                rl = findViewById(R.id.rl1)
            }
            1 -> {
                rl = findViewById(R.id.rl1)
                button = findViewById(R.id.button1_0)
                btnArray = arrayOf(button as Button)
            }
            2 -> {
                rl = findViewById(R.id.rl2)
                button = findViewById(R.id.button2_0)
                button1 = findViewById(R.id.button2_1)
                btnArray = arrayOf(button as Button, button1 as Button)
            }
            3 -> {
                rl = findViewById(R.id.rl3)
                button = findViewById(R.id.button3_0)
                button1 = findViewById(R.id.button3_1)
                button2 = findViewById(R.id.button3_2)
                btnArray = arrayOf(button as Button, button1 as Button, button2 as Button)
            }
            4 -> {
                rl = findViewById(R.id.rl4)
                button = findViewById(R.id.button4_0)
                button1 = findViewById(R.id.button4_1)
                button2 = findViewById(R.id.button4_2)
                button3 = findViewById(R.id.button4_3)
                buttonMore = findViewById(R.id.button4_4)
                (buttonMore as Button).visibility = View.INVISIBLE
                btnArray = arrayOf(button as Button, button1 as Button, button2 as Button, button3 as Button)
            }
            else -> {
                rl = findViewById(R.id.rl4)
                button = findViewById(R.id.button4_0)
                button1 = findViewById(R.id.button4_1)
                button2 = findViewById(R.id.button4_2)
                button3 = findViewById(R.id.button4_3)
                buttonMore = findViewById(R.id.button4_4)
                if (!lvActive){
                    (buttonMore as Button).visibility = View.VISIBLE
                }
                buttonMore?.setOnClickListener(this)
                btnArray = arrayOf(button as Button, button1 as Button, button2 as Button, button3 as Button, buttonMore as Button)
            }
        }
        var i = 0

        while (i < cursor.count && i < 4 && btnArray[i] != null){
            cursor.moveToNext()
            btnArray[i]?.setSingleLine(false)
            var cuenta = cursor.getString(cursor.getColumnIndex(DBMan.DBTotales.Cuenta))
            var cantidad = cursor.getString(cursor.getColumnIndex(DBMan.DBTotales.CantidadActual))
            var idCuenta = cursor.getInt(cursor.getColumnIndex("_id"))
            var moneda = cursor.getString(cursor.getColumnIndex(DBMan.DBMoneda.Moneda))
            btnArray[i]?.text = "$cuenta\n$cantidad\n900.00"
            btnArray[i]?.setOnClickListener(View.OnClickListener {
                val i = Intent(applicationContext, Gasto::class.java)
                i.putExtra("Totales", idCuenta)
                i.getBooleanExtra("Gasto", true)
                startActivity(i)
            })
            i++
        }
        rl.visibility = View.VISIBLE

    }

    override fun onClick(v: View?) {
        if (v?.id == buttonMore?.id) {
            print("more\n")
            // Create Button Dynamically

            var i = 0
            while (i < btnArray.count() && btnArray[i] != null){
                btnArray[i]?.visibility = View.GONE
                i++
            }
            var a1 = ArrayList<CuentaBtn>()
            var a2 = ArrayList<CuentaBtn>()
            i = 0
            cursor = Principal.getTotales(false)
            while (cursor.moveToNext()){
                val cuenta = cursor.getString(cursor.getColumnIndex(DBMan.DBTotales.Cuenta))
                val cantidad = cursor.getDouble(cursor.getColumnIndex(DBMan.DBTotales.CantidadActual))
                val id = cursor.getInt(cursor.getColumnIndex("_id"))
                if(i % 2 == 0){
                    a1.add(CuentaBtn(cuenta, cantidad, id, false))
                } else {
                    a2.add(CuentaBtn(cuenta, cantidad, id, false))
                }
                i++
            }
            var arrayList: ArrayList<ArrayList<CuentaBtn>> = arrayListOf(a1, a2)
            val lv = findViewById(R.id.lv_btn_cuentas) as ListView
            val adapter = RecipeAdapter(this, arrayList)
            lv.visibility = View.VISIBLE
            lv.adapter = adapter
            lvActive = true
        }

 */
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        this.menu = menu
        if (signedIn) {
            menu.findItem(R.id.login).setTitle(R.string.logout)
        } else {
            menu.findItem(R.id.login).setTitle(R.string.login)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.login) {
            if (signedIn) {
                signOut()
            } else {
                signIn()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun doesSDCardAccessible(): Boolean {
        try {
            return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

        } catch (e: Exception) {
            // TODO: handle exception
            e.printStackTrace()
        }

        return false
    }
    fun createAndInitAppDir() {
        try {
            if (doesSDCardAccessible()) {
                //TODO change
                AppDir = File(Environment.getExternalStorageDirectory(), "cuentas" + "/")
                if (!AppDir!!.exists()) {
                    AppDir!!.mkdirs()
                }

            }
        } catch (e: Exception) {
            // TODO: handle exception
            e.printStackTrace()
        }

    }

    fun verifyStoragePermissions(activity: Activity) {
        // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            )
        }
    }

    private fun updateDrive(usrApprove: Boolean): Int {
        if (!usrApprove && Principal.getOnlyWifi() && !Principal.isWifiAvailable(applicationContext)) {
            Log.d("Accoun", "Only Wifi and no wifi available")
            return 1
        }
        if (!Principal.haveNetworkConnection(applicationContext)) {
            Log.d("Accoun", "Can use Mobile data but there is no internet access")
            return 2
        }
        Log.d("Accoun", "Update drive true")
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null && GoogleSignIn.hasPermissions(account, Scope(Scopes.DRIVE_APPFOLDER))) {
            signedIn = true
            if (account.serverAuthCode == null) {
                signIn()
            } else {
                val task = DriveFilesTask()
                //(fragmentMoves as FragmentMoves).loading()
                //(fragmentTotals as FragmentTotals).loading()
                if (!driveRunning)
                    task.execute(account.serverAuthCode)
            }
        } else {
            signedIn = false
        }
        if (signedIn) {
            ibSync!!.setVisibility(View.VISIBLE)
        } else {
            ibSync!!.setVisibility(View.GONE)
        }
        return 0
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            //(fragmentMoves as FragmentMoves).loading()
            //(fragmentTotals as FragmentTotals).loading()
            Log.d("Accoun", "-1")
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
            Log.d("Accoun", "1")
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            signedIn = true
            if(menu != null){
                menu!!.findItem(R.id.login).setTitle(R.string.logout)
            }
            val task = DriveFilesTask()
            //(fragmentMoves as FragmentMoves).loading()
            //(fragmentTotals as FragmentTotals).loading()
            if (!driveRunning)
                task.execute(account!!.serverAuthCode)
        } catch (e: ApiException) {
            e.printStackTrace()
            Log.d("Accoun", "Api error" + e.stackTrace)
            signedIn = false
            menu!!.findItem(R.id.login).setTitle(R.string.login)
        }

        if (signedIn) {
            ibSync!!.setVisibility(View.VISIBLE)
        } else {
            ibSync!!.setVisibility(View.GONE)
        }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.getSignInIntent()
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signOut() {
        mGoogleSignInClient!!.signOut()
                .addOnCompleteListener(this) {
                    Toast.makeText(applicationContext, getString(R.string.signed_out), Toast.LENGTH_SHORT).show()
                    signedIn = false
                    menu!!.findItem(R.id.login).setTitle(R.string.login)
                }
        if (signedIn) {
            ibSync!!.setVisibility(View.VISIBLE)
        } else {
            ibSync!!.setVisibility(View.GONE)
        }
    }

    private inner class DriveFilesTask internal constructor() : AsyncTask<String, Void, Int>() {
        override fun doInBackground(vararg pParams: String): Int? {
            driveRunning = true
            Log.d("Accoun", "Do in BackGround " + pParams[0] + " " + pParams[0])
            val s = DriveMan.getAccessToken(pParams[0], getString(R.string.server_client_id))
            if (s === "" || s == null) {
                return 1
            }
            val HTTP_TRANSPORT = com.google.api.client.http.javanet.NetHttpTransport()

            val googleCredential = GoogleCredential().setAccessToken(s)

            Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, googleCredential)
            val service = Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, googleCredential)
                    .setApplicationName(APPLICATION_NAME)
                    .build()
            try {
                DriveMan(service)
                //DriveMan.deleteAllAndCreate();
                val configId = DriveMan.getFileByName(DBMan.DBConfig.TABLE_NAME)
                //Log.d("Account ", " Config Id " + configId!!)
                if (configId == null || configId === "") {
                    //TODO Create all files
                    Log.d("Account ", "5 Create all files")
                    DriveMan.deleteAllAndCreate()
                } else {
                    Log.d("Accoun", "5 $configId")
                    val jsonArray = DriveMan.getDataFileByID(configId)
                    if (jsonArray == null) {
                        //TODO data corrupted delete and create again
                        return 678
                    } else {
                        val driveLastSyn = jsonArray.getJSONObject(DBMan.DBConfig.LastSync - 1).getString(DBMan.DBConfig.Value)
                        val localLastSync = Principal.getLastSync()
                        Log.d("Accoun Sync", "drive $driveLastSyn local: $localLastSync")
                        if (localLastSync == null) {
                            Principal.deleteTables()
                            DriveMan.downloadFiles()
                        } else {
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                            val parsedDateDrive = dateFormat.parse(driveLastSyn)
                            val parsedDateLocal = dateFormat.parse(localLastSync)
                            val timestampDrive = java.sql.Timestamp(parsedDateDrive.time)
                            val timestampLocal = java.sql.Timestamp(parsedDateLocal.time)
                            Log.d("Accoun", "5a $timestampDrive - $timestampLocal")
                            if (timestampLocal.before(timestampDrive)) {
                                Principal.deleteTables()
                                DriveMan.downloadFiles()
                            } else if (timestampLocal.after(timestampDrive)) {
                                DriveMan.deleteAllAndCreate()
                            } else {
                                return 1
                            }
                        }
                        //Principal.deleteTables();
                        return 0
                    }
                }
                return 0
            } catch (e: JSONException) {
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
                Log.d("Accoun", "6 $e")
                return 2
            } catch (e: IOException) {
                Log.d("Accoun", "6 error $e")
                return 3
            } catch (e: ParseException) {
                Log.d("Accoun", "6 error $e")
                return 4
            }

            //return 1;
        }

        protected fun onProgressUpdate() {

        }

        override fun onPostExecute(result: Int?) {
            driveRunning = false
            //prueba.updateFragments()
            if (result == 0) {
                Toast.makeText(applicationContext, getString(R.string.sync_success), Toast.LENGTH_SHORT).show()
            } else if (result == 1) {
                Log.d("Accoun", "No changes to commit")
            } else {
                //Toast.makeText(cont, "Google Drive connection Error " + result, Toast.LENGTH_SHORT).show();
                signIn()
            }
        }

    }

}
