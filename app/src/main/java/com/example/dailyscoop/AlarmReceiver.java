package com.example.dailyscoop;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {


    public void onReceive(final Context context, Intent intent) {
        SendNotifications(context, intent);
    }

    public static void SendNotifications(final Context context, Intent intent){

        // todo: find closest location and get its placeId
        String placeId = "ChIJHccCSrQgB4gRCm5LXedK-fo";

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
                                    .setContentTitle("Today's Flavor is " + fotd)
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
}