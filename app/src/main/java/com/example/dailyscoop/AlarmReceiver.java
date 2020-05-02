package com.example.dailyscoop;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {

    Location userLastLocation = null;     // Client's last location
    PlacesClient placesClient;

    public void onReceive(final Context context, Intent intent) {
        getClosestLocation(context, intent);
    }

    public static void SendNotifications(final Context context, Intent intent, String placeId){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("locations")
                .whereEqualTo("placeId", placeId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            String fotd = task.getResult().getDocuments().get(0).getString("fotd");
                            String name = task.getResult().getDocuments().get(0).getString("name");
                            String address = task.getResult().getDocuments().get(0).getString("address");

                            // send notification
                            Intent activityIntent = new Intent (context, HomeActivity.class);
                            PendingIntent contentIntent = PendingIntent.getActivity(context,
                                    0, activityIntent, 0);

                            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                            Notification notification = new NotificationCompat.Builder(context, App.CHANNEL_1_ID)
                                    .setSmallIcon(R.drawable.ic_ds_main_logo_outline)
                                    .setContentTitle("Today's Flavor is " + fotd + "!")
                                    .setContentText("At " + name + " (" + address + ")" + Calendar.getInstance().getTime() + "]")
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setContentIntent(contentIntent)
                                    .build();
                            notificationManager.notify(1, notification);
                        } else {
                            Log.w("test", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    public void getClosestLocation(final Context context, final Intent intent){
        // find closest location

        int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 12; // Used to identify permissions for fine location
        FusedLocationProviderClient mFusedLocationProviderClient; // Client to get the devices current location

        // Get a FusedLocationProviderClient
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        // Initialize the Places SDK
        Places.initialize(context, "AIzaSyABHotiUHr9HaqPuoCq4mbqlzPPnZtfC3U");

        // Create a new Places client instance
        placesClient = Places.createClient(context);

        // Check if permission granted
        int permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission != PackageManager.PERMISSION_DENIED) {
            mFusedLocationProviderClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            Location mLastKnownLocation = task.getResult();
                            if (task.isSuccessful() && mLastKnownLocation != null) {
                                userLastLocation = mLastKnownLocation;

                                if (userLastLocation == null) return;

                                AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
                                String query = "Culver's";
                                FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                                        .setOrigin(new LatLng(userLastLocation.getLatitude(),userLastLocation.getLongitude()))
                                        .setCountries("US")
                                        .setTypeFilter(TypeFilter.ESTABLISHMENT)
                                        .setSessionToken(token)
                                        .setQuery(query)
                                        .build();
                                placesClient.findAutocompletePredictions(request).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                                    @Override
                                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                                        if (task.isSuccessful()) {
                                            SendNotifications(context, intent, task.getResult().getAutocompletePredictions().get(0).getPlaceId());
                                        }
                                    }
                                });
                            }
                        }
                    });
        }
    }
}