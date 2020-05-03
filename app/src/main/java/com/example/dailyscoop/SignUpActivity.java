package com.example.dailyscoop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.FirebaseAuthCredentialsProvider;

public class SignUpActivity extends AppCompatActivity {

    private EditText firstName, lastName, email, password, retypedPassword;
    private Button signUpBtn;
    private TextView loginTxt;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        setUpUIViews();

        // Set up the database
        firebaseAuth = FirebaseAuth.getInstance();


        // Set the onclick listener for the sign up button
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateEnteredFields()) {
                    // Upload the data to the database
                    String user_email = email.getText().toString().trim().toLowerCase();
                    String user_password = password.getText().toString().trim();

                    firebaseAuth.createUserWithEmailAndPassword(user_email, user_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                               sendEmailVerification();
                            } else {
                                Exception ex = task.getException();
                                if (ex instanceof FirebaseAuthWeakPasswordException) {
                                    Toast.makeText(SignUpActivity.this, "Failed: " + ((FirebaseAuthWeakPasswordException) ex).getReason(), Toast.LENGTH_SHORT).show();
                                } else if (ex instanceof FirebaseAuthInvalidCredentialsException) {
                                    Toast.makeText(SignUpActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                                } else if (ex instanceof FirebaseAuthUserCollisionException) {
                                    Toast.makeText(SignUpActivity.this, "User already exists with that email. Please sign in", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(SignUpActivity.this, "Sign Up Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });

                    User user = new User(user_email);
                    db.collection("users").add(user);
                }
            }
        });

        // Set the onclick listener to return to login screen
        loginTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            }
        });

        // Set the text observer for the password strength bar
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                caclulatePasswordStrength();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }

    private void caclulatePasswordStrength() {
        String pass = password.getText().toString().trim();
        if (pass.isEmpty()) {
            progressBar.setProgress(0);
            return;
        }

        Drawable progressDrawable = progressBar.getProgressDrawable().mutate();
        progressDrawable.setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
        progressBar.setProgressDrawable(progressDrawable);

        PasswordStrength passwordStrength = PasswordStrength.calculate(pass);
        switch (passwordStrength.msg) {
            case (R.string.weak): {
                progressBar.setProgress(25);
                break;
            }
            case (R.string.medium): {
                progressBar.setProgress(50);
                break;
            }
            case (R.string.strong): {
                progressBar.setProgress(75);
                break;
            }
            case (R.string.very_strong): {
                progressBar.setProgress(100);
                progressDrawable.setColorFilter(Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN);
                progressBar.setProgressDrawable(progressDrawable);
                break;
            }
            default:
                break;
        }
    }

    private void sendEmailVerification() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignUpActivity.this, "Verification Email Sent!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SignUpActivity.this, "Verification Email Failed to Send", Toast.LENGTH_SHORT).show();
                    }
                    firebaseAuth.signOut();
                    finish();
                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                }
            });
        }
    }

    private boolean validateEnteredFields() {
        // Get all the info in String form
        String first = firstName.getText().toString().trim().toLowerCase();
        String last = lastName.getText().toString().trim().toLowerCase();
        String emailAddress = email.getText().toString().trim().toLowerCase();
        String password1 = password.getText().toString().trim();
        String password2 = retypedPassword.getText().toString().trim();

        // Ensure all fields are filled
        if (first.isEmpty() || last.isEmpty() || emailAddress.isEmpty()
                || password1.isEmpty() || password2.isEmpty()) {
            // Pop up a toast to tell user to fill out all fields
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check that passwords match
        if (!password1.equals(password2)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void setUpUIViews() {
        firstName = findViewById(R.id.txt_firstname);
        lastName = findViewById(R.id.txt_lastname);
        email = findViewById(R.id.txt_email);
        password = findViewById(R.id.txt_password1);
        retypedPassword = findViewById(R.id.txt_password2);

        signUpBtn = findViewById(R.id.btn_register);

        loginTxt = findViewById(R.id.txtview_login);

        progressBar = findViewById(R.id.progress_passwordstrength);
    }

    public enum PasswordStrength {

        // we use some color in green tint =>
        //more secure is the password, more darker is the color associated
        WEAK(R.string.weak, Color.parseColor("#61ad85")),
        MEDIUM(R.string.medium, Color.parseColor("#4d8a6a")),
        STRONG(R.string.strong, Color.parseColor("#3a674f")),
        VERY_STRONG(R.string.very_strong, Color.parseColor("#264535"));

        public int msg;
        public int color;
        private static int MIN_LENGTH = 6;
        private static int MAX_LENGTH = 15;

        PasswordStrength(int msg, int color) {
            this.msg = msg;
            this.color = color;
        }

        public static PasswordStrength calculate(String password) {
            int score = 0;
            // boolean indicating if password has an upper case
            boolean upper = false;
            // boolean indicating if password has a lower case
            boolean lower = false;
            // boolean indicating if password has at least one digit
            boolean digit = false;
            // boolean indicating if password has a leat one special char
            boolean specialChar = false;

            for (int i = 0; i < password.length(); i++) {
                char c = password.charAt(i);

                if (!specialChar  &&  !Character.isLetterOrDigit(c)) {
                    score++;
                    specialChar = true;
                } else {
                    if (!digit  &&  Character.isDigit(c)) {
                        score++;
                        digit = true;
                    } else {
                        if (!upper || !lower) {
                            if (Character.isUpperCase(c)) {
                                upper = true;
                            } else {
                                lower = true;
                            }

                            if (upper && lower) {
                                score++;
                            }
                        }
                    }
                }
            }

            int length = password.length();

            if (length > MAX_LENGTH) {
                score++;
            } else if (length < MIN_LENGTH) {
                score = 0;
            }

            // return enum following the score
            switch(score) {
                case 0 : return WEAK;
                case 1 : return MEDIUM;
                case 2 : return STRONG;
                case 3 : return VERY_STRONG;
                default:
            }

            return VERY_STRONG;
        }
    }
}
