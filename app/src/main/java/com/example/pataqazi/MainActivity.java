package com.example.pataqazi;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;


import androidx.annotation.NonNull;

import android.content.Intent;
import android.widget.Toast;

//import com.EmployerMapActivity;
//import com.LoginActivity;
//import com.ProfessionalMapActivity;
//import com.ProfessionalRegisterActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
//import com.onAppKill;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth.AuthStateListener fireBaseAuthListener;
    DatabaseReference userDatabaseRef;
    private FirebaseAuth mAuth;
    boolean loggedIn=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //startService(new Intent(MainActivity.this, onAppKilled.class));

        mAuth = FirebaseAuth.getInstance();
        fireBaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                loggedIn = user != null;
            }
        };

        Thread background = new Thread() {

            public void run() {
                try {

                    sleep(3*1000);

                    if (loggedIn == true){


                        userDatabaseRef = FirebaseDatabase.getInstance().getReference("users");
                        String userId = mAuth.getUid();

                        userDatabaseRef.child(userId)
                                .addValueEventListener(new ValueEventListener() {

                                    String accountType;
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {

                                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                                            if (map.get("type") != null) {

                                                accountType = map.get("type").toString();

                                            }


                                            if (accountType.equals("employer")) {

                                                Toast.makeText(getBaseContext(),"Welcome Employer",Toast.LENGTH_SHORT).show();

                                                Intent intent = new Intent(getBaseContext(), EmployerMapActivity.class);
                                                startActivity(intent);
                                                finish();
                                                return;


                                            } else if (accountType.equals("professional")) {
                                                Toast.makeText(getBaseContext(),"Welcome professional",Toast.LENGTH_SHORT).show();

                                                Intent intent = new Intent(getBaseContext(), ProfessionalMapActivity.class);
                                                startActivity(intent);
                                                finish();
                                                return;



                                            }
                                        }else {

                                            Toast.makeText(getBaseContext(),"Account not found!!",Toast.LENGTH_SHORT).show();

                                            FirebaseAuth.getInstance().signOut();
                                            Intent logout=new Intent(getBaseContext(), LoginActivity.class);
                                            logout.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(logout);
                                            Toast.makeText(getBaseContext(),"Login Again!!",Toast.LENGTH_SHORT).show();
                                        }
                                    }


                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {


                                    }
                                });


                    }else {

                        Intent goToProfessionalRegisterActivity = new Intent(getBaseContext(), ProfessionalRegisterActivity.class);
                        startActivity(goToProfessionalRegisterActivity);
                        finish();
                    }


                } catch (Exception e) {

                }
            }
        };

        mAuth.addAuthStateListener(fireBaseAuthListener);
        background.start();

    }


    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(fireBaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(fireBaseAuthListener);
    }


}