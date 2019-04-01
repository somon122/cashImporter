package com.example.user.friendsapp;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import java.util.Random;
import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {

    private Button tapButton;
    private ImageView wheelImage;
    private TextView counterShow;
    private ProgressBar progressBar;

    private Random r;
    private int degree = 0, degree_old = 0;
    private static final float FACTOR = 15f;

    private InterstitialAd mInterstitialAd;


    private FirebaseDatabase database;
    private DatabaseReference myRef;
    FirebaseAuth auth;
    FirebaseUser user;

    private ClickBalanceControl clickBalanceControl;
    private int counter = 0;
    private int backCount = 0;


    CountDownTimer countDownTimer;
    long timeLeft = 10000;
    boolean timeRunning;
    String timeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar2_id);
        setSupportActionBar(toolbar);

        setTitle("Cash Importer");

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Balance");
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        clickBalanceControl = new ClickBalanceControl();

        tapButton = findViewById(R.id.tapButtonId);
        wheelImage = findViewById(R.id.wheel_id);
        progressBar = findViewById(R.id.progressBar2_id);

        tapButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        counterShow= findViewById(R.id.counterShow_Id);


        r = new Random();

        MobileAds.initialize(this,
                getString(R.string.main_App_id));

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.main_intertital_unit_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

      counterShow.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {

              startActivity(new Intent(MainActivity.this,PhoneActivity.class));
          }
      });

        tapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                {
                    degree_old = degree % 360;
                    degree = r.nextInt(3600) + 720;

                    RotateAnimation animationRotate = new RotateAnimation(degree_old,degree,RotateAnimation.RELATIVE_TO_SELF,
                            0.5f,RotateAnimation.RELATIVE_TO_SELF, 0.5f);
                    animationRotate.setDuration(3600);
                    animationRotate.setFillAfter(true);
                    animationRotate.setInterpolator(new DecelerateInterpolator());
                    animationRotate.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                            tapButton.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                            mInterstitialAd.show();
                            startStop();

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                    wheelImage.startAnimation(animationRotate);

                }

            }
        });

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

                Toast.makeText(MainActivity.this, " Don't Click ..........Ok!", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.

                Toast.makeText(MainActivity.this, " You are doing Mistake..... ", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdClosed() {


                if (clickBalanceControl.getBalance() >=40)

                {
                    Intent intent = new Intent(MainActivity.this,Click_Activity.class);
                    intent.putExtra("click","wheel");
                    startActivity(intent);
                    finish();

                }else {

                    //courrentNumber(360 - (degree%360));
                    counter++;
                    clickBalanceControl.AddBalance(counter);
                    String updateClickBalance = String.valueOf(clickBalanceControl.getBalance());
                    myRef.child(user.getUid()).child("AdsShowCount").setValue(updateClickBalance);
                    //progressDialog.show();
                    progressBar.setVisibility(View.VISIBLE);
                    tapButton.setVisibility(View.GONE);
                    gameOver();

                }

            }
        });

    }

    //------- OnCreate Ending point-----------

    @Override
    protected void onStart() {
        super.onStart();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {

            Intent intent = new Intent(MainActivity.this, PhoneActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }else {

                BalanceControl();

        }

    }

    private void BalanceControl() {


        myRef.child(user.getUid()).child("AdsShowCount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    String value = dataSnapshot.getValue(String.class);
                    clickBalanceControl.setBalance(Integer.parseInt(value));
                    counterShow.setText("Show: "+clickBalanceControl.getBalance());

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value

            }
        });

    }

    private void adIsLoaded() {

        if (mInterstitialAd.isLoaded()){
            tapButton.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

        }else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Please Check your Net Connections", Toast.LENGTH_SHORT).show();
        }

    }

    private String courrentNumber (int degrees){
        String text = "";

        if (degrees >= (FACTOR *1) && degrees < (FACTOR * 3)){

            Toast.makeText(this, "Great Work!", Toast.LENGTH_SHORT).show();

        } if (degrees >= (FACTOR *3) && degrees < (FACTOR * 5)){

            Toast.makeText(this, "Great Work!", Toast.LENGTH_SHORT).show();

        } if (degrees >= (FACTOR *5) && degrees < (FACTOR * 7)){

            Toast.makeText(this, "Great Work!", Toast.LENGTH_SHORT).show();

        } if (degrees >= (FACTOR *7) && degrees < (FACTOR * 9)){

            Toast.makeText(this, "Great Work!", Toast.LENGTH_SHORT).show();

        } if (degrees >= (FACTOR *9) && degrees < (FACTOR * 11)){

            Toast.makeText(this, "Great Work!", Toast.LENGTH_SHORT).show();

        } if (degrees >= (FACTOR *11) && degrees < (FACTOR * 13)){

            Toast.makeText(this, "Great Work!", Toast.LENGTH_SHORT).show();

        } if (degrees >= (FACTOR *13) && degrees < (FACTOR * 15)){

            Toast.makeText(this, "Great Work!", Toast.LENGTH_SHORT).show();

        } if (degrees >= (FACTOR *15) && degrees < (FACTOR * 17)){

            Toast.makeText(this, "Great Work!", Toast.LENGTH_SHORT).show();

        } if (degrees >= (FACTOR *17) && degrees < (FACTOR * 19)){

            Toast.makeText(this, "Great Work!", Toast.LENGTH_SHORT).show();

        } if (degrees >= (FACTOR *19) && degrees < (FACTOR * 21)){

            Toast.makeText(this, "Great Work!", Toast.LENGTH_SHORT).show();

        } if (degrees >= (FACTOR *21) && degrees < (FACTOR * 23)){

            Toast.makeText(this, "Great Work!", Toast.LENGTH_SHORT).show();

        }

        if ((degrees >= (FACTOR * 23 ) && degrees < 360) || (degrees >= 0 && degrees < (FACTOR * 1)))
        {

            Toast.makeText(this, "Great Work!", Toast.LENGTH_SHORT).show();

        }

        return text;

    }

    @Override
    public void onBackPressed() {

        back();

    }

    private void gameOver(){

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setMessage(" Great Work ...!" +
                "\n"+" Click Ok For Continue Game ..." +
                "\n")
                .setCancelable(false)
                .setPositiveButton(" Ok ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        finish();


                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();


    }
 private void back(){

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setMessage("Are you sure to exit ...!")
                .setCancelable(false)
                .setPositiveButton(" Ok ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        finish();


                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "Thank you for stay !", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();


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

                    Toast.makeText(MainActivity.this, "Show Successfully", Toast.LENGTH_SHORT).show();

                }
            }.start();
            timeRunning = true;
            //startBtn.setText("Pause");

        }

        private void updateTimer() {

            //progressDialog.show();
            int minutes = (int) (timeLeft /60000);
            int seconds = (int) (timeLeft % 60000 /1000);
            timeText = ""+minutes;
            timeText += ":";
            if (seconds <10)timeText += "0";
            timeText +=seconds;
            //timeTv.setText(timeText);


        }

        private void stopTime() {
            countDownTimer.cancel();
            timeRunning = false;
            //progressDialog.dismiss();
            // startBtn.setText("Start");



        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_layout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.logOut_id) {

            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, PhoneActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    }

