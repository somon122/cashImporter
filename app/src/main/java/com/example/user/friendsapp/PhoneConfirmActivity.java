package com.example.user.friendsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneConfirmActivity extends AppCompatActivity {


    Button confirmButton;
    EditText codeET;
    String varificationId;
    FirebaseAuth auth;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_confirm);


        auth= FirebaseAuth.getInstance();
        confirmButton = findViewById(R.id.confirmCode_Id);
        codeET = findViewById(R.id.codeEditT_id);
        progressDialog = new ProgressDialog(this);

        String phoneNumber = getIntent().getStringExtra("phoneNumber");

        Toast.makeText(this, phoneNumber, Toast.LENGTH_SHORT).show();

        sentVarificationCode(phoneNumber);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String codeNumber = codeET.getText().toString().trim();
                progressDialog.show();

                if (codeNumber.isEmpty() || codeNumber.length()<6){

                    codeET.setError("Please Enter Code...");
                    codeET.requestFocus();
                    progressDialog.dismiss();
                    return;


                }else {
                    verifyCode(codeNumber);
                    progressDialog.dismiss();


                }



            }
        });


    }

    //-------------    end the onCreate method



    private void signInWithCredential(PhoneAuthCredential credential) {

        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){

                            Intent intent = new Intent(PhoneConfirmActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            progressDialog.dismiss();



                        }else {
                            Toast.makeText(PhoneConfirmActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });

    }


    private  void sentVarificationCode(String number){

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallBack
        );
        progressDialog.show();


    }

    private  PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            varificationId = s;



        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            progressDialog.show();
            String code = phoneAuthCredential.getSmsCode();
            if (code != null)
            {
                codeET.setText(code);
                verifyCode(code);
            }

        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

            Toast.makeText(PhoneConfirmActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    };

    private void verifyCode( String code){

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(varificationId,code);
        signInWithCredential(credential);


    }
}
