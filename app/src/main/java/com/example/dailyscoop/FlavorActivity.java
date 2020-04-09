package com.example.dailyscoop;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;

public class FlavorActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();


    private ListView lv;
    private ArrayList<String> f = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flavor);

        lv = (ListView) findViewById(R.id.flavors_list_view);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item1:
                        Intent a = new Intent(FlavorActivity.this,HomeActivity.class);
                        startActivity(a);
                        break;
                    case R.id.item2:
                        Intent b = new Intent(FlavorActivity.this,LocationActivity.class);
                        startActivity(b);
                        break;
                    case R.id.item3:
                        break;
                }
                return false;
            }
        });

        final ArrayList<String> flavors = new ArrayList<String>();

        db.collection("flavors")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("test", document.getString("name"));
                                flavors.add(document.getString("name"));
                            }
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                                    FlavorActivity.this,
                                    android.R.layout.simple_list_item_1,
                                    flavors );
                            lv.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
                            lv.setAdapter(arrayAdapter);
                        } else {
                            Log.w("test", "Error getting documents.", task.getException());
                        }


                    }


                });


    }
}
