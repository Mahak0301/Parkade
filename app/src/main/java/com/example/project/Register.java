package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.jar.Attributes;

public class Register extends AppCompatActivity {
    private EditText name,phone,email,password;
    private ProgressBar progressBar;
    private RadioGroup UserTypes;
    private int userType;
    FirebaseAuth auth;
    DatabaseReference reference;
    TextView Signin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        name=findViewById(R.id.nameField);
        phone=findViewById(R.id.contactNoField);
        email=findViewById(R.id.emailField);
        password=findViewById(R.id.passwordField);
        UserTypes=findViewById(R.id.userTypes);
        Signin = findViewById(R.id.loginSwitchText);
        progressBar=findViewById(R.id.progressBar3);
        Button RegisterBtn= findViewById(R.id.registerBtn);
        RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selected=UserTypes.getCheckedRadioButtonId();

                String NameText=name.getText().toString();
                String PhoneText=phone.getText().toString();
                String EmailText=email.getText().toString();
                String PasswordText=password.getText().toString();
                if(TextUtils.isEmpty(NameText)){
                    Toast.makeText(Register.this,"Please enter your name",Toast.LENGTH_SHORT).show();
                    name.setError("Name is required");
                    name.requestFocus();
                }else if(TextUtils.isEmpty(EmailText)) {
                    Toast.makeText(Register.this, "Please enter your E-mail ID", Toast.LENGTH_SHORT).show();
                    email.setError("Name is required");
                   email.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(EmailText).matches()) {
                    Toast.makeText(Register.this, "Please enter valid E-mail ID", Toast.LENGTH_SHORT).show();
                    email.setError("Valid E-mail ID required");
                    email.requestFocus();
                } else if (UserTypes.getCheckedRadioButtonId()==-1) {
                    Toast.makeText(Register.this, "Please select a User Type", Toast.LENGTH_SHORT).show();
                    UserTypes.requestFocus();
                } else if(TextUtils.isEmpty(PhoneText)){
                    Toast.makeText(Register.this,"Please enter your Contact Number",Toast.LENGTH_SHORT).show();
                    phone.setError("Contact Number is required");
                    phone.requestFocus();
                }else if(PhoneText.length()!=10){
                    Toast.makeText(Register.this,"Please enter valid Contact Number",Toast.LENGTH_SHORT).show();
                    phone.setError("Valid Contact Number is required");
                    phone.requestFocus();
                } else if (TextUtils.isEmpty(PasswordText)) {
                    Toast.makeText(Register.this,"Please enter a password",Toast.LENGTH_SHORT).show();
                    password.setError("Password is required");
                    password.requestFocus();
                }else if(PasswordText.length()<6){
                    Toast.makeText(Register.this,"Password should be atleast 6 digits",Toast.LENGTH_SHORT).show();
                    password.setError("Password too weak");
                    password.requestFocus();

                }else{
                    if(selected==R.id.normalType)
                        userType=3;
                    else
                        userType=2;
                    progressBar.setVisibility(View.VISIBLE);
                    registerUser(NameText,PhoneText,EmailText,PasswordText,userType);
                }
            }

        });
        Signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Register.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void registerUser(String nameText, String phoneText, String emailText, String passwordText, int userType) {
        final AppConstants globalClass=(AppConstants) getApplicationContext();
        auth=FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(emailText,passwordText).addOnCompleteListener(Register.this,new OnCompleteListener<AuthResult>() {

        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            if(task.isSuccessful()){

                FirebaseUser firebaseUser=auth.getCurrentUser();

                UserDetails writeUserDetails=new UserDetails(nameText,phoneText,emailText,userType);
                globalClass.setUserObj(writeUserDetails);
                reference=FirebaseDatabase.getInstance().getReference("Users");
                reference.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            //firebaseUser.sendEmailVerification();
                            Toast.makeText(Register.this,"User registered Successfully, Please verify your E-mail",Toast.LENGTH_LONG).show();
                            firebaseUser.sendEmailVerification().addOnCompleteListener(Register.this, new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Register.this, "Verification email sent to " + firebaseUser.getEmail()+"!", Toast.LENGTH_SHORT).show();
                                        FirebaseAuth.getInstance().signOut();
                                    } else {
                                        Toast.makeText(Register.this, "Failed to send verification email!", Toast.LENGTH_SHORT).show();
                                        FirebaseAuth.getInstance().signOut();
                                    }
                                }
                            });
                            Intent intent= new Intent(Register.this,MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                       else{
                            Toast.makeText(Register.this,"User registered failed",Toast.LENGTH_LONG).show();

                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });

            }
        }
    });
    }


}
