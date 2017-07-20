package com.example.soundrecorder.activities;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;

import com.example.soundrecorder.R;
import com.example.soundrecorder.adapters.ViewPagerAdapter;
import com.example.soundrecorder.fragments.FileViewerFragment;
import com.example.soundrecorder.fragments.RecordFragment;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "myLogs";

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout)findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void setupViewPager(ViewPager viewPager){

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new RecordFragment(), "Запись");
        adapter.addFragment(new FileViewerFragment(), "Сохраненные");
        viewPager.setAdapter(adapter);
    }
}
