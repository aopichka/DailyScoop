package com.example.dailyscoop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class FlavorDetailActivity extends AppCompatActivity {
    private ListView listView;
    private ArrayList<String> listItems;
    private ArrayList<RestaurantInfo> restaurants;
    private String flavorName;
    private FirebaseFirestore firestore;
    private PlacesClient placesClient;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flavor_detail);

        listView = (ListView) findViewById(R.id.mobile_list);

        placesClient = Places.createClient(this);
        firestore = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("com.example.dailyscoop", Context.MODE_PRIVATE);

        restaurants = new ArrayList<>();
        listItems = new ArrayList<>();

        // Get the flavor name from the intent
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                flavorName = null;
            } else {
                flavorName = extras.getString("Name");
            }
        } else {
            flavorName = (String) savedInstanceState.getSerializable("Name");
        }

        // Update title of the flavor name
        TextView title = (TextView) findViewById(R.id.flav1);
        title.setText(flavorName);

        String flavorNamePath = getPathForFlavor(flavorName);
        updatePicture(flavorNamePath);

        loadCachedLocations();

        BottomNavigationView navigation;
        navigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item1:
                        Intent a = new Intent(FlavorDetailActivity.this, HomeActivity.class);
                        finish();
                        startActivity(a);
                        break;
                    case R.id.item2:
                        Intent b = new Intent(FlavorDetailActivity.this, LocationActivity.class);
                        finish();
                        startActivity(b);
                        break;
                    case R.id.item3:
                        Intent c = new Intent(FlavorDetailActivity.this, FlavorActivity.class);
                        finish();
                        startActivity(c);
                        break;
                }
                return false;
            }
        });
    }

    private void loadCachedLocations() {
        restaurants.clear();
        for (int i=0; i<6; i++) {
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
                                loadNewLocations(textViewIndex, 1);
                            } else {
                                restaurants.add(restaurantInfo);

                                // Start the service to update the full month of data
                                CulversFotdDataAcquisition.startActionGetMonthsFlavor(getBaseContext(), restaurantInfo.getWebsiteUri(), restaurantInfo.getPlaceId());

                                updateListView();
                            }
                        }
                    }
                });
            } else {
                loadNewLocations(i, 1);
                return;
            }
            // TODO deal with this error condition
        }
    }

    private void loadNewLocations(final int offset, final int num) {
        double userStoredLatitude = Double.longBitsToDouble(sharedPreferences.getLong("UserLastLatitude", (long) 0.0));
        double userStoredLongitude = Double.longBitsToDouble(sharedPreferences.getLong("UserLastLongitude", (long) 0.0));

        // Calculate the location bias bounds
        LatLng southwest = new LatLng(userStoredLatitude - 0.125, userStoredLongitude - 0.125);
        LatLng northeast = new LatLng(userStoredLatitude + 0.125, userStoredLongitude + 0.125);

        // Craft the query for nearby Culver's
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        String query = "Culver's";
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setOrigin(new LatLng(userStoredLatitude,userStoredLongitude))
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
                        if (i < offset) { i++; continue; }
                        if (i++ >= num + offset) return;

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
                                    restaurants.add(restaurantInfo);

                                    // Start the service to update the full month of data
                                    CulversFotdDataAcquisition.startActionGetMonthsFlavor(getBaseContext(), restaurantInfo.getWebsiteUri(), restaurantInfo.getPlaceId());

                                    updateListView();
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void updateListView() {
        listItems.clear();
        // Construct the items for the listview
        for (int i=0; i<restaurants.size(); i++) {
            RestaurantInfo restaurantInfo = restaurants.get(i);

            // Get the days away
            String daysAway = "Not upcoming";
            String nextDateTemp = restaurantInfo.getDateForFlavor(flavorName);
            if (!nextDateTemp.isEmpty()) {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd", Locale.US);
                Date parsedDate;
                try {
                    parsedDate = sdf.parse(nextDateTemp);
                } catch (ParseException ex) {
                    return; // TODO Fix this error handling
                }
                cal.setTime(parsedDate);

                // Set the time to this year
                cal.set(Calendar.HOUR_OF_DAY, 0);

                cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));

                if (Calendar.getInstance().before(cal)) {
                    daysAway = String.valueOf(daysBetween(new GregorianCalendar().getTime(), cal.getTime()));
                }
            }

            // Construct the string to add
            listItems.add(restaurantInfo.getAddress() + "- Days Away: " + daysAway);
        }

        // Construct the data source
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(FlavorDetailActivity.this, android.R.layout.simple_list_item_1, listItems);

        // Attach the adapter to a ListView
        listView.setAdapter(arrayAdapter);
    }

    private int daysBetween(Date d1, Date d2){
        return (int)( (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }

    private String getPathForFlavor(String name) {
        String pathToImage = name.toLowerCase();
        pathToImage = pathToImage.trim();
        pathToImage = pathToImage.replaceAll(" ", "");
        pathToImage = pathToImage.replaceAll("’", "");

        return pathToImage;
    }


    private void updatePicture(String newImage) {

        String uri = newImage;

        int imageResource = getResources().getIdentifier(uri, "drawable", getPackageName()); //get image  resource

        Drawable res = getResources().getDrawable(imageResource); // convert into drawble

        ImageView img = findViewById(R.id.img_rest1);

        if (img != null) {
            img.setImageDrawable(res); // set as image
        }

    }
}
