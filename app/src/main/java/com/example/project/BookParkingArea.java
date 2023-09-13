package com.example.project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;

public class BookParkingArea extends AppCompatActivity {
    Spinner numberPlateSpinner,areaSpinner;
    TextView wheelerText,amountText,entryDateText,entryTimeText,endDateText,endTimeText,avSlots;
    LinearLayout entryDate,entryTime,endDate,endTime;
    Calendar calendar;
    NotificationHelper mNotificationHelper;
    FirebaseAuth auth;
    FirebaseDatabase db;
    List<Integer> numberPlateWheeler = new ArrayList<Integer>();

    List<String> numberPlateNumber = new ArrayList<String>();
    List<String> AreaNameList = new ArrayList<String>();

    BookedSlots bookingSlot = new BookedSlots();

    UserDetails userObj;
    AppConstants globalClass;
    UPIPayment upiPayment=new UPIPayment();
    FloatingActionButton bookBtn;
    Button CalcAmt;
    UpiInfo upiInfo;
    public String AreaName;
    String name,userID;
    int totalSlots,occupiedSlots,availableSlots,amount2,amount3,amount4;
    Boolean endTimeFlag,startTimeFlag;
    public List<SlotNoInfo> slotNos = new ArrayList<>();
    DatabaseReference reference;
    BasicUtils utils=new BasicUtils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_parking_area);
        if(!utils.isNetworkAvailable(getApplication())){
            Toast.makeText(BookParkingArea.this, "No Network Available!", Toast.LENGTH_SHORT).show();
        }
        initComponents();
        addItemsOnAreaSpinner();
        addItemsOnNumberPlateSpinner();
        addListenerOnAreaSpinnerItemSelection();
        addListenerOnNumberPlateSpinnerItemSelection();
        attachListeners();
    }
    private void initComponents() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("ParkingAreas");
        globalClass=(AppConstants)getApplicationContext();
        userObj=globalClass.getUserObj();
        wheelerText = findViewById(R.id.wheelerText);
        numberPlateSpinner=findViewById(R.id.vehicleSelect);
        areaSpinner=findViewById(R.id.areaSelect);
        avSlots=findViewById(R.id.available);
        amountText = findViewById(R.id.amountText);
        entryDate=findViewById(R.id.startDate);
        entryTime=findViewById(R.id.startTime);
        entryDateText=findViewById(R.id.entryDateText);
        entryTimeText=findViewById(R.id.entryTimeText);
        endDate = findViewById(R.id.endDate);
        endTime = findViewById(R.id.endTime);
        endDateText = findViewById(R.id.endDateText);
        endTimeText = findViewById(R.id.endTimeText);
        CalcAmt=findViewById(R.id.CalcAmt);
        bookBtn=findViewById(R.id.bookBtn);
        mNotificationHelper=new NotificationHelper(this);
        calendar=new GregorianCalendar();
        bookingSlot.startTime=bookingSlot.endTime=bookingSlot.checkoutTime=calendar.getTime();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("hh:mm a");
        entryTimeText.setText(simpleDateFormat.format(bookingSlot.startTime));
        endTimeText.setText(simpleDateFormat.format(bookingSlot.endTime));
        simpleDateFormat=new SimpleDateFormat("dd-MM-yyyy");
        endDateText.setText(simpleDateFormat.format(bookingSlot.endTime));
        entryDateText.setText(simpleDateFormat.format(bookingSlot.endTime));
        bookingSlot.readNotification=0;
        bookingSlot.readBookedNotification=1;
        bookingSlot.hasPaid=0;
        bookingSlot.userID=auth.getCurrentUser().getUid();

    }
    private void addItemsOnAreaSpinner() {

        AreaNameList.add("Select an area");
        db.getReference().child("ParkingAreas").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ParkingArea area=dataSnapshot.getValue(ParkingArea.class);
                    AreaNameList.add(area.name);

                }
                ArrayAdapter<String> AreaAdapter
                        = new ArrayAdapter<String>(BookParkingArea.this,
                        android.R.layout.simple_spinner_item, AreaNameList);
                areaSpinner.setAdapter(AreaAdapter);
                AreaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addItemsOnNumberPlateSpinner() {
        numberPlateWheeler.add(0);
        numberPlateNumber.add("Select a vehicle");

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
                        ArrayAdapter<String> NumberPlateAdapter = new ArrayAdapter<String>(BookParkingArea.this,
                                android.R.layout.simple_spinner_item, numberPlateNumber);
                        numberPlateSpinner.setAdapter(NumberPlateAdapter);
                        NumberPlateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
    public void addListenerOnAreaSpinnerItemSelection(){
        areaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (position != 0){
                    AreaName=adapterView.getItemAtPosition(position).toString();
                    areaSpinner.setSelection(getIndex(areaSpinner,AreaName));
                    reference.child(AreaName).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                            if(datasnapshot.exists()){
                                ParkingArea area=datasnapshot.getValue(ParkingArea.class);
                                name=area.name;
                                userID= area.userID;
                                totalSlots=area.totalSlots;
                                occupiedSlots=area.occupiedSlots;
                                availableSlots=area.availableSlots;
                                amount2=area.amount2;
                                amount3=area.amount3;
                                amount4=area.amount4;
                                slotNos=area.slotNos;
                                if(availableSlots==0)
                                    Toast.makeText(BookParkingArea.this,"Please Select Another Area!",Toast.LENGTH_SHORT).show();
                                String availableSlot=String.valueOf(area.availableSlots);
                                avSlots.setText("Available Slots:"+availableSlot);
                                // amountText.setText(String.valueOf(amount4));
                            }
                            db.getReference().child("ParkingAreas").child(name)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            ParkingArea parkingArea = snapshot.getValue(ParkingArea.class);
                                            //setAddValues(parkingArea);
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

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    public void addListenerOnNumberPlateSpinnerItemSelection(){
        numberPlateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (position != 0) {
                    String numberPlate=adapterView.getItemAtPosition(position).toString();

                    bookingSlot.numberPlate = numberPlateNumber.get(position);
                    bookingSlot.wheelerType = numberPlateWheeler.get(position);
                    //               Toast.makeText(BookParkingArea.this, bookingSlot.numberPlate, Toast.LENGTH_SHORT).show();

                    numberPlateSpinner.setSelection(getIndex(numberPlateSpinner,numberPlate));
                    String wheelerTypeStr = String.valueOf(bookingSlot.wheelerType);
                    wheelerText.setText(wheelerTypeStr);

                }
            }
            @Override


            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    private int getIndex(Spinner spinner, String selectedText) {
        for(int i=0;i<spinner.getCount();i++){
            if(spinner.getItemAtPosition(i).toString().equals(selectedText)) {
                return i;
            }

        }
        return 0;
    }
    private void attachListeners() {
        updateUI();

        CalcAmt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParkingArea parkingArea=new ParkingArea(name,userID,totalSlots,occupiedSlots,amount2,amount3,amount4,slotNos);
                bookingSlot.calcAmount(parkingArea);
                String amountStr=String.valueOf(bookingSlot.amount);
                amountText.setText(amountStr);
            }
        });
        entryDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEntryDatePicker(entryDateText);
            }
        });
        entryTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEntryTimePicker(entryTimeText);
            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEndDatePicker(endDateText);
            }
        });
        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEndTimePicker(endTimeText);
            }
        });
        bookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(numberPlateSpinner.getSelectedItemPosition()==0){
                    Toast.makeText(BookParkingArea.this, "Please select a vehicle!", Toast.LENGTH_SHORT).show();
                }else if(areaSpinner.getSelectedItemPosition()==0){
                    Toast.makeText(BookParkingArea.this, "Please select an area!", Toast.LENGTH_SHORT).show();
                }
                else if(bookingSlot.endTime.equals(bookingSlot.startTime)){
                    Toast.makeText(BookParkingArea.this,
                            "Please set the end time!", Toast.LENGTH_SHORT).show();
                } else if (amountText.getText().toString().isEmpty()) {
                    Toast.makeText(BookParkingArea.this, "Please Click the Generate Amount Button to know your amount details", Toast.LENGTH_SHORT).show();
                } else if(!bookingSlot.timeDiffValid()){
                    Toast.makeText(BookParkingArea.this,
                            "Less time difference (<15 minutes)!", Toast.LENGTH_SHORT).show();
                }else if(bookingSlot.endTime.before(bookingSlot.startTime)){
                    Toast.makeText(BookParkingArea.this, "Please select an end time after start time", Toast.LENGTH_SHORT).show();
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(BookParkingArea.this);
                    builder.setCancelable(true);
                    builder.setTitle("Confirm Booking");
                    builder.setMessage("Confirm Booking for this area?");
                    builder.setPositiveButton("Confirm",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final ParkingArea parkingArea=new ParkingArea(name,userID,totalSlots,occupiedSlots,amount2,amount3,amount4,slotNos);
                                    bookingSlot.placeName=parkingArea.name;
                                    if(parkingArea.availableSlots>0) {
                                        parkingArea.allocateSpace();
                                        db.getReference("ParkingAreas").child(parkingArea.name).setValue(parkingArea);
                                        String note ="Payment for ".concat(bookingSlot.placeName).concat(" and number ").concat(bookingSlot.numberPlate);
                                        //      Boolean upi=upiPayment.payUsingUpi(String.valueOf(bookingSlot.amount), "mahak@oksbi", "Michael", note,BookParkingArea.this);
//                                        Boolean upi=upiPayment.payUsingUpi(String.valueOf(bookingSlot.amount), upiInfo.upiId, upiInfo.upiName, note,BookParkingAreaActivity.this);
                                        //                                       saveData();

                                        bookingSlot.notificationID=Math.abs((int)Calendar.getInstance().getTimeInMillis());
                                        final String key=db.getReference("BookedSlots").push().getKey();
                                        bookingSlot.slotNo=parkingArea.allocateSlot(bookingSlot.numberPlate);
                                        db.getReference("BookedSlots").child(key).setValue(bookingSlot).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    db.getReference("ParkingAreas").child(bookingSlot.placeName).setValue(parkingArea);
                                                    Toast.makeText(BookParkingArea.this,"Success",Toast.LENGTH_SHORT).show();
                                                    File file = new File(BookParkingArea.this.getExternalCacheDir(), File.separator + "invoice.pdf");
                                                    InvoiceGenerator invoiceGenerator=new InvoiceGenerator(bookingSlot,parkingArea,key,userObj,file);
                                                    invoiceGenerator.create();
                                                    invoiceGenerator.uploadFile(BookParkingArea.this,getApplication());
                                                    Intent intent = new Intent(BookParkingArea.this, MainNormal.class);
                                                    intent.putExtra("FRAGMENT_NO", 0);
                                                    startActivity(intent);
                                                    //overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);  //slide from left to right
                                                    finish();
                                                }else{
                                                    Toast.makeText(BookParkingArea.this,"Failed",Toast.LENGTH_SHORT).show();
                                                    parkingArea.deallocateSpace();
                                                    parkingArea.deallocateSlot(bookingSlot.slotNo);
                                                    db.getReference("ParkingAreas").child(bookingSlot.placeName).setValue(parkingArea);
                                                }
                                            }
                                        });

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

    }

    private void updateUI() {
        if(endTimeFlag && startTimeFlag){
            CalcAmt.setVisibility(View.VISIBLE);
            bookBtn.setVisibility(View.VISIBLE);
        }
    }

    private void showEndDatePicker(final TextView button) {
        DatePickerDialog.OnDateSetListener dateSetListener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, final int date) {
                calendar.set(Calendar.YEAR,year);
                calendar.set(Calendar.MONTH,month);
                calendar.set(Calendar.DAY_OF_MONTH,date);
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd-MM-yyyy");
                button.setText(simpleDateFormat.format(calendar.getTime()));
                bookingSlot.endTime = bookingSlot.checkoutTime = calendar.getTime();
                if(bookingSlot.endTime.before(bookingSlot.startTime)){
                    Toast.makeText(BookParkingArea.this,
                            "Please select a time after Entry Date!", Toast.LENGTH_SHORT).show();
                }
                //calcRefreshAmount();
            }
        };
        DatePickerDialog datePickerDialog=new DatePickerDialog(BookParkingArea.this,dateSetListener,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }
    private void showEntryDatePicker(final TextView button) {
        DatePickerDialog.OnDateSetListener dateSetListener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, final int date) {
                calendar.set(Calendar.YEAR,year);
                calendar.set(Calendar.MONTH,month);
                calendar.set(Calendar.DAY_OF_MONTH,date);
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd-MM-yyyy");
                button.setText(simpleDateFormat.format(calendar.getTime()));
                bookingSlot.startTime = calendar.getTime();
                //calcRefreshAmount();
            }
        };
        DatePickerDialog datePickerDialog=new DatePickerDialog(BookParkingArea.this,dateSetListener,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

//    private void calcRefreshAmount() {
//        bookingSlot.calcAmount(parkingArea);
//        String amountStr=String.valueOf(bookingSlot.amount);
//        amountText.setText(amountStr);
//    }

    private void showEndTimePicker(final TextView button) {
        TimePickerDialog.OnTimeSetListener timeSetListener= new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                endTimeFlag=true;
                calendar.set(Calendar.HOUR_OF_DAY,hour);
                calendar.set(Calendar.MINUTE,minute);
                calendar.set(Calendar.SECOND, 0);
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("hh:mm a");
                bookingSlot.endTime = bookingSlot.checkoutTime = calendar.getTime();
                if(bookingSlot.endTime.after(bookingSlot.startTime)){
                    button.setText(simpleDateFormat.format(calendar.getTime()));
                    bookingSlot.endTime = bookingSlot.checkoutTime = calendar.getTime();

                    //  calcRefreshAmount();
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
    private void showEntryTimePicker(final TextView button) {
        TimePickerDialog.OnTimeSetListener timeSetListener= new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                startTimeFlag=true;
                calendar.set(Calendar.HOUR_OF_DAY,hour);
                calendar.set(Calendar.MINUTE,minute);
                calendar.set(Calendar.SECOND, 0);
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("hh:mm a");
                bookingSlot.startTime= calendar.getTime();
                if(bookingSlot.startTime.before(calendar.getTime())){
                    //button.setText(simpleDateFormat.format(calendar.getTime()));
                    Toast.makeText(BookParkingArea.this,
                            "Please select a time after Present time!", Toast.LENGTH_SHORT).show();
                    // bookingSlot.endTime = bookingSlot.checkoutTime = calendar.getTime();
                    //  calcRefreshAmount();
                }
                else{
                    button.setText(simpleDateFormat.format(calendar.getTime()));
                }
            }
        };
        TimePickerDialog timePickerDialog=new TimePickerDialog(BookParkingArea.this,timeSetListener,calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),false);
        timePickerDialog.show();
    }

}