package com.example.dailyscoop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.LocationBias;
import com.google.android.libraries.places.api.model.LocationRestriction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    // Used to identify permissions for fine location
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 12; // Could be any number

    // Client to get the devices current location
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // Client's last location
    Location userLastLocation = null;

    private ImageView imgRest1, imgRest2, imgRest3, imgRest4;
    private TextView txtRes1, txtRes2, txtRes3, txtRes4;
    private FirebaseAuth firebaseAuth;
    private PlacesClient placesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setUpUIViews();

        // Set up the firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();

        // Get a FusedLocationProviderClient
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize the Places SDK
        Places.initialize(getApplicationContext(), "AIzaSyABHotiUHr9HaqPuoCq4mbqlzPPnZtfC3U");

        // Create a new Places client instance
        placesClient = Places.createClient(this);

        // Set the closest locations
        getCurrentLatLng();
        setFourClosestLocations();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item1:
                        break;
                    case R.id.item2:
                        Intent a = new Intent(HomeActivity.this,LocationActivity.class);
                        startActivity(a);
                        break;
                    case R.id.item3:
                        Intent b = new Intent(HomeActivity.this,FlavorActivity.class);
                        startActivity(b);
                        break;
                }
                return false;
            }
        });
    }

    private void setFourClosestLocations() {
        final TextView[] textViews = new TextView[] {txtRes1, txtRes2, txtRes3, txtRes4};

        if (userLastLocation == null) return;

        // Calculate the location bias bounds
        LatLng southwest = new LatLng(userLastLocation.getLatitude() - 0.125, userLastLocation.getLongitude() - 0.125);
        LatLng northeast = new LatLng(userLastLocation.getLatitude() + 0.125, userLastLocation.getLongitude() + 0.125);

        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        String query = "Culver's";
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setOrigin(new LatLng(userLastLocation.getLatitude(),userLastLocation.getLongitude()))
                .setLocationBias(RectangularBounds.newInstance(southwest, northeast))
                .setCountries("US")
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setSessionToken(token)
                .setQuery(query)
                .build();

        placesClient.findAutocompletePredictions(request).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
            @Override
            public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                if (task.isSuccessful()) {
                    int i = 0;
                    for (AutocompletePrediction prediction: task.getResult().getAutocompletePredictions()) {
                        if (i++ >= 1) return;
//                        TextView txtView = textViews[i++];
//                        txtView.setText(prediction.getFullText(null));

                        // Make an ArrayList of the fields we want returned
                        List<Place.Field> fields = new ArrayList<>();
                        fields.add(Place.Field.WEBSITE_URI);

                        // Send a Place detail requests for each location
                        placesClient.fetchPlace(FetchPlaceRequest.newInstance(prediction.getPlaceId(), fields))
                                .addOnCompleteListener(new OnCompleteListener<FetchPlaceResponse>() {
                            @Override
                            public void onComplete(@NonNull Task<FetchPlaceResponse> task) {
                                if (task.isSuccessful()) {
                                    System.out.println(task.getResult().getPlace().getWebsiteUri().toString());
                                }
                            }
                        });
                    }
                }
            }
        });


    }

    private void getCurrentLatLng() {
        // Check if permission granted
        int permission = ActivityCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);

        // If permission is not granted, ask for it
        if (permission == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            // Display marker at current location
            mFusedLocationProviderClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            Location mLastKnownLocation = task.getResult();
                            if (task.isSuccessful() && mLastKnownLocation != null) {
                                userLastLocation = mLastKnownLocation;
                                setFourClosestLocations();
                            }
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLatLng();
            }
        }
    }

    private void logout() {
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logoutMenu:
                logout();
                break;
            case R.id.settingsMenu:
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updatePicture(String newImage, int index) {

        String uri = "@drawable/" + newImage;

        int imageResource = getResources().getIdentifier(uri, null, getPackageName()); //get image  resource

        Drawable res = getResources().getDrawable(imageResource); // convert into drawble

        ImageView img = null;
        if (index == 0) {
            img = findViewById(R.id.img_rest1);
        } else if (index == 1) {
            img = findViewById(R.id.img_rest2);
        } else if (index == 2) {
            img = findViewById(R.id.img_rest3);
        } else if (index == 3) {
            img = findViewById(R.id.img_rest4);
        }

        if (img != null) {
            img.setImageDrawable(res); // set as image
        }

    }

    private void setUpUIViews() {
        imgRest1 = findViewById(R.id.img_rest1);
        imgRest2 = findViewById(R.id.img_rest2);
        imgRest3 = findViewById(R.id.img_rest3);
        imgRest4 = findViewById(R.id.img_rest4);

        txtRes1 = findViewById(R.id.txt_rest1);
        txtRes2 = findViewById(R.id.txt_rest2);
        txtRes3 = findViewById(R.id.txt_rest3);
        txtRes4 = findViewById(R.id.txt_rest4);
    }

}
