package com.ardic.android.happyfaces.activity;
import com.ardic.android.happyfaces.R;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

public class SelectActivity extends AppCompatActivity {
    public final String SETTINGS_FILE="FileEnableSettings";
    public static final String SETTINGS_TENSORFLOW="TensorFlowEnableSettings";
    private SharedPreferences mSharedpreferences;
    private CheckBox mSaveFileCheckBox, mTurnOnTF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_select);
       mSaveFileCheckBox=findViewById(R.id.checkBoxSaveFile);
      
       mTurnOnTF=findViewById(R.id.TurnonTensorflow);
        mSharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);

       // mSharedpreferences.
    }
    @Override
    public void onBackPressed() {
        boolean saveFileMode=mSaveFileCheckBox.isChecked();
        boolean turnOnTf=mTurnOnTF.isChecked();
        SharedPreferences.Editor editor = mSharedpreferences.edit();

        editor.putBoolean(SETTINGS_FILE, saveFileMode);
        editor.putBoolean(SETTINGS_TENSORFLOW, turnOnTf);
        editor.commit();
        Intent intent = new Intent(SelectActivity.this,MainActivity.class);
        //startActivity(intent);
        SelectActivity.this.startActivity(intent);
    }
}
