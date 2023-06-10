package com.example.project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class BookParkingArea extends AppCompatActivity {
    Spinner numberPlateSpinner,areaSpinner;
    TextView wheelerText;
    FirebaseAuth auth;
    FirebaseDatabase db;
    List<Integer> numberPlateWheeler = new ArrayList<Integer>();

    List<String> numberPlateNumber = new ArrayList<String>();
    List<String> AreaList = new ArrayList<String>();

    BookedSlots bookingSlot = new BookedSlots();
    ParkingArea parkingArea;
    UserDetails userObj;
    AppConstants globalClass;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_parking_area);
        initComponents();
        attachListeners();
        addItemsOnNumberPlateSpinner();
        addItemsOnAreaSpinner();
    }

    private void addItemsOnAreaSpinner() {
        ArrayAdapter<String> AreaAdapter = new ArrayAdapter<String>(BookParkingArea.this,
                android.R.layout.simple_spinner_item, AreaList);
        areaSpinner.setAdapter(AreaAdapter);
        db.getReference().child("ParkingAreas").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                 ParkingArea area=dataSnapshot.getValue(ParkingArea.class);
                 AreaList.add(area.name);
                 AreaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addItemsOnNumberPlateSpinner() {
        ArrayAdapter<String> NumberPlateAdapter = new ArrayAdapter<String>(BookParkingArea.this,
                android.R.layout.simple_spinner_item, numberPlateNumber);
        numberPlateSpinner.setAdapter(NumberPlateAdapter);
        db.getReference().child("NumberPlates").orderByChild("userID").equalTo(auth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener(){
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            NumberPlate numberPlate = dataSnapshot.getValue(NumberPlate.class);
                            if(numberPlate.isDeleted==0){
                                numberPlateWheeler.add(numberPlate.wheelerType);
                                numberPlateNumber.add(numberPlate.numberPlate);
                            }
                        }
                        NumberPlateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }


    private void initComponents() {
        auth = FirebaseAuth.getInstance();
        userID=auth.getCurrentUser().getUid();

        db = FirebaseDatabase.getInstance();
       // db = FirebaseDatabase.getInstance().getReference("NumberPlates").orderByChild(key).equalTo(userID);
        globalClass=(AppConstants)getApplicationContext();
        userObj=globalClass.getUserObj();
        wheelerText = findViewById(R.id.wheelerText);
        numberPlateSpinner=findViewById(R.id.vehicleSelect);
        areaSpinner=findViewById(R.id.areaSelect);

    }

    private void attachListeners() {

    }
}