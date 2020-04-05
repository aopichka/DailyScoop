package com.example.dailyscoop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        testDisplayFlavors();
        testDisplayLocations();
        testDisplaySchedules();

    }

    private void testDisplayLocations(){
        db.collection("locations")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<Location> locations = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                locations.add(document.toObject(Location.class));
                            }
                            TextView locationDisplay = findViewById(R.id.locations);
                            for (Location location : locations){
                                locationDisplay.setText(locationDisplay.getText() + "\n" + location.name);
                            }
                        } else {
                            // error getting documents
                        }
                    }
                });
    }

    private void testDisplayFlavors(){
        db.collection("flavors")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<Flavor> flavors = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                flavors.add(document.toObject(Flavor.class));
                            }
                            TextView flavorDisplay = findViewById(R.id.flavors);
                            for (Flavor flavor : flavors){
                                flavorDisplay.setText(flavorDisplay.getText() + "\n" + flavor.name);
                            }
                        } else {
                            // error getting documents
                        }
                    }
                });
    }

    private void testDisplaySchedules(){
        db.collection("schedules")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<Schedule> schedules = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                schedules.add(document.toObject(Schedule.class));
                            }
                            TextView scheduleDisplay = findViewById(R.id.schedules);
                            for (Schedule schedule : schedules){
                                scheduleDisplay.setText(scheduleDisplay.getText() + "\n" + schedule);
                            }
                        } else {
                            // error getting documents
                        }
                    }
                });
    }
}
