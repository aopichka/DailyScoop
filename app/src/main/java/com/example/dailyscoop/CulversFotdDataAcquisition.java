package com.example.dailyscoop;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class CulversFotdDataAcquisition extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_GET_MONTHS_FLAVORS = "com.example.dailyscoop.action.GET_MONTHS_FLAVORS";
    private static final String ACTION_BAZ = "com.example.dailyscoop.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_WEBSITE_URI = "com.example.dailyscoop.extra.WEBSITE_URI";
    private static final String EXTRA_PLACE_ID = "com.example.dailyscoop.extra.PLACE_ID";

    private FirebaseFirestore firestore;

    public CulversFotdDataAcquisition() {
        super("CulversFotdDataAcquisition");
        firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionGetMonthsFlavor(Context context, String websiteUri, String placeId) {
        Intent intent = new Intent(context, CulversFotdDataAcquisition.class);
        intent.setAction(ACTION_GET_MONTHS_FLAVORS);
        intent.putExtra(EXTRA_WEBSITE_URI, websiteUri);
        intent.putExtra(EXTRA_PLACE_ID, placeId);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, CulversFotdDataAcquisition.class);
        intent.setAction(ACTION_BAZ);
        //intent.putExtra(EXTRA_PARAM1, param1);
        //intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_MONTHS_FLAVORS.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_WEBSITE_URI);
                final String param2 = intent.getStringExtra(EXTRA_PLACE_ID);
                handleActionGetMonthsData(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                //final String param1 = intent.getStringExtra(EXTRA_LATITUDE);
                //final String param2 = intent.getStringExtra(EXTRA_LONGITUDE);
                //handleActionBaz(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionGetMonthsData(final String websiteUri, final String placeId) {
        // Get the RestaurantInfo object for this place
        firestore.collection("locations").document(placeId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    RestaurantInfo restaurantInfo = task.getResult().toObject(RestaurantInfo.class);
                    if (restaurantInfo == null) return; //TODO Fix this error handling

                    // Get the page contents
                    Document document;
                    try {
                        document = Jsoup.connect(websiteUri).get();
                    } catch (IOException ex) {
                        return;
                    }

                    // Parse the page for the FOTDs
                    Elements fotdElements = document.select("div.fotdlist-unordered flavorsParted1st > div.lowerstub");
                    for (int i=0; i<fotdElements.size(); i++) {
                        String date = fotdElements.get(i).child(0).text().trim();
                        String fotd = fotdElements.get(i).child(1).attributes().get("alt");

                        // Add the fotd to the RestaurantInfo instance
                        restaurantInfo.addFotdToSchedule(date, fotd);
                    }

                    // Upload the data to firestore
                    firestore.collection("locations").document(placeId).set(restaurantInfo);
                }
            }
        });
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
