package com.originalstocksllc.himanshuraj.thenewschannel;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import com.azoft.carousellayoutmanager.CarouselZoomPostLayoutListener;
import com.azoft.carousellayoutmanager.CenterScrollListener;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import io.fabric.sdk.android.Fabric;
import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // NEWS VAR
    private String category = "";
    public static final String API_KEY = "44a3763104b94c76944260fe614a201a";
    static final int REQUEST_LOCATION = 1;
    static final String KEY_TITLE = "title";
    static final String KEY_DESCRIPTION = "description";
    static final String KEY_URL = "url";
    static final String KEY_URLTOIMAGE = "urlToImage";
    static final String KEY_PUBLISHEDAT = "publishedAt";
    private String categoryV = "";
    private ArrayList<HashMap<String, String>> dataListVertical = new ArrayList<HashMap<String, String>>();
    private CircleImageView profileIcon;
    private RecyclerView VerticalRecyclerViews;
    private ProgressBar progressBarV;
    private Toolbar toolbar;
    private FirebaseAuth mAuth;
    private Button weatherRefreshButton, offlineButton, newsRefreshButton;
    private TextView greetingText;

    // weather var
    public static final String prefStoreData = "weatherData";
    private double lati , longi;
    private Geocoder geocoder;
    private List<Address> addresses;
    private String mLocationString;
    private TextView cityField, detailsField, currentTemperatureField, humidity_field, pressure_field, weatherIcon, updatedField;
    private Typeface weatherFont, mCustomFont;
    private Dialog mDialog;

    // Location Providers
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;
    /**
     * Provides the entry point to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Paper.init(this);
        mAuth = FirebaseAuth.getInstance();

        final Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics())
                .debuggable(true)
                .build();
        Fabric.with(fabric);

        initNavDrawer();
        initRecyclerViewVertical();
        initWeatherUI();
        initProfileImage();
        refreshWeatherButtonUI();
        reconnectToNet();
    }


    private void reconnectToNet() {
        offlineButton = findViewById(R.id.offlineButton);
        newsRefreshButton = findViewById(R.id.refreshButtonNews);

        if (!haveNetworkConnection()) {
            offlineButton.setVisibility(View.VISIBLE);
            offlineButton.setEnabled(true);
        }

        final ConnectionDetector connectionDetector = new ConnectionDetector(this);
        offlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                offlineButton.setVisibility(View.INVISIBLE);
                offlineButton.setEnabled(false);
                newsRefreshButton.setVisibility(View.VISIBLE);
                newsRefreshButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (haveNetworkConnection()) {
                            Toast.makeText(MainActivity.this, "Updating content...", Toast.LENGTH_SHORT).show();
                            DownloadNewsForVertical newsTask = new DownloadNewsForVertical();
                            newsTask.execute();
                            refreshWeatherButtonUI();
                            initRecyclerViewVertical();
                            newsRefreshButton.setVisibility(View.INVISIBLE);

                        }
                    }
                });
            }
        });
    }

    private void refreshWeatherButtonUI() {

        weatherRefreshButton = findViewById(R.id.refreshButton);
        weatherRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLastLocation();
                initWeatherUI();
            }
        });
    }

    private void initProfileImage() {

        profileIcon = findViewById(R.id.profileIcon);

        greetingText = findViewById(R.id.greetingsTexts);
        getGreetings(greetingText);

        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            Picasso.get().load(firebaseUser.getPhotoUrl()).into(profileIcon);
        }
        // transitions....not working :(
        final Intent toProfileActivity = new Intent(getApplicationContext(), ProfileActivity.class);
        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(toProfileActivity);
            }
        });

    }

    private void initRecyclerViewVertical() {
        VerticalRecyclerViews = findViewById(R.id.recyclerViewVertical);
        progressBarV = findViewById(R.id.loaderV);

        ArrayList<HashMap<String, String>> arrayList = Paper.book().read("newsList", null);
        VerticalRecyclerAdapter recyclerAdapter = new VerticalRecyclerAdapter(this, arrayList);

        if (!haveNetworkConnection()) {
            progressBarV.setVisibility(View.INVISIBLE);
            VerticalRecyclerViews.setAdapter(recyclerAdapter);
        }

        //calling api
        else if (Function.isNetworkAvailable(getApplicationContext())) {
            DownloadNewsForVertical forVertical = new DownloadNewsForVertical();
            forVertical.execute();
        }
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = connectivityManager.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    private void initWeatherUI() {

        weatherFont = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/weathericons-regular-webfont.ttf");
        mCustomFont = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/RoundedElegance.ttf");

        cityField = findViewById(R.id.City_textView);
        updatedField = findViewById(R.id.updated_textView);
        detailsField = findViewById(R.id.details_textView);
        currentTemperatureField = findViewById(R.id.current_temperature_textView);
        humidity_field = findViewById(R.id.humidity_textView);
        pressure_field = findViewById(R.id.pressure_textView);
        weatherIcon = findViewById(R.id.weather_icon);

        weatherIcon.setTypeface(weatherFont);
        cityField.setTypeface(mCustomFont);
        updatedField.setTypeface(mCustomFont);
        detailsField.setTypeface(mCustomFont);
        currentTemperatureField.setTypeface(mCustomFont);
        humidity_field.setTypeface(mCustomFont);
        pressure_field.setTypeface(mCustomFont);

        getLastLocation();

        final FunctionCurrentWeather.placeIdTask asyncTask;
        asyncTask = new FunctionCurrentWeather.placeIdTask(new FunctionCurrentWeather.AsyncResponse() {
            @Override
            public void processFinish(String weather_city, String weather_description, String weather_temperature, String weather_humidity, String weather_pressure, String weather_updatedOn, String weather_iconText, String sun_rise) {

                cityField.setText(weather_city);
                updatedField.setText(weather_updatedOn);
                detailsField.setText(weather_description);
                currentTemperatureField.setText(weather_temperature);
                humidity_field.setText("Humidity: " + weather_humidity);
                pressure_field.setText("Pressure: " + weather_pressure);
                weatherIcon.setText(Html.fromHtml(weather_iconText));

                //Toast.makeText(MainActivity.this, weather_city + " " + weather_temperature, Toast.LENGTH_LONG).show();

                final SharedPreferences preferencesDataStore = getSharedPreferences(prefStoreData, MODE_PRIVATE);

                SharedPreferences.Editor readWeatherData = preferencesDataStore.edit();
                readWeatherData.putString("mCity", weather_city);
                readWeatherData.putString("mUpdate", weather_updatedOn);
                readWeatherData.putString("mDetails", weather_description);
                readWeatherData.putString("mTemp", weather_temperature);
                readWeatherData.putString("mHumidity", weather_humidity);
                readWeatherData.putString("mPressure", weather_pressure);
                readWeatherData.putString("mIcon", weather_iconText);
                readWeatherData.apply();

            }
        });

        ConnectionDetector connectionDetector = new ConnectionDetector(this);

        if (connectionDetector.isConnected()) {

        } else {

            getLastLocation();

            //Toasty.error(MainActivity.this, "Please Connect to cellular data or WiFi", Toast.LENGTH_LONG, true).show();

            SharedPreferences writeWeatherData = getSharedPreferences(prefStoreData, MODE_PRIVATE);
            String sCity = writeWeatherData.getString("mCity", null);
            String sUpdate = writeWeatherData.getString("mUpdate", null);
            String sDetails = writeWeatherData.getString("mDetails", null);
            String sTemperature = writeWeatherData.getString("mTemp", null);
            String sHumidity = writeWeatherData.getString("mHumidity", null);
            String sPressure = writeWeatherData.getString("mPressure", null);
            String sWeatherIcon = writeWeatherData.getString("mIcon", null);

            cityField.setText(sCity);
            updatedField.setText(sUpdate);
            detailsField.setText(sDetails);
            currentTemperatureField.setText(sTemperature);
            humidity_field.setText("Humidity: " + sHumidity);
            pressure_field.setText("Pressure: " + sPressure);
            weatherIcon.setText(Html.fromHtml(sWeatherIcon));

        }
        asyncTask.execute(mLocationString);

    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    mLastLocation = task.getResult();

                    lati = mLastLocation.getLatitude();
                    longi = mLastLocation.getLongitude();

                    geocoder = new Geocoder(MainActivity.this, Locale.ENGLISH);

                    try {
                        List<Address> addresses = geocoder.getFromLocation(lati, longi, 1);
                        String city = addresses.get(0).getLocality();
                        String state = addresses.get(0).getAdminArea();
                        String country = addresses.get(0).getCountryName();

                        mLocationString = city + "," + state + "," + country;



                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                   /* mLatitudeText.setText(String.format(Locale.ENGLISH, "%s: %f",
                            mLatitudeLabel,
                            mLastLocation.getLatitude()));
                    mLongitudeText.setText(String.format(Locale.ENGLISH, "%s: %f",
                            mLongitudeLabel,
                            mLastLocation.getLongitude()));*/
                } else {
                    Log.w(TAG, "getLastLocation:exception", task.getException());
                    showSnackbar(getString(R.string.no_location_detected));
                }
            }
        });
    }

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbar(final String text) {
        View container = findViewById(R.id.main_activity_container);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startLocationPermissionRequest();
                        }
                    });

        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest();
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                getLastLocation();
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

    private void getGreetings(TextView view) {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if (timeOfDay >= 0 && timeOfDay < 12) {
            view.setText(R.string.morning);
        } else if (timeOfDay >= 12 && timeOfDay < 16) {
            view.setText(R.string.afternoon);
        } else if (timeOfDay >= 16 && timeOfDay < 21) {
            view.setText(R.string.evening);
        } else if (timeOfDay >= 21 && timeOfDay < 24) {
            // Toast.makeText(this, "Good Night", Toast.LENGTH_SHORT).show();
            view.setText(R.string.night);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            getLastLocation();
        }

        if (mAuth.getCurrentUser() == null) {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        return true;
    }

    private void initNavDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.hamburger));

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        final CircleImageView imageProfile = (CircleImageView) headerView.findViewById(R.id.profileIconNav);
        final TextView userName = (TextView) headerView.findViewById(R.id.profileName);
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            Picasso.get().load(firebaseUser.getPhotoUrl()).into(imageProfile);
            userName.setText("Hi \n" + firebaseUser.getDisplayName());
        }

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        int id = item.getItemId();

        switch (id) {
            case R.id.home:
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;
            case R.id.categories:
                startActivity(new Intent(getApplicationContext(), CategoriesActivity.class));
                break;
            case R.id.profile:
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                break;
            case R.id.feedback:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.help);
                builder.setIcon(R.drawable.ic_live_help_black_24dp);
                builder.setMessage(R.string.aMessage);
                builder.setNegativeButton(R.string.negativeButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.setPositiveButton(R.string.positiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent Email = new Intent(Intent.ACTION_SEND);
                        Email.setType("text/email");
                        Email.putExtra(Intent.EXTRA_EMAIL, new String[]{"developer.rio105@gmail.com"});
                        Email.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
                        Email.putExtra(Intent.EXTRA_TEXT, "Dear," + " ");
                        startActivity(Intent.createChooser(Email, "Send Feedback:"));
                    }
                });
                builder.show();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public class DownloadNewsForVertical extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String xml = "";

            String urlParameters = "";
            xml = Function.excuteGet("https://newsapi.org/v2/top-headlines?country=in&category=" + categoryV + "&apiKey=" + API_KEY, urlParameters);
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {

            if (xml.length() > 10) {
                progressBarV.setVisibility(View.VISIBLE);
                try {
                    JSONObject jsonResponse = new JSONObject(xml);
                    JSONArray jsonArrays = jsonResponse.optJSONArray("articles");

                    for (int i = 0; i < jsonArrays.length(); i++) {
                        JSONObject jsonObject = jsonArrays.getJSONObject(i);
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(KEY_TITLE, jsonObject.optString(KEY_TITLE).toString());
                        map.put(KEY_DESCRIPTION, jsonObject.optString(KEY_DESCRIPTION).toString());
                        map.put(KEY_URL, jsonObject.optString(KEY_URL).toString());
                        map.put(KEY_URLTOIMAGE, jsonObject.optString(KEY_URLTOIMAGE).toString());
                        map.put(KEY_PUBLISHEDAT, jsonObject.optString(KEY_PUBLISHEDAT).toString());
                        dataListVertical.add(map);

                        if (haveNetworkConnection()) {
                            Paper.book().write("newsList", dataListVertical);
                        }
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Unexpected error", Toast.LENGTH_SHORT).show();
                }

                progressBarV.setVisibility(View.INVISIBLE);

                VerticalRecyclerAdapter verticalRecyclerAdapter = new VerticalRecyclerAdapter(MainActivity.this, dataListVertical);
                CarouselLayoutManager layoutManager = new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL);
                layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());
                VerticalRecyclerViews.setHasFixedSize(true);
                VerticalRecyclerViews.setLayoutManager(layoutManager);
                VerticalRecyclerViews.addOnScrollListener(new CenterScrollListener());
                VerticalRecyclerViews.setAdapter(verticalRecyclerAdapter);


            } else {
                Toast.makeText(MainActivity.this, "No News Found...Try again in a bit", Toast.LENGTH_SHORT).show();
            }

        }

    }

}
