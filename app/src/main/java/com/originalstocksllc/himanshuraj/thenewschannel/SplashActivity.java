package com.originalstocksllc.himanshuraj.thenewschannel;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.FusedLocationProviderClient;

import io.fabric.sdk.android.Fabric;
import pl.droidsonroids.gif.GifImageView;

public class SplashActivity extends AppCompatActivity {

    private GifImageView gifImageView;
    private TextView logoText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        setContentView(R.layout.activity_splash);

        final Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics())
                .debuggable(true)
                .build();
        Fabric.with(fabric);

        logoText = findViewById(R.id.textLogo);
        gifImageView = findViewById(R.id.gifImage);

        startActivityMain();


    }//close

    // check if gps enabled or not


    private void startActivityMain() {
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Intent mIntent = new Intent(SplashActivity.this, LoginActivity.class);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mIntent);
            }
        };
        handler.postDelayed(runnable, 4200);
    }

    @Override
    protected void onStop() {
        super.onStop();
        gifImageView.setImageURI(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gifImageView.setImageURI(null);
    }
}
