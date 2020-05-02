package com.example.dailyscoop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    private static final int METERS_CHANGED_BEFORE_UPDATE = 8000;

    // Used to identify permissions for fine location
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 12; // Could be any number

    // Client to get the devices current location
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // Client's last location
    private Location userLastLocation = null;

    // Access to the firebase DB
    private FirebaseFirestore firestore;

    // Access to SharedPreferences to store the user's last location
    private SharedPreferences sharedPreferences;

    // Model - Data to be displayed on the screen
    List<RestaurantInfo> restaurantInfos;

    private ImageView imgRest1, imgRest2, imgRest3, imgRest4, imgRest5;
    private TextView flav1, flav2;
    private TextView txtRes1, txtRes2;
    private List<TextView> textViews;
    private List<TextView> flavViews;
    private FirebaseAuth firebaseAuth;
    private PlacesClient placesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setUpUIViews();

        // Set up the firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();

        // Set up the firestore database
        firestore = FirebaseFirestore.getInstance();

        // Set up the SharedPreferences Access
        sharedPreferences = getSharedPreferences("com.example.dailyscoop", Context.MODE_PRIVATE);

        // Get a FusedLocationProviderClient
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize the Places SDK
        Places.initialize(getApplicationContext(), "AIzaSyABHotiUHr9HaqPuoCq4mbqlzPPnZtfC3U");

        // Create a new Places client instance
        placesClient = Places.createClient(this);

        // Set the closest locations
        getCurrentLatLng();
        restaurantInfos = new ArrayList<>();
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

    private void setUpcomingFavoriteFlavors() {
        // TODO TESTING
        List<String> favFlavors = new ArrayList<>();
        favFlavors.add("Turtle");
        favFlavors.add("Mint Chip");
        favFlavors.add("Cookie Dough Craving");

        // Set the dates for each flavor
        for (String flavor : favFlavors) {
            String nextDateString = "";
            Calendar nextDateCal = null;
            for (RestaurantInfo restaurantInfo : restaurantInfos) {
                String nextDateTemp = restaurantInfo.getDateForFlavor(flavor);
                if (nextDateTemp.isEmpty()) continue;

                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd", Locale.US);
                Date parsedDate;
                try {
                    parsedDate = sdf.parse(date);
                } catch (ParseException ex) {
                    return; // TODO Fix this error handling
                }
                cal.setTime(parsedDate);

                // Set the time to midnight
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);

                if (nextDateString.isEmpty() || cal.before(nextDateCal)) {
                    nextDateString = nextDateTemp;
                    nextDateCal = cal;
                }
            }
            if (!nextDateString.isEmpty()) {
                // Set the UI elements here
            } else {
                // Set the default UI elements here
            }
        }
    }

    private void setFourClosestLocations() {
        if (userLastLocation == null) {
            // Set the Textviews to default values
            for (TextView textView : textViews) {
                textView.setText("Unable to load data");
            }
            return;
        } else {
            // Set the Textviews to default values
            for (TextView textView : textViews) {
                textView.setText("Loading Data...");
            }
            for (TextView flavView : flavViews) {
                flavView.setText("Loading Data...");
            }
        }

        // Check the current user location with the last one stored
        double userStoredLatitude = Double.longBitsToDouble(sharedPreferences.getLong("UserLastLatitude", (long) 0.0));
        double userStoredLongitude = Double.longBitsToDouble(sharedPreferences.getLong("UserLastLongitude", (long) 0.0));
        float[] results = new float[1];
        Location.distanceBetween(userLastLocation.getLatitude(), userLastLocation.getLongitude(), userStoredLatitude, userStoredLongitude, results);
        if (results[0] < METERS_CHANGED_BEFORE_UPDATE) {
            loadCachedLocations();
        } else {
            loadNewLocations();
        }
    }

    private void loadNewLocations() {
        restaurantInfos.clear();

        // Calculate the location bias bounds
        LatLng southwest = new LatLng(userLastLocation.getLatitude() - 0.125, userLastLocation.getLongitude() - 0.125);
        LatLng northeast = new LatLng(userLastLocation.getLatitude() + 0.125, userLastLocation.getLongitude() + 0.125);

        // Craft the query for nearby Culver's
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
                    for (AutocompletePrediction prediction : task.getResult().getAutocompletePredictions()) {
                        if (i++ >= textViews.size()) return;

                        // Make an ArrayList of the fields we want returned
                        List<Place.Field> fields = new ArrayList<>();
                        fields.add(Place.Field.WEBSITE_URI);
                        fields.add(Place.Field.NAME);
                        fields.add(Place.Field.ID);
                        fields.add(Place.Field.ADDRESS);

                        // Send a Place detail request for each location
                        placesClient.fetchPlace(FetchPlaceRequest.newInstance(prediction.getPlaceId(), fields)).addOnCompleteListener(new OnCompleteListener<FetchPlaceResponse>() {
                            @Override
                            public void onComplete(@NonNull Task<FetchPlaceResponse> task) {
                                if (task.isSuccessful()) {
                                    Place place = task.getResult().getPlace();

                                    // Use the website data to get the FOTD
                                    String websiteUri = place.getWebsiteUri().toString()
                                            .replaceFirst("^http", "https");

                                    // Get the FOTD and update the TextViews (All done in the AsyncTask)
                                    // Create the Restaurant info object to store the data we receive
                                    RestaurantInfo restaurantInfo = new RestaurantInfo(place.getId());
                                    restaurantInfo.setName(place.getName());
                                    restaurantInfo.setAddress(place.getAddress());
                                    //restaurantInfo.setImage();
                                    restaurantInfo.setWebsiteUri(websiteUri);
                                    restaurantInfos.add(restaurantInfo);

                                    new CulversInfoAsyncTask().execute(restaurantInfo);
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void loadCachedLocations() {
        restaurantInfos.clear();
        for (int i=0; i<textViews.size(); i++) {
            // Get the PlaceId of the stored location
            String placeId = sharedPreferences.getString("RestaurantInfo" + i, "");
            if (!placeId.isEmpty()) {
                final int textViewIndex = i;
                firestore.collection("locations")
                        .document(placeId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    RestaurantInfo restaurantInfo = task.getResult().toObject(RestaurantInfo.class);
                                    if (restaurantInfo == null) {
                                        loadNewLocations();
                                    } else {
                                        restaurantInfos.add(restaurantInfo);
                                        updateFotd(restaurantInfo);
                                        updateRestaurantInfoTextViews();
                                    }
                                }
                            }
                        });
            } else {
                loadNewLocations();
                return;
            }
            // TODO deal with this error condition
        }
    }

    private RestaurantInfo updateFotd(RestaurantInfo restaurantInfo) {
        // Get the current day at midnight
        Calendar today = new GregorianCalendar();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        // If needed recompute the flavor of the day
        if (!today.before(restaurantInfo.getFotdLastUpdatedDate())) {
            new CulversInfoAsyncTask().execute(restaurantInfo);
        }
        return restaurantInfo;
    }

    private void updateRestaurantInfoTextViews() {
        for (int i=0; i<textViews.size() && i<restaurantInfos.size(); i++) {
            TextView textView = textViews.get(i);
            TextView flavView = flavViews.get(i);
            RestaurantInfo restaurantInfo = restaurantInfos.get(i);

            // Store the location data in SharedPreferences
            sharedPreferences.edit().putString("RestaurantInfo" + i, restaurantInfo.getPlaceId()).apply();

            // Craft the string for the rest info TESTING TODO
            String label = restaurantInfo.getAddress();
            textView.setText(label);
            String label2 = restaurantInfo.getFotd();
            flavView.setText(label2);
        }

        setUpcomingFavoriteFlavors(); // TODO maybe combine these methods into a master method
    }

    private String getFOTD(String websiteUri) {
        if (websiteUri == null) return "";

        // Get the page contents
        Document document;
        try {
            document = Jsoup.connect(websiteUri).get();
        } catch (IOException ex) {
            return "Unable to find FOD";
        }

        // Parse the page for the FOD
        Element fodImage = document.select("div.ModuleRestaurantDetail-fotd > img").first();
        return fodImage.attributes().get("alt");
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
            mFusedLocationProviderClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            Location mLastKnownLocation = task.getResult();
                            if (task.isSuccessful() && mLastKnownLocation != null) {
                                userLastLocation = mLastKnownLocation;

                                // Store the user's last location in Shared Preferences
                                sharedPreferences.edit().putLong("UserLastLatitude", Double.doubleToLongBits(userLastLocation.getLatitude())).apply();
                                sharedPreferences.edit().putLong("UserLastLongitude", Double.doubleToLongBits(userLastLocation.getLongitude())).apply();

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
        } else if (index == 4) {
            img = findViewById(R.id.img_rest5);
        }

        if (img != null) {
            img.setImageDrawable(res); // set as image
        }

    }

    private void setUpUIViews() {

        // Image
        imgRest1 = findViewById(R.id.img_rest1);
        imgRest2 = findViewById(R.id.img_rest2);
        imgRest3 = findViewById(R.id.img_rest3);
        imgRest4 = findViewById(R.id.img_rest4);
        imgRest5 = findViewById(R.id.img_rest5);

        // Address
        txtRes1 = findViewById(R.id.txt_rest1);
        txtRes2 = findViewById(R.id.txt_rest2);

        // Flavor
        flav1 = findViewById(R.id.flav1);
        flav2 = findViewById(R.id.flav2);

        textViews = new ArrayList<>();
        textViews.add(txtRes1); // Addresses
        textViews.add(txtRes2);

        flavViews = new ArrayList<>();
        flavViews.add(flav1); // Addresses
        flavViews.add(flav2);
    }

    private class CulversInfoAsyncTask extends AsyncTask<RestaurantInfo, Integer, RestaurantInfo> {
        protected RestaurantInfo doInBackground(RestaurantInfo... restaurantInfo) {
            restaurantInfo[0].setFotd(getFOTD(restaurantInfo[0].getWebsiteUri()));

            // Start the service to update the full month of data
            CulversFotdDataAcquisition.startActionGetMonthsFlavor(getBaseContext(), restaurantInfo[0].getWebsiteUri(), restaurantInfo[0].getPlaceId());

            // Upload the data to the firestore DB
            firestore.collection("locations").document(restaurantInfo[0].getPlaceId()).set(restaurantInfo[0]);

            return restaurantInfo[0];
        }
        protected void onProgressUpdate(Integer... progress) { }
        protected void onPostExecute(RestaurantInfo restaurantInfo) { updateRestaurantInfoTextViews(); }
    }
}


