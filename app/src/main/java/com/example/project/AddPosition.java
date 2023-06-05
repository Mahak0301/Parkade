package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class AddPosition extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseDatabase db;

    FloatingActionButton addLocationBtn;
    AppCompatEditText areaNameText,upiIdText,upiNameText,amount2Text,amount3Text,amount4Text,totalSlotsText;
    Button loadFromFile;

    FusedLocationProviderClient client;
    List<SlotNoInfo> slotNos = new ArrayList<>();
    List<String> slotNoString = new ArrayList<>();
    BasicUtils utils=new BasicUtils();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_position);
        initComponents();
        attachListeners();

    }

    private void initComponents() {

        client= LocationServices.getFusedLocationProviderClient(AddPosition.this);

        auth= FirebaseAuth.getInstance();
        db= FirebaseDatabase.getInstance();

        areaNameText=findViewById(R.id.areaNameText);
        upiIdText=findViewById(R.id.upiIdText);
        upiNameText=findViewById(R.id.upiNameText);
        totalSlotsText=findViewById(R.id.totalSlotsText);
        amount2Text=findViewById(R.id.amount2Text);
        amount3Text=findViewById(R.id.amount3Text);
        amount4Text=findViewById(R.id.amount4Text);
        addLocationBtn=findViewById(R.id.addLocationBtn);
//        slotNoString.add("Slot-01");
    }
    private void attachListeners(){
       addLocationBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {

               String areaName = areaNameText.getText().toString();
               String upiId = upiIdText.getText().toString();
               String upiName = upiNameText.getText().toString();
               String amount2 = amount2Text.getText().toString();
               String amount3 = amount3Text.getText().toString();
               String amount4 = amount4Text.getText().toString();
               String totalSlots = totalSlotsText.getText().toString();
               int n=Integer.parseInt(totalSlots);
               for (int i=1;i<=n;i++) {
                   String text=Integer.toString(i);
                   slotNos.add(new SlotNoInfo((String) text,false));
               }
               if(areaName.equals("") || upiId.equals("") || upiName.equals("") || amount2.equals("") || amount3.equals("") || amount4.equals("") || totalSlots.equals("")){
                   Toast.makeText(AddPosition.this, "Please enter all fields!", Toast.LENGTH_SHORT).show();
               }else if(!utils.isUpiIdValid(upiId)){
                   Toast.makeText(AddPosition.this, "Invalid UPI ID!", Toast.LENGTH_SHORT).show();
               }else {

                   final ParkingArea parkingArea = new ParkingArea(areaName/* globalLatLng.latitude, globalLatLng.longitude*/,auth.getCurrentUser().getUid(), Integer.parseInt(totalSlots), 0, Integer.parseInt(amount2), Integer.parseInt(amount3), Integer.parseInt(amount4), slotNos);
                   final UpiInfo upiInfo = new UpiInfo(upiId, upiName);
                   final String key=db.getReference("ParkingAreas").push().getKey();
                   db.getReference("ParkingAreas").child(key).setValue(parkingArea).addOnCompleteListener(new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task) {
                           if (task.isSuccessful()) {
                               db.getReference("UpiInfo").child(auth.getCurrentUser().getUid()).setValue(upiInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task) {
                                       if (task.isSuccessful()) {
                                           Toast.makeText(AddPosition.this, "Success", Toast.LENGTH_SHORT).show();
                                           Intent intent = new Intent(AddPosition.this, MainOwner.class);
                                           intent.putExtra("FRAGMENT_NO", 0);
                                           startActivity(intent);
                                           //overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);  //slide from left to right
                                           finish();
                                       } else {
                                           Toast.makeText(AddPosition.this, "Failed to add UPI details", Toast.LENGTH_SHORT).show();
                                       }
                                   }
                               });
                           } else {
                               Toast.makeText(AddPosition.this, "Failed to add extra details", Toast.LENGTH_SHORT).show();
                           }
                       }
                   });
               }
           }
       });
    }
}