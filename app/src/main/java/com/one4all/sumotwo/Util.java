package com.one4all.sumotwo;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Util {
    private static final String TAG = "Util";

    public static void getUsersDetailsFromFireBase(final Context context) {
        String userUid = FirebaseAuth.getInstance().getUid();
        FirebaseDatabase.getInstance().getReference().child("userList/" + userUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot usersList : dataSnapshot.getChildren()) {
                    Users users = usersList.getValue(Users.class);
                    String userName = users.getMdisplayName();
                    Log.d(TAG, "onDataChange: " + userName);
                    String imageUri = users.getUri();
                    Log.d(TAG, "onDataChange: imageUri" + imageUri);
                    String userEmail = users.getEmailAddress();
                    SharedPreferences.Editor editorUserDetails = context.getSharedPreferences("userDetails", Context.MODE_PRIVATE).edit();
                    editorUserDetails.clear();
                    editorUserDetails.putString("userName", userName);
                    editorUserDetails.putString("imageUri", imageUri);
                    editorUserDetails.putString("userEmail", userEmail).commit();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: database error" + databaseError.getDetails());
                Log.d(TAG, "onCancelled: database message" + databaseError.getMessage());

            }
        });
    }

    public static String getUserName(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName", "stranger");
        return userName;
    }

    public static String getUserEmail(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("userEmail", "stranger");
        return userEmail;
    }

    public static String getUserImageLink(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        String userImageLink = sharedPreferences.getString("imageUri", "default");
        return userImageLink;
    }
}
