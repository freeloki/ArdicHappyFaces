package com.ardic.android.happyfaces.activity;

import com.ardic.android.happyfaces.R;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {
    public static final String SETTINGS_FILE = "FileEnableSettings";
    public static final String SETTINGS_TENSORFLOW = "TensorFlowEnableSettings";
    public static final String SETTINGS_PREF = "SettingsSharedPref";
    public static final String SETTINGS_FRAMES="Frames Settings";
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private CheckBox mSaveFileCheckBox;
    private CheckBox mTurnOnTFCheckBox;
    boolean saveFileMode = false;
    boolean turnOnTf = false;
    private EditText mNumberofFramesEdittext;
    private int mNumberofFrames=-1;
    private TextView mNameTextEditView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        Log.i("TEST", "SettingsActivity onCreate");
        mNumberofFramesEdittext=findViewById(R.id.NumberOfFrameseditText);
        mNameTextEditView=findViewById(R.id.EditTextNameTextView);
        mSaveFileCheckBox = findViewById(R.id.checkBoxSaveFile);
        mSaveFileCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("Test", "Checked Changed For File1:" + isChecked);

                saveFileMode = isChecked;
                saveSharedPreferencesForFile();
                if(saveFileMode){
                    mNumberofFramesEdittext.setVisibility(View.VISIBLE);
                    mNameTextEditView.setVisibility(View.VISIBLE);

                }
                else{
                    mNumberofFramesEdittext.setVisibility(View.INVISIBLE);
                    mNameTextEditView.setVisibility(View.INVISIBLE);
                    mNumberofFrames=-1;
                }

            }
        });
        mTurnOnTFCheckBox = findViewById(R.id.turnOnTensorflow);
        mTurnOnTFCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("Test", "Checked Changed For TF2:" + isChecked);

                turnOnTf = isChecked;
                saveSharedPreferencesForTF();
            }
        });


        mSharedPreferences = getSharedPreferences(SettingsActivity.SETTINGS_PREF, Context.MODE_PRIVATE);



        readSharedPreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("TEST", "SettingsActivity onResume");
        readSharedPreferences();
    }

    @Override
    public void onBackPressed() {
        Log.i("Frames set bnack", mNumberofFrames+ "     "+turnOnTf+"    "+saveFileMode);
        saveSharedPreferencesForFrames();
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void readSharedPreferences() {
        if (mSharedPreferences.contains(SETTINGS_FILE) && mSharedPreferences.contains(SETTINGS_TENSORFLOW)) {
            saveFileMode = mSharedPreferences.getBoolean(SETTINGS_FILE, true);
            mSaveFileCheckBox.setChecked(saveFileMode);
            if(saveFileMode){
                mNumberofFramesEdittext.setVisibility(View.VISIBLE);
                mNameTextEditView.setVisibility(View.VISIBLE);
                mNumberofFrames=mSharedPreferences.getInt(SETTINGS_FRAMES, 1);
            }
            else{
                mNumberofFramesEdittext.setVisibility(View.INVISIBLE);
                mNameTextEditView.setVisibility(View.INVISIBLE);
                mNumberofFrames=-1;
            }
            turnOnTf = mSharedPreferences.getBoolean(SETTINGS_TENSORFLOW, true);
            mTurnOnTFCheckBox.setChecked(turnOnTf);
            Log.i("TEST", "READ TF: " + turnOnTf + " READ FILE: " + saveFileMode);


        }
    }

    private void saveSharedPreferencesForFile() {

        mEditor = mSharedPreferences.edit();
        mEditor.putBoolean(SETTINGS_FILE, saveFileMode);
        Log.i("TEST", "SET TF: " + turnOnTf + " SET FILE: " + saveFileMode);
        mEditor.commit();
    }

    private void saveSharedPreferencesForTF() {

        mEditor = mSharedPreferences.edit();
        mEditor.putBoolean(SETTINGS_TENSORFLOW, turnOnTf);
        Log.i("TEST", "SET TF: " + turnOnTf + " SET FILE: " + saveFileMode);
        mEditor.commit();
    }
    private void saveSharedPreferencesForFrames() {

        mEditor = mSharedPreferences.edit();
        mNumberofFrames=Integer.valueOf(mNumberofFramesEdittext.getText().toString());
        mEditor.putInt(SETTINGS_FRAMES, mNumberofFrames);
        Log.i("TEST", "SET file: " + saveFileMode + " SET Frames: " + mNumberofFrames);
        mEditor.commit();
    }
}
