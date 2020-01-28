package com.lala;

import android.widget.EditText;

/**
 * Created by ALEX on 22/11/2015.
 */
public class EditTextError {

    public static boolean checkError(EditText editText, String s){
        editText.setError(null);
        String string = editText.getText().toString();
        string = string.replaceAll("\\s+","");
        if("".equals(string)){
            editText.setError(s);
            return true;
        }
        return false;
    }
}
