package com.originalstocksllc.himanshuraj.thenewschannel;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.originalstocksllc.himanshuraj.thenewschannel.Fragments.BusinessFragment;
import com.originalstocksllc.himanshuraj.thenewschannel.Fragments.HealthFragment;
import com.originalstocksllc.himanshuraj.thenewschannel.Fragments.MediaFragment;
import com.originalstocksllc.himanshuraj.thenewschannel.Fragments.SportsFragment;
import com.originalstocksllc.himanshuraj.thenewschannel.Fragments.TechFragment;

import io.fabric.sdk.android.Fabric;

public class CategoriesActivity extends AppCompatActivity {

    private BottomNavigationView mNavFrame;
    private FrameLayout mMainFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        final Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics())
                .debuggable(true)
                .build();
        Fabric.with(fabric);

        initUI();
    }

    private void initUI() {

        mNavFrame = findViewById(R.id.navFrame);
        mMainFrame = findViewById(R.id.mainFrame);

        final BusinessFragment businessFragment = new BusinessFragment();
        final MediaFragment mediaFragment = new MediaFragment();
        final TechFragment techFragment = new TechFragment();
        final HealthFragment healthFragment = new HealthFragment();
        final SportsFragment sportsFragment = new SportsFragment();

        setFragment(businessFragment);


        mNavFrame.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.business:
                        //Toast.makeText(CategoriesActivity.this, "Business", Toast.LENGTH_SHORT).show();
                        setFragment(businessFragment);
                        break;
                    case R.id.media:
                        //Toast.makeText(CategoriesActivity.this, "media", Toast.LENGTH_SHORT).show();
                        setFragment(mediaFragment);
                        break;
                    case R.id.tech:
                        //Toast.makeText(CategoriesActivity.this, "tech", Toast.LENGTH_SHORT).show();
                        setFragment(techFragment);
                        break;
                    case R.id.health:
                        //Toast.makeText(CategoriesActivity.this, "health", Toast.LENGTH_SHORT).show();
                        setFragment(healthFragment);
                        break;
                    case R.id.sports:
                        //Toast.makeText(CategoriesActivity.this, "sports", Toast.LENGTH_SHORT).show();
                        setFragment(sportsFragment);
                        break;

                }
                return true;
            }
        });
    }

    private void setFragment(android.support.v4.app.Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.mainFrame, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
