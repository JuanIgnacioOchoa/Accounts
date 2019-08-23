package com.lala;

import android.database.Cursor;
import android.os.SystemClock;
import android.util.Log;

import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;

public class DriveMan {
    private static final String APPLICATION_NAME = "Account";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static String mAccessToken;
    private static long mTokenExpired;
    private static Drive drive;
    public DriveMan(Drive s){
        drive = s;
    }
    public static FileList retrieveAllFiles() throws IOException {
        if(drive == null){
            return null;
        }
        FileList files = drive.files().list()
                .setSpaces("appDataFolder")
                .setFields("nextPageToken, files(id, name)")
                .setPageSize(60)
                .execute();
        Log.d("Accoun", "3 " + files.getFiles().size());
        /*for (File file : files.getFiles()) {
            //getDataFileByID(file.getId());
            Log.d("Accoun", "4 " +file.getName() + " " + file.getId());
        }*/
        return files;
    }

    public static String getAccessToken(String authCode, String clientId){

        if (mAccessToken != null && SystemClock.elapsedRealtime() < mTokenExpired) {
            return mAccessToken;
        }
        mTokenExpired = 0;
        mAccessToken = null;
        HttpURLConnection conn = null;
        OutputStream os = null;
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        try {
            final URL url = new URL("https://www.googleapis.com/oauth2/v4/token");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setConnectTimeout(3000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            final StringBuilder b = new StringBuilder();
            b.append("code=").append(authCode).append('&')
                    .append("client_id=").append(clientId).append('&')
                    .append("client_secret=").append("KQ7tZY6JfYSXxWH-FGuvFtdP").append('&')
                    .append("redirect_uri=").append("").append('&')
                    .append("grant_type=").append("authorization_code");

            final byte[] postData = b.toString().getBytes("UTF-8");

            os = conn.getOutputStream();
            os.write(postData);

            final int responseCode = conn.getResponseCode();
            if (200 <= responseCode && responseCode <= 299) {
                is = conn.getInputStream();
                isr = new InputStreamReader(is);
                br = new BufferedReader(isr);
            } else {
                Log.d("Error:", conn.getResponseMessage());
                return null;
            }

            b.setLength(0);
            String output;
            while ((output = br.readLine()) != null) {
                b.append(output);
            }

            final JSONObject jsonResponse = new JSONObject(b.toString());
            mAccessToken = jsonResponse.getString("access_token");
            mTokenExpired = SystemClock.elapsedRealtime() + jsonResponse.getLong("expires_in") * 1000;
            return mAccessToken;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    public static String getFileByName(String name) throws IOException{
        Log.d("Accoun", "getFileByName " + name + " " + drive);
        if(drive == null){
            return null;
        }
        FileList files = drive.files().list()
                .setSpaces("appDataFolder")
                .setFields("nextPageToken, files(id, name)")
                .setQ("name='" + name + "'")
                .setPageSize(15)
                .execute();
        if(files.getFiles().size() <= 0){
            //TODO Create all files in db
            return null;
        } else {
            com.google.api.services.drive.model.File file = files.getFiles().get(0);
            Log.d("Accoun ", "4a " + file.getName());
            return file.getId();
        }
    }

    public static JSONArray getDataFileByID(String id) throws IOException, JSONException {
        if(drive == null){
            return null;
        }
        InputStream response  = drive.files().get(id).executeMediaAsInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(response));

        String line = null;
        String l = null;

        while ((line = br.readLine()) != null) {
            if (line.equalsIgnoreCase("quit")) {
                break;
            }
            //Log.d("Accoun", "7 " + line);
            l = line;
        }
        if(l == null || l == "null"){
            return null;
        }
        return new JSONArray(l);
    }

    public static void deleteAll() throws IOException{
        FileList files = retrieveAllFiles();
        Log.d("Accoun","deleteAll");
        for (File file : files.getFiles()) {
            Log.d("Accoun", "delete " +file.getName());
            drive.files().delete(file.getId()).execute();
        }
        Log.d("Accoun","deleted");
        files = retrieveAllFiles();
        for (File file : files.getFiles()) {
            Log.d("Accoun", "exists " +file.getName());
        }
    }

    public static void createAll() throws IOException, JSONException{
        Cursor c = Principal.getAllTables();
        Log.d("Accoun", "Create All c:" + c.getCount());
        String name = "";
        while(c.moveToNext()){
            name = c.getString(c.getColumnIndex("name"));
            String s = Principal.getTableAsJsonString(name);
            java.io.File tmpFile = java.io.File.createTempFile(name, ".json");
            FileWriter fileWriter = new FileWriter(tmpFile.getAbsoluteFile());
            fileWriter.write(s);
            fileWriter.flush();
            File fileMetadata = new File();
            fileMetadata.setName(name+".json");
            fileMetadata.setParents(Collections.singletonList("appDataFolder"));
            FileContent mediaContent = new FileContent("application/json", tmpFile);
            File file = drive.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .setFields("name")
                    .execute();
            tmpFile.delete();
            Log.d("Accoun", "created: " + file.getName());
        }
    }

    public static void deleteAllAndCreate() throws IOException, JSONException{
        deleteAll();
        createAll();
        retrieveAllFiles();
    }

    public static void downloadFiles() throws IOException, JSONException{
        Log.d("Accoun", "Download files");
        FileList files = retrieveAllFiles();
        for(File file : files.getFiles()){
            JSONArray data = getDataFileByID(file.getId());

            for (int i=0; i < data.length(); i++) {
                JSONObject d = data.getJSONObject(i);
                String keys[] = new String[d.length()];
                String values[] = new String[d.length()];
                int x = 0;
                for (Iterator<String> it = d.keys(); it.hasNext(); ) {
                    String k = it.next();
                    keys[x] = k;
                    values[x] = d.getString(k);
                    x++;
                }
                Principal.insertIntoTable(file.getName().substring(0, file.getName().length() -5), keys, values);
            }
        }
    }
}
