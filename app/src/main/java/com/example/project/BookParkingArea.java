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
    Spinner numberPlateSpinner , areaSpinner;
    TextView wheelerText,amountText,endDateText,endTimeText;
    FloatingActionButton bookBtn;
    LinearLayout endDate,endTime;

    FirebaseAuth auth;
    FirebaseDatabase db;
    List<Integer> numberPlateWheeler = new ArrayList<Integer>();
    List<String> numberPlateNumber = new ArrayList<String>();

    List<String> areaList = new ArrayList<String>();

    Calendar calendar;

    BookedSlots bookingSlot=new BookedSlots();
    ParkingArea parkingArea;
    UserDetails userObj;
    NotificationHelper mNotificationHelper;
    AppConstants globalClass;

    BasicUtils utils=new BasicUtils();

    UpiInfo upiInfo;

    String[] PERMISSIONS = {
//            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.INTERNET,
    };

    UPIPayment upiPayment=new UPIPayment();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_parking_area);
        initComponents();
        attachListeners();
        if(!utils.isNetworkAvailable(getApplication())){
            Toast.makeText(BookParkingArea.this, "No Network Available!", Toast.LENGTH_SHORT).show();
        }
    }



    private void initComponents() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        globalClass=(AppConstants)getApplicationContext();
        userObj=globalClass.getUserObj();

        areaSpinner = findViewById(R.id.areaSelect);
        numberPlateSpinner = findViewById(R.id.vehicleSelect);
        endDate = findViewById(R.id.endDate);
        endTime = findViewById(R.id.endTime);
        endDateText = findViewById(R.id.endDateText);
        endTimeText = findViewById(R.id.endTimeText);
