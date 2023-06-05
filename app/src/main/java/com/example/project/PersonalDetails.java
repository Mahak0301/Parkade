package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class PersonalDetails extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseDatabase db;

    AppCompatEditText nameText,phoneText,emailText,newEmailText,currentPasswordText;
    Button bt_submit,bt_submit_email;

    UserDetails userObj;
    String userID;

    BasicUtils utils=new BasicUtils();

    AppConstants globalClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_details);

        initComponents();
        attachListeners();
        if(!utils.isNetworkAvailable(getApplication())){
            Toast.makeText(PersonalDetails.this, "No Network Available!", Toast.LENGTH_SHORT).show();
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
//        getSupportActionBar().setTitle("Personal Details");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);

        auth= FirebaseAuth.getInstance();
        db= FirebaseDatabase.getInstance();

        globalClass=(AppConstants)getApplicationContext();
        userID=auth.getCurrentUser().getUid();

        userObj=globalClass.getUserObj();

        nameText=findViewById(R.id.nameText);
        phoneText=findViewById(R.id.phoneText);
        emailText=findViewById(R.id.emailText);
        bt_submit=findViewById(R.id.bt_submit);
        newEmailText=findViewById(R.id.newEmailText);
        currentPasswordText=findViewById(R.id.currentPasswordText);
        bt_submit_email=findViewById(R.id.bt_submit_email);

        nameText.setText(userObj.name);
        nameText.setSelection(nameText.getText().length());
        phoneText.setText(userObj.phone);
        phoneText.setSelection(phoneText.getText().length());
        emailText.setText(userObj.email);
        emailText.setSelection(emailText.getText().length());
    }

    private void attachListeners() {
        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userObj.name = nameText.getText().toString();
                userObj.phone = phoneText.getText().toString();
                if(userObj.name.isEmpty() || userObj.phone.isEmpty()){
                    Toast.makeText(PersonalDetails.this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                }else if(!utils.isNameValid(userObj.name)){
                    Toast.makeText(PersonalDetails.this,"Name is Invalid!",Toast.LENGTH_SHORT).show();
                }else if(!utils.isPhoneNoValid(userObj.phone)){
                    Toast.makeText(PersonalDetails.this,"Phone Number is Invalid!",Toast.LENGTH_SHORT).show();
                }else{
                    db.getReference("Users")
                            .child(userID)
                            .setValue(userObj).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(PersonalDetails.this, "Success", Toast.LENGTH_SHORT).show();
                                        finish();

                                    } else {
                                        Toast.makeText(PersonalDetails.this, "Failed!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });


        bt_submit_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String newEmail=newEmailText.getText().toString();
                final String email=emailText.getText().toString();
                if(newEmail.isEmpty() || currentPasswordText.getText().toString().isEmpty() || email.isEmpty()) {
                    Toast.makeText(PersonalDetails.this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                }else if(newEmail.equals(email)){
                    Toast.makeText(PersonalDetails.this, "Please enter a new Email ID!", Toast.LENGTH_SHORT).show();
                }else{
                    final FirebaseUser user = auth.getCurrentUser();
                    AuthCredential credential = EmailAuthProvider.getCredential(emailText.getText().toString(), currentPasswordText.getText().toString()); // Current Login Credentials \\
                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d(String.valueOf(PersonalDetails.this.getClass()), "User re-authenticated.");
                                    user.updateEmail(newEmail)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(PersonalDetails.this, "Email Updated! Please Login again!", Toast.LENGTH_SHORT).show();
                                                        FirebaseAuth.getInstance().signOut();
                                                        stopService(new Intent(PersonalDetails.this, MyParkingService.class));

                                                        startActivity(new Intent(PersonalDetails.this,MainActivity.class));
                                                        finish();
                                                        Log.d(String.valueOf(PersonalDetails.this.getClass()), "User email address updated.");
                                                    }else{
                                                        Toast.makeText(PersonalDetails.this, "Failed to update Email!", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            });
                }
            }
        });
    }
}