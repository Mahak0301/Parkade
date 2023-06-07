package com.example.project;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class DashboardOwner extends Fragment {
    LinearLayout expandCard, slotStatus, slot_individual_list_view, historyBtn;
    TextView areaName,availableText, occupiedText, price2Text, price3Text, price4Text, slotName, numberPlate;
    SlotNoInfo slotNoInfo;

    BasicUtils utils = new BasicUtils();

    boolean dataSet = false;

    FirebaseAuth auth;
    FirebaseDatabase db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_dashboard_owner, container, false);

        initComponents(root, inflater);
        attachListeners(inflater);

        return root;
    }

    private void initComponents(View root, LayoutInflater inflater) {
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        expandCard = root.findViewById(R.id.expandCard);
        areaName=expandCard.findViewById(R.id.areaNameText);
        availableText = expandCard.findViewById(R.id.availableText2);
        occupiedText = expandCard.findViewById(R.id.occupiedText);
        price2Text = expandCard.findViewById(R.id.price2Text);
        price3Text = expandCard.findViewById(R.id.price3Text);
        price4Text = expandCard.findViewById(R.id.price4Text);
        slotStatus = root.findViewById(R.id.slotStatus);
        historyBtn = root.findViewById(R.id.historyBtn);
    }

    private void attachListeners(LayoutInflater inflater) {
        historyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AreaHistory.class));
                //getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        db.getReference().child("ParkingAreas").orderByChild("userID").equalTo(auth.getCurrentUser().getUid())
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        ParkingArea parkingArea = snapshot.getValue(ParkingArea.class);
                        setDashboardValues(parkingArea, inflater);
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
        db.getReference().child("ParkingAreas").orderByChild("userID").equalTo(auth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            ParkingArea parkingArea = dataSnapshot.getValue(ParkingArea.class);
                            setDashboardValues(parkingArea, inflater);
                            Log.e("DashboardOwnerFragment", "Fetch parking area");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void setDashboardValues(ParkingArea parkingArea, LayoutInflater inflater) {
        String prepend = "Rs.";
        areaName.setText(String.valueOf(parkingArea.name));
        availableText.setText(String.valueOf(parkingArea.availableSlots));
        occupiedText.setText(String.valueOf(parkingArea.occupiedSlots));
        price2Text.setText(prepend.concat(String.valueOf(parkingArea.amount2).concat("/Hr")));
        price3Text.setText(prepend.concat(String.valueOf(parkingArea.amount3).concat("/Hr")));
        price4Text.setText(prepend.concat(String.valueOf(parkingArea.amount4).concat("/Hr")));
        if(((LinearLayout) slotStatus).getChildCount() > 0)
            ((LinearLayout) slotStatus).removeAllViews();
        for(int i=0;i<parkingArea.slotNos.size();i++){
            slot_individual_list_view = (LinearLayout)inflater.inflate(R.layout.include_slot_individual_list_view, null);
            slotName = (TextView)slot_individual_list_view.findViewById(R.id.slotName);
            numberPlate = (TextView)slot_individual_list_view.findViewById(R.id.numberPlate);
            slotNoInfo=parkingArea.slotNos.get(i);
            slotName.setText(slotNoInfo.name);
            numberPlate.setText(slotNoInfo.numberPlate);
            slotStatus.addView(slot_individual_list_view);
        }
    }
}