//        endDateText.setInputType(InputType.TYPE_NULL);
//        endTimeText.setInputType(InputType.TYPE_NULL);
        bookBtn = findViewById(R.id.bookBtn);
        wheelerText = findViewById(R.id.wheelerText);
        amountText = findViewById(R.id.amountText);
        mNotificationHelper=new NotificationHelper(this);

        calendar=new GregorianCalendar();
        bookingSlot.startTime=bookingSlot.endTime=bookingSlot.checkoutTime=calendar.getTime();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("hh:mm a");
        endTimeText.setText(simpleDateFormat.format(bookingSlot.endTime));
        simpleDateFormat=new SimpleDateFormat("dd-MM-yyyy");
        endDateText.setText(simpleDateFormat.format(bookingSlot.endTime));
        bookingSlot.readNotification=0;
        bookingSlot.readBookedNotification=1;
        bookingSlot.hasPaid=0;
        bookingSlot.userID=auth.getCurrentUser().getUid();

        Bundle bundle = getIntent().getExtras();
        bookingSlot.placeID=bundle.getString("UUID");
        final ParkingArea parkingArea = (ParkingArea) getIntent().getSerializableExtra("ParkingArea");
        Log.e(String.valueOf(BookParkingArea.this.getClass()),"Fetched parking Area:"+ parkingArea.name+" "+bookingSlot.placeID);

    }
    private void attachListeners() {endDate.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showDatePicker(endDateText);
        }
    });
        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker(endTimeText);
            }
        });
        bookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(numberPlateSpinner.getSelectedItemPosition()==0){
                    Toast.makeText(BookParkingArea.this, "Please select a vehicle!", Toast.LENGTH_SHORT).show();
                }else if(areaSpinner.getSelectedItemPosition()==0){
                    Toast.makeText(BookParkingArea.this, "Please select an Area!", Toast.LENGTH_SHORT).show();
                }
                else if(bookingSlot.endTime.equals(bookingSlot.startTime)){
                    Toast.makeText(BookParkingArea.this,
                            "Please set the end time!", Toast.LENGTH_SHORT).show();
                }else if(!bookingSlot.timeDiffValid()){
                    Toast.makeText(BookParkingArea.this,
                            "Less time difference (<15 minutes)!", Toast.LENGTH_SHORT).show();
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(BookParkingArea.this);
                    builder.setCancelable(true);
                    builder.setTitle("Confirm Booking");
                    builder.setMessage("Confirm Booking for this area?");
                    builder.setPositiveButton("Confirm",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(parkingArea.availableSlots>0) {
                                        parkingArea.allocateSpace();
                                        db.getReference("ParkingAreas").child(bookingSlot.placeID).setValue(parkingArea);
                                        String note ="Payment for ".concat(bookingSlot.placeID).concat(" and number ").concat(bookingSlot.numberPlate);
                                        Boolean upi=upiPayment.payUsingUpi(String.valueOf(bookingSlot.amount), "7014924886@ybl", "Akshat", note,BookParkingArea.this);
//                                        Boolean upi=upiPayment.payUsingUpi(String.valueOf(bookingSlot.amount), upiInfo.upiId, upiInfo.upiName, note,BookParkingAreaActivity.this);
//                                        saveData();
                                    }else{
                                        Toast.makeText(BookParkingArea.this,"Failed! Slots are full.",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
        db.getReference().child("ParkingAreas").child(bookingSlot.placeID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if(snapshot.getKey().equals("availableSlots") || snapshot.getKey().equals("occupiedSlots") || snapshot.getKey().equals("totalSlots")){
                            parkingArea.setData(snapshot.getKey(),snapshot.getValue(int.class));
                            Log.e(String.valueOf(BookParkingArea.this.getClass()),"Fetched updated parking Area:"+ String.valueOf(snapshot.getKey())+snapshot.getValue(int.class));
                        }
                    }
                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        db.getReference().child("ParkingAreas").child(bookingSlot.placeID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ParkingArea parkingArea = snapshot.getValue(ParkingArea.class);
                        setAddValues(parkingArea);
                        db.getReference().child("UpiInfo").child(parkingArea.userID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                upiInfo=snapshot.getValue(UpiInfo.class);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
    private void setAddValues(ParkingArea parkingArea) {
        this.parkingArea=parkingArea;
        //placeText.setText(parkingArea.name);
    }
    private void showTimePicker(final TextView button) {
        TimePickerDialog.OnTimeSetListener timeSetListener= new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY,hour);
                calendar.set(Calendar.MINUTE,minute);
                calendar.set(Calendar.SECOND, 0);
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("hh:mm a");
                bookingSlot.endTime = bookingSlot.checkoutTime = calendar.getTime();
                if(bookingSlot.endTime.after(bookingSlot.startTime)){
                    button.setText(simpleDateFormat.format(calendar.getTime()));
                    bookingSlot.endTime = bookingSlot.checkoutTime = calendar.getTime();
                    calcRefreshAmount();
                }else{
                    bookingSlot.endTime = bookingSlot.checkoutTime = bookingSlot.startTime;
                    Toast.makeText(BookParkingArea.this,
                            "Please select a time after Present time!", Toast.LENGTH_SHORT).show();
                }
            }
        };
        TimePickerDialog timePickerDialog=new TimePickerDialog(BookParkingArea.this,timeSetListener,calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),false);
        timePickerDialog.show();
    }

    private void showDatePicker(final TextView button) {
        DatePickerDialog.OnDateSetListener dateSetListener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, final int date) {
                calendar.set(Calendar.YEAR,year);
                calendar.set(Calendar.MONTH,month);
                calendar.set(Calendar.DAY_OF_MONTH,date);
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd-MM-yyyy");
                button.setText(simpleDateFormat.format(calendar.getTime()));
                bookingSlot.endTime = bookingSlot.checkoutTime = calendar.getTime();
                calcRefreshAmount();
            }
        };
        DatePickerDialog datePickerDialog=new DatePickerDialog(BookParkingArea.this,dateSetListener,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }
    private void calcRefreshAmount() {
        bookingSlot.calcAmount(parkingArea);
        String amountStr=String.valueOf(bookingSlot.amount);
        amountText.setText(amountStr);
    }

}