package com.one4all.sumotwo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    public static final String CHAT_PREFS = "ChatPrefs";
    public static final String DISPLAY_NAME_KEY = "username";


    // UI references.
    private AutoCompleteTextView mEmailView;
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private EditText mConfirmPasswordView;
    private CircleImageView circleImageView;
    ProgressDialog progressDialog;

    // Firebase instance variables
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    Button choosePhoto;
    Uri uri;
    String imageLink;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            try {
                uri = data.getData();
                Bitmap bit = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                BitmapDrawable bitmapDrawable1 = new BitmapDrawable(bit);
                circleImageView.setImageBitmap(bit);
                choosePhoto.setAlpha(0f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initializeReference();
        choosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 0);
            }
        });
        // Keyboard sign in action
        mConfirmPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.integer.register_form_finished || id == EditorInfo.IME_NULL) {
                    attemptRegistration();
                    return true;
                }
                return false;
            }
        });
        firebaseAuth = FirebaseAuth.getInstance();


    }

    // Executed when Sign Up button is pressed.
    public void signUp(View v) {
        attemptRegistration();
    }

    /**
     * Checks if the user registration details is correct or not
     */
    private void attemptRegistration() {

        // Reset errors displayed in the form.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        progressDialog.setTitle("Please wait!!");
        progressDialog.setMessage("Account is creating");
        progressDialog.show();

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the userListFragment entered one.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // TODO: Call create FirebaseUser() here
            createFireBaseUser();

        }
    }

    private boolean isEmailValid(String email) {
        // You can add more checking logic here.
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            return true;
        }
        return false;
    }

    private boolean isPasswordValid(String password) {
        //Check  confirm password
        String confiremPassword = mConfirmPasswordView.getText().toString();

        return password.equals(confiremPassword) && password.length() > 5;
    }

    /**
     * Creates a firebase user account
     */
    public void createFireBaseUser() {
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d("sumo", "sumo created");

                if (!task.isSuccessful()) {
                    if (task.getException() != null) {
                        String message = task.getException().getMessage();
                        showErrorDialog(message);
                    }
                } else {
                    uploadImageToFirebaseStorage();
                    saveName();
                    progressDialog.dismiss();
                    Intent intent = new Intent(RegisterActivity.this, LatestMessageActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });
    }

    // Save the display name to Shared Preferences
    private void saveName() {
        String name = mUsernameView.getText().toString();
        SharedPreferences sharedPreferences = getSharedPreferences(CHAT_PREFS, MODE_PRIVATE);
        sharedPreferences.edit().putString(DISPLAY_NAME_KEY, name).apply();
    }

    public void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Oops")
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        progressDialog.dismiss();
                    }
                }).show();
    }

    /**
     * Upload image to firebase and saves the user information
     */
    public void uploadImageToFirebaseStorage() {
        /*
         If the user doesn't want to upload image choose default image i.e sumo logo
         */
        if (uri == null) {
            uri = Uri.parse("android.resource://com.one4all.sumotwo/" + R.drawable.sumo1);
        }
        String fileName = UUID.randomUUID().toString();
        final StorageReference firebaseStorage = FirebaseStorage.getInstance().getReference("/images/" + fileName);


        firebaseStorage.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> downloadUri = taskSnapshot.getStorage().getDownloadUrl();
                downloadUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        imageLink = uri.toString();
                        Log.d(TAG, "onComplete: photo was successfully completed");
                        Log.d("name", uri.toString());
                        Users fireBaseUserList = new Users(firebaseAuth.getUid(), mUsernameView.getText().toString(), mEmailView.getText().toString(), imageLink);
                        String uid = FirebaseAuth.getInstance().getUid();
                        databaseReference.child("userList/" + uid).push().setValue(fireBaseUserList).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "onComplete: Account successfully created");
                                } else {
                                    Log.d(TAG, "onComplete: There was an error to create Account");
                                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                                }
                            }
                        });
                    }
                });
            }
        });
    }
    private void initializeReference(){
        progressDialog = new ProgressDialog(RegisterActivity.this);
        FirebaseApp.initializeApp(getApplicationContext());

        mEmailView = findViewById(R.id.register_email);
        mPasswordView = findViewById(R.id.register_password);
        mConfirmPasswordView = findViewById(R.id.register_confirm_password);
        mUsernameView = findViewById(R.id.register_username);
        choosePhoto = findViewById(R.id.choose_photo);
        circleImageView = findViewById(R.id.profile_image);
        getSupportActionBar().setTitle("Enter your details");
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }
}
















