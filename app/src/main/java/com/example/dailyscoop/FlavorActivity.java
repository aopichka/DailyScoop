package com.example.dailyscoop;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;

public class FlavorActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth;

    private ListView lv;
    private ArrayList<String> f = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        firebaseAuth = FirebaseAuth.getInstance();
        final String userID = firebaseAuth.getCurrentUser().getUid();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flavor);

        lv = (ListView) findViewById(R.id.flavors_list_view);

        final ArrayList<String> selectedFlavorIDs = new ArrayList<String>();

        db.collection("user_flavor_relationship")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(document.getString("userID").equalsIgnoreCase(userID)){
                                    selectedFlavorIDs.add(document.getString("flavorID"));
                                }
                            }
                        }
                    }
                });

        final ArrayList<String> flavors = new ArrayList<String>();
        final ArrayList<String> ids = new ArrayList<>();

        db.collection("flavors")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(selectedFlavorIDs.contains(document.getId())){
                                    flavors.add(document.getString("name") + " (selected)");
                                    ids.add(document.getId());
                                }
                                else{
                                    flavors.add(document.getString("name"));
                                    ids.add(document.getId());
                                }
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

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String flavorID = ids.get(position);

                TextView tv = (TextView)view.findViewById(android.R.id.text1);
                if(tv.getText().toString().contains("(selected)")){
                    Log.w("contains", tv.getText().toString());
                    tv.setText(parent.getItemAtPosition(position).toString().replace("(selected)", ""));
                    Log.w("after", tv.getText().toString());
                }
                else{
                    Log.w("not", tv.getText().toString());
                    tv.setText(parent.getItemAtPosition(position).toString() + " (selected)");
                }

                db.collection("user_flavor_relationship")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                boolean found = false;
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        //Document exists, remove it
                                        if(document.getString("userID").equalsIgnoreCase(userID) && document.getString("flavorID").equalsIgnoreCase(flavorID)){
                                            String docID = document.getId();
                                            db.collection("user_flavor_relationship").document(docID).delete();
                                            found = true;
                                            break;
                                        }
                                    }
                                    if(!found){
                                        UserFlavor userFlavor = new UserFlavor(flavorID, userID);
                                        db.collection("user_flavor_relationship").add(userFlavor);
                                    }
                                }
                            }
                        });
            }
        });

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
                startActivity(new Intent(FlavorActivity.this, SettingsActivity.class));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(FlavorActivity.this, LoginActivity.class));
    }
}
