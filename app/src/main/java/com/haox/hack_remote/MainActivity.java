package com.haox.hack_remote;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.karan.churi.PermissionManager.PermissionManager;

import java.util.ArrayList;
import java.util.Map;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends AppCompatActivity {

    private PermissionManager permissionManager;
    private TextView tv;
    Map<String, ?> Items;

    private FirebaseAuth mAuth;
    private DatabaseReference storeUserDefaultDataRefernce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //For Runtime Permissions
        permissionManager = new PermissionManager() {
        };
        permissionManager.checkAndRequestPermissions(this);

        tv = findViewById(R.id.Image_Url);

        ConnectivityManager mgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = mgr.getActiveNetworkInfo();

        if (netInfo != null) {
            if (netInfo.isConnected()) {
                //Firebase login code
                mAuth = FirebaseAuth.getInstance();

                tv.setText(android.os.Build.MODEL);

                //UserLogin Check
                SharedPreferences sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE);
                Items = sharedPreferences.getAll();
                if (!Items.isEmpty()) {
                    Toast.makeText(this, "Saved in preference already", Toast.LENGTH_SHORT).show();
                    Login(sharedPreferences.getString("email", null), "lame123");
                } else {
                    phoneDetails();
                }
            } else {
                //No internet
            }
        } else {
            //No internet
            Toast.makeText(this, "Internet is off please turned on pocket data or WIFI", Toast.LENGTH_SHORT).show();

        }


    }

    public void savedUsers(String email) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.apply();
        Toast.makeText(getApplicationContext(), "Saved UserData !!", Toast.LENGTH_SHORT).show();
    }

    /*@Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){        if(currentUser == null){


        }
    }
*/
    public void RegisterAccount(final String name, final String email, final String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String current_UserId = mAuth.getCurrentUser().getUid();
                            storeUserDefaultDataRefernce = FirebaseDatabase.getInstance().getReference().child("Users").child(current_UserId);
                            storeUserDefaultDataRefernce.child("User_Email").setValue(email);
                            storeUserDefaultDataRefernce.child("User_Name").setValue(name)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Login(email, password);
                                                Toast.makeText(getApplicationContext(), "Successfully Register on Firebase", Toast.LENGTH_LONG).show();
                                                savedUsers(email);
                                            }
                                        }
                                    });

                        } else {
                            Toast.makeText(getApplicationContext(), "Error Occured", Toast.LENGTH_LONG).show();

                        }
                    }
                });
    }

    public void phoneDetails() {
        AccountManager am = AccountManager.get(this);
        Account[] accounts = am.getAccounts();
        ArrayList<String> strings = new ArrayList<>();

        for (Account ac : accounts) {
            String actype = ac.type;
            // Take your time to look at all available accounts
            if (actype.equals("com.google")) {
                String phoneNumber = ac.name;
                strings.add("Accounts : " + phoneNumber + "\nAccount type:  " + actype);
                tv.setText(strings.toString());
                RegisterAccount(Build.MODEL, phoneNumber, "lame123");
                return;
            }
        }

        if (strings.size() == 0) {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            tv.setText(telephonyManager.getDeviceId());
            RegisterAccount(Build.MODEL, telephonyManager.getDeviceId(), "lame123");
        }
    }

    public void Login(String email, String pass) {
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Sucessfully login", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Facing problem to get values from shared preference objects", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


}
