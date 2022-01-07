package com.example.socialbike.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.socialbike.utilities.ConnectedUser;
import com.example.socialbike.utilities.MyPreferences;
import com.example.socialbike.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashMap;

public class LogInActivity extends AppCompatActivity {

    private SignInButton GoogleLogInButton;
    private GoogleSignInOptions gso;
    private GoogleSignInClient mGoogleSignInClient;
    private Button logInButton, signUpButton;
    private int RC_SIGN_IN = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        GoogleLogInButton = findViewById(R.id.sign_in_button);
        logInButton = findViewById(R.id.LoginButton);
        signUpButton = findViewById(R.id.signupButton);

        initialize();
        startListening();
    }

    private void initialize() {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void startListening() {
        GoogleLogInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logIn();
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = ((EditText)findViewById(R.id.EmailAddress)).getText().toString();
                String password = ((EditText)findViewById(R.id.TextPassword)).getText().toString();
                signUpButton.setText("Please wait..");
                signUp(email, password);
            }
            });

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = ((EditText)findViewById(R.id.EmailAddress)).getText().toString();
                String password = ((EditText)findViewById(R.id.TextPassword)).getText().toString();
                logInButton.setText("please wait");
                login(email, password);

            }
        });
    }

    private void login(String email, String password) {
        MainActivity.mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            System.out.println("signInWithEmail:success");
                            checkAccount();
                        } else {
                            // If sign in fails, display a message to the user.
                            System.out.println("signInWithEmail:failure");

                        }
                    }
                });
    }


    private void signUp(String email, String password) {


        MainActivity.mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            System.out.println("Success!");
                            // FirebaseUser user = MainActivity.getCurrentUser();
                            checkAccount();

                        } else {
                            // If sign in fails, display a message to the user.

                            System.out.println("Sign up failed.");
                        }
                    }
                });
    }

    private void logIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            handleSignInResult(task);
        } else if(requestCode == 3313){
            int x = 4;
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            System.out.println("login successful. Hello " + account.getEmail());
            firebaseAuthWithGoogle(account.getIdToken());
           // getAccountDetails();

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.

            System.out.println("login unsuccessful, " +  e.getStatusCode());

          //  updateUI(null);
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        MainActivity.mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                    //        MainActivity.mAuth.getCurrentUser();
                            checkAccount();
                            System.out.println("registered ");
                        } else {
                        //    stopLoadingAnimation();
                       //     toast.showMsg(getContext(), "Could not sign in.");
                            System.out.println("ERROR CONNECT " + task.getException());
                        }
                    }
                });
    }

    private void checkAccount() {
        MainActivity.mDatabase.child("users").child(MainActivity.mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final HashMap<String, Object> dataMap = (HashMap<String, Object>) dataSnapshot.getValue();
                    String publicKey = String.valueOf(dataMap.get("userPublicKey"));
                    String isActiveAccount = String.valueOf(dataMap.get("activeAccount"));

                    if (isActiveAccount.equals("false")) {
                       // toast.showMsg(getContext(), "Could not log in");
                        }
                    else {
                        MainActivity.isUserConnected = true;
                        savePublicKeyOnDevice(publicKey);
                        ConnectedUser.setPublicKey(publicKey);
                        getNicknameFromDBAndSave();
                    }
                }
                else{
                    System.out.println("User is not connected");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("ERROR CONNECT " + databaseError.getDetails());
            }
        });

    }

    private void getNicknameFromDBAndSave() {
        MainActivity.mDatabase.child("public").child(ConnectedUser.getPublicKey()).child("profile").child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!  dataSnapshot.exists()) {
                    String nickname = (String) dataSnapshot.getValue();
                    ConnectedUser.setNickname(nickname);
                    saveNicknameOnDevice(nickname);
                    MainActivity.startChat();
                    refreshAndClose();
                } else {
                    openSetNicknameActivity();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                System.out.println("& " + databaseError.getDetails());

            }

        });
    }

    private void setRefresh(){
        Intent intent = new Intent();
        intent.putExtra("refresh", "true");
        setResult(Activity.RESULT_OK, intent);
    }

    private void refreshAndClose() {
        setRefresh();
        finish();
    }


    private void saveNicknameOnDevice(String nickname) {
        MyPreferences.setSharedPreference(LogInActivity.this, MyPreferences.USER_FOLDER, "nickname", nickname);
    }

    private void openSetNicknameActivity() {
        startActivityForResult(new Intent(this, WelcomeActivity.class), 3313);
    }

    private void savePublicKeyOnDevice(String publicKey) {
        MyPreferences.setSharedPreference(this, MyPreferences.USER_FOLDER, "user_public_key", publicKey);
    }

}