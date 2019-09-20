package com.lala;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

public class NewMotive extends AppCompatActivity {
    private Button bAgregar;
    private myAdapter adapter;
    private EditText etMotivo;
    private ListView listView;
    private Cursor c;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_motive);
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
        context = this;
        bAgregar = (Button) findViewById(R.id.new_motivo_bAgregar);
        etMotivo = (EditText) findViewById(R.id.new_motivo_etMotivo);
        listView = (ListView) findViewById(R.id.new_motive_list);

        bAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!EditTextError.checkError(etMotivo, getString(R.string.required_field))) {
                    Principal.insertMotive(etMotivo.getText().toString());
                    c = Principal.getMotiveAll();
                    adapter = new myAdapter(context,c);
                    listView.setAdapter(adapter);
                    etMotivo.setText("");
                }
            }
        });

        c = Principal.getMotiveAll();
        adapter = new myAdapter(context,c);
        listView.setAdapter(adapter);

    }
    public void onDestroy(){
        super.onDestroy();
        //Toast.makeText(context,"Destroy", Toast.LENGTH_SHORT).show();
        /*for(int i = 0; i < c.getCount(); i ++) {
            Cursor x = (Cursor) listView.getItemAtPosition(i);
            int a = x.getInt(x.getColumnIndex("Active"));
            Toast.makeText(context,a + " b",Toast.LENGTH_SHORT).show();
        }*/
    }

    public class myAdapter extends CursorAdapter {

        public myAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.list_new_motive, parent, false);
        }

        @Override
        public void bindView(View view, final Context context, final Cursor cursor) {
            // Find fields to populate in inflated template
            final CheckBox cbActivo = (CheckBox) view.findViewById(R.id.new_motive_cbMotivo);
            final EditText etMotivo = (EditText) view.findViewById(R.id.new_motive_etMotive);
            final int id = cursor.getInt(cursor.getColumnIndex("_id"));
            cbActivo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int act;
                    if(cbActivo.isChecked()) act = 1;
                    else act = 0;
                    Principal.updateActiveMotive(act,id);
                    adapter.changeCursor(Principal.getMotiveAll());

                    //Toast.makeText(context, "act: "+ act + " _id: " + id,Toast.LENGTH_SHORT).show();
                }
            });
            etMotivo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(!hasFocus){
                        String m = etMotivo.getText().toString();
                        Principal.updateNameMotive(m,id);
                        adapter.changeCursor(Principal.getMotiveAll());
                        //Toast.makeText(context, "Motivo: " + m + " _id: " + id,Toast.LENGTH_SHORT).show();
                    }
                }
            });
            // Extract properties from cursor
            //int id = 0;
            //id = 0;
            String motivo = cursor.getString(cursor.getColumnIndex(DBMan.DBMotivo.Motivo));
            int active = cursor.getInt(cursor.getColumnIndex("Active"));
            // Populate fields with extracted properties
            //Toast.makeText(context,fecha + " aa",Toast.LENGTH_SHORT).show();
            etMotivo.setText(motivo);
            if(active==1)cbActivo.setChecked(true);
            else cbActivo.setChecked(false);
        }
    }

}
