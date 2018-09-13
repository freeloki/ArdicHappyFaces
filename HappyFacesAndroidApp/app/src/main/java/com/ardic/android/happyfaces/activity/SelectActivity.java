package com.ardic.android.happyfaces.activity;
import com.ardic.android.happyfaces.R;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class SelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_select);
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SelectActivity.this,MainActivity.class);
        //startActivity(intent);
        SelectActivity.this.startActivity(intent);
    }
}
