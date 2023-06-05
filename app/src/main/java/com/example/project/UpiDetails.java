package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UpiDetails extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseDatabase db;

    AppCompatEditText upiIdText,upiNameText;
    Button bt_submit;

    UpiInfo upiInfo;
    UserDetails userObj;
    String userID;

    BasicUtils utils=new BasicUtils();

    AppConstants globalClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upi_details);

        initComponents();
        attachListeners();
        if(!utils.isNetworkAvailable(getApplication())){
            Toast.makeText(UpiDetails.this, "No Network Available!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();

    }

    private void initComponents() {
//        getSupportActionBar().setTitle("UPI Details");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);

        auth= FirebaseAuth.getInstance();
        db= FirebaseDatabase.getInstance();

        globalClass=(AppConstants)getApplicationContext();
        userID=auth.getCurrentUser().getUid();

        userObj=globalClass.getUserObj();

        upiIdText=findViewById(R.id.upiIdText);
        upiNameText=findViewById(R.id.upiNameText);
        bt_submit=findViewById(R.id.bt_submit);
    }

    private void attachListeners() {
        db.getReference().child("UpiInfo").child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                upiInfo=snapshot.getValue(UpiInfo.class);
                if(upiInfo!=null){
                    upiIdText.setText(upiInfo.upiId);
                    upiIdText.setSelection(upiIdText.getText().length());
                    upiNameText.setText(upiInfo.upiName);
                    upiNameText.setSelection(upiNameText.getText().length());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });


        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String upiId = upiIdText.getText().toString();
                String upiName = upiNameText.getText().toString();

                if(upiId.isEmpty() || upiName.isEmpty()){
                    Toast.makeText(UpiDetails.this, "Please fill all details!", Toast.LENGTH_SHORT).show();
                }else{
                    upiInfo=new UpiInfo(upiId,upiName);
                    db.getReference("UpiInfo").child(userID).setValue(upiInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(UpiDetails.this, "Success", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(UpiDetails.this, "Failed to add UPI details", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }
}