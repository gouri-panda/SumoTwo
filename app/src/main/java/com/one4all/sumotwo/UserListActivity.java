package com.one4all.sumotwo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class UserListActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    TextView textView;
    ProgressDialog progressDialog;
    ProgressBar progressBar;
    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> imageUrl = new ArrayList<>();
    private ArrayList<String> uid = new ArrayList<>();
    private RecyclerViewAdapter recyclerViewAdapter;
    private DatabaseReference fireBaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        textView = findViewById(R.id.user_name_from_user_list);
        getSupportActionBar().setTitle("Users");
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progressBar_2);
        progressBar.setVisibility(View.VISIBLE);

        fetchUser();

    }

    private void fetchUser() {
        String userUid = FirebaseAuth.getInstance().getUid();

        fireBaseReference = FirebaseDatabase.getInstance().getReference().child("/userList");
        fireBaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

//                GroupAdapter groupA = new GroupAdapter<ViewHolder>();
                 recyclerViewAdapter = new RecyclerViewAdapter(imageUrl,names,uid,UserListActivity.this);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Log.d("userList", snapshot.toString());
//                    progressDialog.dismiss();
                    progressBar.setVisibility(View.INVISIBLE);
                    for (DataSnapshot sn: snapshot.getChildren()) {
                        Users string = sn.getValue(Users.class);
                        if (!string.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                            imageUrl.add(string.getUri());
                            names.add(string.getMdisplayName());
                            uid.add(string.getUid());
                        }
                    }


                }

                recyclerView.setAdapter(recyclerViewAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(UserListActivity.this));



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT |ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                String uid = recyclerViewAdapter.getUid(viewHolder.getAdapterPosition());
                fireBaseReference.child(uid).setValue(new Users(FirebaseAuth.getInstance().getUid(), "No one", "gouri", "gourishanakr"));
                Toast.makeText(UserListActivity.this, "Swiped", Toast.LENGTH_LONG ).show();
                recyclerViewAdapter.notifyDataSetChanged();


            }
        }).attachToRecyclerView(recyclerView);


    }
}


