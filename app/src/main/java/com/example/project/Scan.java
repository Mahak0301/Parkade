package com.example.project;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class Scan extends Fragment implements NumberPlatePopUp.NumberPlatePopUpListener{
    FloatingActionButton fab_textBtn,fab_add;
    TextView empty_view;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    boolean rotate = false;

    BasicUtils utils=new BasicUtils();

    FirebaseAuth auth;
    FirebaseDatabase db;

    View lyt_textBtn, back_drop;

    Map<String, NumberPlate> numberPlatesList = new HashMap<String, NumberPlate>();
    List<String> keys = new ArrayList<String>();
    Map<String, NumberPlate> treeMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_scan, container, false);

        initComponents(root);
        attachListeners(root);
        if(!utils.isNetworkAvailable(getActivity().getApplication())){
            Toast.makeText(getActivity(), "No Network Available!", Toast.LENGTH_SHORT).show();
        }

        return root;    }
    private void initComponents(View root){
        fab_textBtn = (FloatingActionButton) root.findViewById(R.id.fab_textBtn);
        fab_add = (FloatingActionButton) root.findViewById(R.id.fab_add);
        back_drop = root.findViewById(R.id.back_drop);
        empty_view = root.findViewById(R.id.empty_view);
        lyt_textBtn = root.findViewById(R.id.lyt_textBtn);
        ViewAnimation.initShowOut(lyt_textBtn);
        back_drop.setVisibility(View.GONE);
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFabMode(v);
            }
        });

        back_drop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFabMode(fab_add);
            }
        });

        fab_textBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFabMode(fab_add);
                Bundle args = new Bundle();
                args.putString("numberPlate", "");
                NumberPlatePopUp numberPlateDialog = new NumberPlatePopUp();
               // numberPlateDialog.setTargetFragment(Scan.this, AppConstants.NUMBER_PLATE_POPUP_REQUEST_CODE);
                numberPlateDialog.setArguments(args);
                numberPlateDialog.show(getParentFragmentManager(), "exampledialog");
            }
        });

        recyclerView = (RecyclerView) root.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);


        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
    }
    private void attachListeners(View root){
        db.getReference().child("NumberPlates").orderByChild("userID_isDeletedQuery").equalTo(auth.getCurrentUser().getUid()+"_0")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        SimpleToDeleteCallback itemTouchHelperCallback=new SimpleToDeleteCallback(getActivity()) {
                            @Override
                            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                                return false;
                            }

                            @Override
                            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                                final int position=viewHolder.getAdapterPosition();
                                final String data = keys.get(position);
                                Snackbar snackbar = Snackbar
                                        .make(recyclerView, "Number Plate Removed", Snackbar.LENGTH_LONG)
                                        .setAction("UNDO", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                keys.add(position, data);
                                                mAdapter.notifyItemInserted(position);
                                                recyclerView.scrollToPosition(position);
                                                db.getReference().child("NumberPlates").child(data).child("userID_isDeletedQuery")
                                                        .setValue(auth.getCurrentUser().getUid()+"_0");
                                                db.getReference().child("NumberPlates").child(data).child("isDeleted")
                                                        .setValue(0);
                                            }
                                        });
                                snackbar.show();
                                keys.remove(position);
                                mAdapter.notifyItemRemoved(position);
                                db.getReference().child("NumberPlates").child(data).child("userID_isDeletedQuery")
                                        .setValue(auth.getCurrentUser().getUid()+"_1");
                                db.getReference().child("NumberPlates").child(data).child("isDeleted")
                                        .setValue(1);
                                checkRecyclerViewIsEmpty();
                            }
                        };
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            NumberPlate numberPlate = dataSnapshot.getValue(NumberPlate.class);
                            numberPlatesList.put(dataSnapshot.getKey(),numberPlate);
                            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
                            itemTouchHelper.attachToRecyclerView(recyclerView);
                        }
                        treeMap = new TreeMap<String, NumberPlate>(numberPlatesList);
                        keys.addAll(treeMap.keySet());
                        mAdapter = new NumberPlateAdapter(treeMap,keys);
                        recyclerView.setAdapter(mAdapter);
                        checkRecyclerViewIsEmpty();
                        Log.d(String.valueOf(getActivity().getClass()),"Scan"+String.valueOf(numberPlatesList));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

    }

    private void checkRecyclerViewIsEmpty() {
        if(keys.isEmpty()){
            recyclerView.setVisibility(View.GONE);
            empty_view.setVisibility(View.VISIBLE);
        }else{
            recyclerView.setVisibility(View.VISIBLE);
            empty_view.setVisibility(View.GONE);
        }
    }
    private void toggleFabMode(View v) {
        rotate = ViewAnimation.rotateFab(v, !rotate);
        if (rotate) {

            ViewAnimation.showIn(lyt_textBtn);
            back_drop.setVisibility(View.VISIBLE);
        } else {

            ViewAnimation.showOut(lyt_textBtn);
            back_drop.setVisibility(View.GONE);
        }
    }
    private void saveData(String vehicleNumber,int wheelerType) {
        final NumberPlate numberPlate = new NumberPlate(vehicleNumber,wheelerType,0,auth.getCurrentUser().getUid(),auth.getCurrentUser().getUid()+"_0");
        final String key=db.getReference("NumberPlates").push().getKey();
        db.getReference("NumberPlates")
                .child(key)
                .setValue(numberPlate).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Success", Toast.LENGTH_SHORT).show();
                            numberPlatesList.put(key,numberPlate);
                            treeMap = new TreeMap<String, NumberPlate>(numberPlatesList);
                            keys.add(key);
                            mAdapter = new NumberPlateAdapter(treeMap,keys);
                            recyclerView.setAdapter(mAdapter);
                            checkRecyclerViewIsEmpty();
                        } else {
                            Toast.makeText(getActivity(), "Failed to add extra details", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}