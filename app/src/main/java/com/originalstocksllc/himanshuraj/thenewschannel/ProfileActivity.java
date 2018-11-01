package com.originalstocksllc.himanshuraj.thenewschannel;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import io.fabric.sdk.android.Fabric;

public class ProfileActivity extends AppCompatActivity {

    private Button mLogoutButton;
    private FirebaseAuth mAuth;
    private CircleImageView profileImageView;
    private TextView nameText, emailText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();

        final Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics())
                .debuggable(true)
                .build();
        Fabric.with(fabric);

        profileImageView = findViewById(R.id.profileImage);
        mLogoutButton = findViewById(R.id.button);
        nameText = findViewById(R.id.profileName);
        emailText = findViewById(R.id.Email);

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            Picasso.get().load(firebaseUser.getPhotoUrl()).into(profileImageView);
            nameText.setText(firebaseUser.getDisplayName());
            emailText.setText(firebaseUser.getEmail());
        }

        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                if (mAuth.getCurrentUser() == null) {
                    updateUI();
                }
            }
        });

    }

    private void updateUI() {
        Toast.makeText(this, "You've successfully logged out", Toast.LENGTH_SHORT).show();

        Intent accIntent = new Intent(ProfileActivity.this, LoginActivity.class);
        startActivity(accIntent);
        finish();
    }

}
