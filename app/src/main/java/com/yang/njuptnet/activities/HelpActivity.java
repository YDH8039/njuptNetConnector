package com.yang.njuptnet.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.yang.njuptnet.R;

import java.util.Objects;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        Objects.requireNonNull(getSupportActionBar()).hide();
    }
}