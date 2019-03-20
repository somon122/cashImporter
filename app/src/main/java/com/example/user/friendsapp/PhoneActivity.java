package com.example.user.friendsapp;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

public class PhoneActivity extends AppCompatActivity {

    CountryCodePicker countryCodePicker;
    EditText phoneNumberET;
    Button sentCodeButton;

    String countryCodeNumber;

    FirebaseDatabase database;
    DatabaseReference myRef;

    String deviceId;
    String id;
    String checkPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);


        countryCodePicker = findViewById(R.id.countryCodePicker_Id);
        phoneNumberET= findViewById(R.id.phoneNumber_Id);
        sentCodeButton= findViewById(R.id.sentCode_Id);

        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("message");

        //deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        //authCheck();
        //Toast.makeText(PhoneAuthActivity.this, id+"\n"+checkPhone, Toast.LENGTH_LONG).show();

        sentCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (haveNetwork()){
                    phoneAuthentication();
                }else {
                    Toast.makeText(PhoneActivity.this, "Net connection is Error", Toast.LENGTH_SHORT).show();
                }


            }
        });

    }

    //------ End of onCreate Method ----------






    private void phoneAuthentication(){
        countryCodeNumber = countryCodePicker.getFullNumberWithPlus();
        String phoneNumber = phoneNumberET.getText().toString().trim();

        if (phoneNumber.isEmpty() || phoneNumber.length()<10){

            phoneNumberET.setError("Please Enter the Correct Phone Number");
            phoneNumberET.requestFocus();
            return;
        }else {
            //String phoneCheck = phoneNumberET.getText().toString().trim();
            String number = countryCodeNumber+phoneNumber;
            //myRef.setValue(phoneNumber);
            Intent intent = new Intent(PhoneActivity.this,PhoneConfirmActivity.class);
            intent.putExtra("phoneNumber",number);
            startActivity(intent);

            Toast.makeText(PhoneActivity.this, " Welcome New User", Toast.LENGTH_SHORT).show();


        }


    }

    private boolean haveNetwork ()
    {
        boolean have_WiFi = false;
        boolean have_Mobile = false;

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

        for (NetworkInfo info : networkInfo){

            if (info.getTypeName().equalsIgnoreCase("WIFI"))
            {
                if (info.isConnected())
                {
                    have_WiFi = true;
                }
            }
            if (info.getTypeName().equalsIgnoreCase("MOBILE"))

            {
                if (info.isConnected())
                {
                    have_Mobile = true;
                }
            }

        }
        return have_WiFi || have_Mobile;

    }



}
