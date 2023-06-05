package com.example.project;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;


public class Profile extends Fragment {

    LinearLayout personalDetailsBtn,changePasswordBtn,aboutMeBtn,logoutBtn,upiDetailsBtn;
    TextView nameText;
    UserDetails userObj;

    FirebaseAuth auth;
    BasicUtils utils=new BasicUtils();
    AppConstants globalClass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        initComponents(root);
        attachListeners();

        if(!utils.isNetworkAvailable(getActivity().getApplication())){
            Toast.makeText(getActivity(), "No Network Available!", Toast.LENGTH_SHORT).show();
        }

        return root;
    }

    private void initComponents(View root) {
        globalClass=(AppConstants)getActivity().getApplicationContext();
        userObj=globalClass.getUserObj();
        logoutBtn = root.findViewById(R.id.logoutBtn);
        nameText = root.findViewById(R.id.nameText);
        personalDetailsBtn = root.findViewById(R.id.personalDetailsBtn);
        changePasswordBtn = root.findViewById(R.id.changePasswordBtn);
        aboutMeBtn = root.findViewById(R.id.aboutMeBtn);
        upiDetailsBtn = root.findViewById(R.id.upiDetailsBtn);
        nameText.setText(userObj.name);

    }

    private void attachListeners() {
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                getActivity().stopService(new Intent(getActivity(), MyParkingService.class));

                Toast.makeText(getActivity(), "Logout Success", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), MainActivity.class));
                getActivity().finish();
            }
        });

        personalDetailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), PersonalDetails.class));

            }
        });

        upiDetailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), UpiDetails.class));

            }
        });

        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ChangePassword.class));

            }
        });

        aboutMeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), About.class));

            }
        });
    }
}