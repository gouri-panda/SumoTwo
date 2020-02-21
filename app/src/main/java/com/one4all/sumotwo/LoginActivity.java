package com.one4all.sumotwo;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    TextView textView3;
    ProgressBar progressBar;
    ImageView loginImage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBarLogin);
        progressBar.setVisibility(View.INVISIBLE);
        mEmailView =  findViewById(R.id.login_email);
        loginImage = findViewById(R.id.login_image);
        mPasswordView =  findViewById(R.id.login_password);

        loginImage.animate().scaleX(2f).scaleY(2f).setDuration(2000).start();


        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.integer.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        textView3 = findViewById(R.id.textView3);

        String text = "<font color=#cc0029>S</font><font color=#37BAF5>U</font><font color=#E000EE>M</font><font color=#BF0731>O</font>";
        textView3.setText(Html.fromHtml(text));
        textView3.setScaleX(2.5f);
        if (FirebaseAuth.getInstance().getUid() != null){
            Intent intent = new Intent(LoginActivity.this,LatestMessageActivity.class);
            startActivity(intent);
            finish();
        }


    }

    public void signInExistingUser(View v) {
        progressBar.setVisibility(View.VISIBLE);
        attemptLogin();

    }

    public void registerNewUser(View v) {
        Intent intent = new Intent(this, com.one4all.sumotwo.RegisterActivity.class);
        finish();
        startActivity(intent);
    }
//
   // attemptLogin() method
    private void attemptLogin() {
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        if (email.isEmpty() || password.isEmpty()) {
            if (email.isEmpty()){
                mEmailView.setError("Email Required");
                progressBar.setVisibility(View.GONE);
                return;
            }else if (password.isEmpty()){
                mPasswordView.setError("Password Required");
                progressBar.setVisibility(View.GONE);
                return;
            }
            progressBar.setVisibility(View.GONE);
            return;
        } else {
            Toast.makeText(this, "Login progress...", Toast.LENGTH_SHORT).show();


//        //FirebaseAuth to sign in with email and password
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        progressBar.setVisibility(View.GONE);
                        showError(Objects.requireNonNull(task.getException()).getMessage());
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Intent intent = new Intent(LoginActivity.this, LatestMessageActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                }
            });
        }
    }
//
//        show error Dialogue
    private  void showError(String messge) {
        new AlertDialog.Builder(this)
                .setTitle("Oops")
                .setMessage(messge)
                .setPositiveButton("ok", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }
//    }
}




