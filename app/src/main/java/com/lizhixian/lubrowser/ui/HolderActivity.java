package com.lizhixian.lubrowser.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.lizhixian.lubrowser.database.Record;

import java.util.Timer;

public class HolderActivity extends AppCompatActivity {

    private static final int TIMER_SCHEDULE_DEFAULT = 512;

    private Record first;
    private Record second;
    private Timer mTimer;
    private boolean background = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
