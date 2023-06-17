package com.example.project;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class Add extends Fragment {
    private FirebaseAuth auth;
    private FirebaseDatabase db;

    FloatingActionButton addLocationBtn;
    AppCompatEditText areaNameText,upiIdText,upiNameText,amount2Text,amount3Text,amount4Text,totalSlotsText;
    Button loadFromFile;

    FusedLocationProviderClient client;
    List<SlotNoInfo> slotNos = new ArrayList<>();
    List<String> slotNoString = new ArrayList<>();
    BasicUtils utils=new BasicUtils();
    AppConstants globalClass;
    UserDetails userObj;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_add, container, false);

        initComponents(root);
        attachListeners();

        if(!utils.isNetworkAvailable(getActivity().getApplication())){
            Toast.makeText(getActivity(), "No Network Available!", Toast.LENGTH_SHORT).show();
        }
        return root;
    }

    private void initComponents(View root) {
        client= LocationServices.getFusedLocationProviderClient(getActivity());

        auth= FirebaseAuth.getInstance();
        db= FirebaseDatabase.getInstance();

        areaNameText=root.findViewById(R.id.areaNameText);
        upiIdText=root.findViewById(R.id.upiIdText);
        upiNameText=root.findViewById(R.id.upiNameText);
        totalSlotsText=root.findViewById(R.id.totalSlotsText);
        amount2Text=root.findViewById(R.id.amount2Text);
        amount3Text=root.findViewById(R.id.amount3Text);
        amount4Text=root.findViewById(R.id.amount4Text);
        addLocationBtn=root.findViewById(R.id.addLocationBtn);
        globalClass=(AppConstants)getActivity().getApplicationContext();
        userObj=globalClass.getUserObj();
        db.getReference().child("ParkingAreas").orderByChild("userID").equalTo(auth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            ParkingArea parkingArea = dataSnapshot.getValue(ParkingArea.class);
                            String prepend = "Rs.";
                            areaNameText.setText(String.valueOf(parkingArea.name));
                            totalSlotsText.setText(String.valueOf(parkingArea.totalSlots));
                            amount2Text.setText(String.valueOf(parkingArea.amount2));
                            amount3Text.setText(String.valueOf(parkingArea.amount3));
                            amount4Text.setText(String.valueOf(parkingArea.amount4));
                            Log.e("DashboardOwnerFragment", "Fetch parking area");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });


    }

    private void attachListeners() {

        addLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                db.getReference().child("UpiInfo").child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UpiInfo  upiInfo=snapshot.getValue(UpiInfo.class);
                        if(upiInfo!=null){
                            upiIdText.setText(String.valueOf(upiInfo.upiId));

                            upiNameText.setText(String.valueOf(upiInfo.upiName));

                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
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
                    Toast.makeText(getActivity(), "Please enter all fields!", Toast.LENGTH_SHORT).show();
                }else if(!utils.isUpiIdValid(upiId)){
                    Toast.makeText(getActivity(), "Invalid UPI ID!", Toast.LENGTH_SHORT).show();
                }else {

                    final ParkingArea parkingArea = new ParkingArea(areaName/* globalLatLng.latitude, globalLatLng.longitude*/,auth.getCurrentUser().getUid(), Integer.parseInt(totalSlots), 0, Integer.parseInt(amount2), Integer.parseInt(amount3), Integer.parseInt(amount4), slotNos);
                    final UpiInfo upiInfo = new UpiInfo(upiId, upiName);
                    final String key=db.getReference("ParkingAreas").push().getKey();

                    db.getReference("ParkingAreas").child(parkingArea.name).setValue(parkingArea).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                db.getReference("UpiInfo").child(auth.getCurrentUser().getUid()).setValue(upiInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getActivity(), "Success", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getActivity(), MainOwner.class);
                                            intent.putExtra("FRAGMENT_NO", 0);
                                            startActivity(intent);
                                            //overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);  //slide from left to right
                                            //finish();
                                        } else {
                                            Toast.makeText(getActivity(), "Failed to add UPI details", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(getActivity(), "Failed to add extra details", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });


    }
}