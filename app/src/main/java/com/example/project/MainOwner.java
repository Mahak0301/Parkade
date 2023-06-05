package com.example.project;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;

import com.example.project.databinding.ActivityMainOwnerBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainOwner extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase db;

    AppConstants globalClass;
    Boolean dialogshown=false;
    Button addPosition;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        initComponents();
        attachListeners();
    }
    private void initComponents() {
        globalClass=(AppConstants)getApplicationContext();
        addPosition=findViewById(R.id.button);
        db=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
    }
    private void attachListeners() {
        db.getReference().child("ParkingAreas").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int found=0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ParkingArea parkingArea = dataSnapshot.getValue(ParkingArea.class);
                    if(parkingArea.userID.equals(auth.getCurrentUser().getUid())){
                        found=1;
                        break;
                    }
                    Log.d(String.valueOf(MainOwner.this.getClass()),"Load parking Area: "+String.valueOf(parkingArea));

                }
                if(found==0){
                    Intent intent = new Intent(MainOwner.this, AddPosition.class);
                    startActivity(intent);
                }
                else {
                    setContentView(R.layout.activity_main_owner);
                    BottomNavigationView bottomNavigationView= findViewById(R.id.bottomNavigationView);
                    NavController navController= Navigation.findNavController(MainOwner.this,R.id.nav_host_fragment);
                    NavigationUI.setupWithNavController(bottomNavigationView,navController);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}