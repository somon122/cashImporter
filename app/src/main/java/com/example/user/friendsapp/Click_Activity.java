package com.example.user.friendsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Click_Activity extends AppCompatActivity {


    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private String value;
    private int clickScore = 0;
    private InterstitialAd mInterstitialAd;
    private Button clickButton;
    private ProgressDialog progressDialog;


    CountDownTimer countDownTimer;
    long timeLeft = 50000;
    boolean timeRunning;
    String timeText;

    FirebaseAuth auth;
    FirebaseUser user;

    ClickBalanceControl clickBalanceControl;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_);



        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Balance");

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        clickBalanceControl = new ClickBalanceControl();

        clickButton = findViewById(R.id.completeClick);
        progressDialog = new ProgressDialog(this);


        progressDialog.show();
        clickButton.setEnabled(false);

        balanceControl();

        MobileAds.initialize(this,
                "ca-app-pub-3940256099942544~3347511713");

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());


        Bundle bundle = getIntent().getExtras();

        if (bundle != null){
            value = bundle.getString("click");
        }else {
            Toast.makeText(this, "Sorry", Toast.LENGTH_SHORT).show();
        }

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {

                adIsLoaded();

                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {

            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.

                Toast.makeText(Click_Activity.this, "Thank you and Wait 50 second..", Toast.LENGTH_SHORT).show();
                startStop();

            }

            @Override
            public void onAdClosed() {


                if (clickScore==1){
                    clickScoreControl();

                }else {

                    Toast.makeText(Click_Activity.this, " !..Your Work is not Completed. Try Again Ok ", Toast.LENGTH_SHORT).show();
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                }
            }
        });

    }

    //------- OnCreate Ending point-----------

    private void clickScoreControl(){
        Toast.makeText(Click_Activity.this, " Your Work is Successfully Completed ", Toast.LENGTH_SHORT).show();

        clickBalanceControl.AddBalance(clickScore);
        String updateScore= String.valueOf(clickBalanceControl.getBalance());
        myRef.child(user.getUid()).child("ClickCount").setValue(updateScore);
        myRef.child(user.getUid()).child("AdsShowCount").removeValue();


        startActivity(new Intent(Click_Activity.this,MainActivity.class));
        finish();

    }


    private void adIsLoaded() {

        if (mInterstitialAd.isLoaded()){

            clickButton.setEnabled(true);
            progressDialog.dismiss();
        }else {
            progressDialog.dismiss();
            Toast.makeText(this, "Please Check your Net Connections", Toast.LENGTH_SHORT).show();
        }

    }

    private void balanceControl() {


        myRef.child(user.getUid()).child("ClickCount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    String value = dataSnapshot.getValue(String.class);
                    clickBalanceControl.setBalance(Integer.parseInt(value));


                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value

            }
        });

    }



    public void CompleteButton(View view) {

        if (clickScore ==0){

            mInterstitialAd.show();

        }else {

            Toast.makeText(this, " Ad is not loaded...", Toast.LENGTH_SHORT).show();

        }

    }



    private void startStop() {
        if (timeRunning){
            stopTime();
        }else {
            startTime();
        }

    }


    private void startTime() {
        countDownTimer = new CountDownTimer(timeLeft,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft =millisUntilFinished;
                updateTimer();

            }

            @Override
            public void onFinish() {
                clickScore++;
                progressDialog.dismiss();
                myRef.child(user.getUid()).child("AdsShowCount").removeValue();
                Toast.makeText(Click_Activity.this, "Time is Completed...", Toast.LENGTH_SHORT).show();

            }
        }.start();
        timeRunning = true;
        //startBtn.setText("Pause");

    }

    private void updateTimer() {

        progressDialog.show();
        int minutes = (int) (timeLeft /60000);
        int seconds = (int) (timeLeft % 60000 /1000);
        timeText = ""+minutes;
        timeText += ":";
        if (seconds <10)timeText += "0";
        timeText +=seconds;
        //countdownShow.setText(timeText);


    }

    private void stopTime() {
        countDownTimer.cancel();
        timeRunning = false;
        progressDialog.dismiss();
        // startBtn.setText("Start");



    }

}
