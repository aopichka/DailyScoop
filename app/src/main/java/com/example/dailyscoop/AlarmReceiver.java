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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {


    public void onReceive(final Context context, Intent intent) {
        SendNotifications(context, intent);
    }

    public static void SendNotifications(final Context context, Intent intent){
        final ArrayList<String> flavors = new ArrayList<String>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("flavors")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                flavors.add(document.getString("name"));
                            }

                            // send notification
                            Intent activityIntent = new Intent (context, HomeActivity.class);
                            PendingIntent contentIntent = PendingIntent.getActivity(context,
                                    0, activityIntent, 0);

                            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                            Notification notification = new NotificationCompat.Builder(context, App.CHANNEL_1_ID)
                                    .setSmallIcon(R.drawable.ic_event_available_black_24dp)
                                    .setContentTitle("Today's Flavor is " + flavors.get(0))
                                    .setContentText("At _______ [" + Calendar.getInstance().getTime() + "]")
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