package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.acl.Owner;

public class MainActivity extends AppCompatActivity {
    EditText email,password;
    ProgressBar progressBar;
    FirebaseAuth AuthProfile;
    TextView forgotPasswordText;
    TextView registerSwitchText;
    FirebaseDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        email = findViewById(R.id.emailField);
        password = findViewById(R.id.passwordField);
        registerSwitchText = findViewById(R.id.registerSwitchText);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        progressBar=findViewById(R.id.progressBar);

        AuthProfile=FirebaseAuth.getInstance();
        db=FirebaseDatabase.getInstance();
        Button login=findViewById(R.id.loginBtn);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textEmail=email.getText().toString();
                String textpwd=password.getText().toString();
                if(TextUtils.isEmpty(textEmail)){
                    Toast.makeText(MainActivity.this,"Please enter your E-Mail",Toast.LENGTH_SHORT).show();
                    email.setError("E-mail is required");
                    email.requestFocus();
                }else if(TextUtils.isEmpty(textpwd)){
                    Toast.makeText(MainActivity.this, "Please Enter your Password", Toast.LENGTH_SHORT).show();
                    password.setError("Password is required");
                    password.requestFocus();
                }else{
                    progressBar.setVisibility(View.VISIBLE);
                     loginUser(textEmail,textpwd);
                }
            }
        });
        registerSwitchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String passEmail=email.getText().toString();
                Intent intent=new Intent(MainActivity.this, Register.class);
                if(!passEmail.isEmpty()){
                    intent.putExtra("EMAIL",passEmail);
                    startActivity(intent);
                }
                else{
                    startActivity(intent);
                }
                finish();
            }
        });
        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String passEmail=email.getText().toString();
                Intent intent=new Intent(MainActivity.this, ForgotPassword.class);
                if(!passEmail.isEmpty()){
                    intent.putExtra("EMAIL",passEmail);
                    startActivity(intent);
                }else{
                    startActivity(intent);
                }
            }
        });

    }

    private void loginUser(String email, String pwd) {
        final AppConstants globalClass= (AppConstants) getApplicationContext();
        AuthProfile.signInWithEmailAndPassword(email,pwd).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    db.getReference("Users").child(AuthProfile.getCurrentUser().getUid()).child("email").setValue(AuthProfile.getCurrentUser().getEmail());
                    db.getReference().child("Users").child(AuthProfile.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UserDetails userObj=snapshot.getValue(UserDetails.class);
                            globalClass.setUserObj(userObj);
                            Toast.makeText(MainActivity.this, "You are Logged-In now", Toast.LENGTH_SHORT).show();

                            Intent intent;
                            if(userObj.UserType==2)
                                intent=new Intent(MainActivity.this, MainOwner.class);
                            else
                                intent=new Intent(MainActivity.this, MainNormal.class);
                            intent.putExtra("FRAGMENT_NO", 0);
                            try{ progressBar.setVisibility(View.GONE);
                            }catch (Exception e){ e.printStackTrace();}
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            try{ progressBar.setVisibility(View.GONE);

                            }catch (Exception e){ e.printStackTrace();}
                        }
                    });
                }else{
                    Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

                }
                progressBar.setVisibility(View.GONE);
            }
        });
      }
    

    }
