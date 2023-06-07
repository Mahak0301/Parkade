package com.example.project;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class DashboardNormal extends Fragment {
    FirebaseAuth auth;
    FirebaseDatabase db;
    BasicUtils utils=new BasicUtils();
    TextView empty_view;

    Button bookButton;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    List<BookedSlotKey> bookedSlotKeyList=new ArrayList<BookedSlotKey>();

    AppConstants globalClass;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View root=inflater.inflate(R.layout.fragment_dashboard_normal, container, false);
        initComponents(root);
        attachListeners();
        if(!utils.isNetworkAvailable(getActivity().getApplication())){
            Toast.makeText(getActivity(), "No Network Available!", Toast.LENGTH_SHORT).show();
        }
        return root;
    }

    private void attachListeners() {

        bookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), BookParkingArea.class));
            }
        });

        db.getReference().child("BookedSlots").orderByChild("userID").equalTo(auth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            BookedSlots bookedSlot = dataSnapshot.getValue(BookedSlots.class);
                            BookedSlotKey bookedSlotKey=new BookedSlotKey(bookedSlot,dataSnapshot.getKey());
                            bookedSlotKeyList.add(bookedSlotKey);
                        }
                        mAdapter = new BookingHistoryAdapter(bookedSlotKeyList);
                        recyclerView.setAdapter(mAdapter);
                        if(bookedSlotKeyList.isEmpty()){
                            recyclerView.setVisibility(View.GONE);
                            empty_view.setVisibility(View.VISIBLE);
                        }else{
                            recyclerView.setVisibility(View.VISIBLE);
                            empty_view.setVisibility(View.GONE);
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void initComponents(View root) {
        globalClass=(AppConstants)getActivity().getApplicationContext();

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

//        getSupportActionBar().setTitle("Booking History");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);

        empty_view=root.findViewById(R.id.empty_view);
        recyclerView = (RecyclerView) root.findViewById(R.id.user_history_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        bookButton= root.findViewById(R.id.book_button);
    }
}