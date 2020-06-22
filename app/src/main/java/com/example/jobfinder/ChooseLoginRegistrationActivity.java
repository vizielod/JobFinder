package com.example.jobfinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.jobfinder.Employer.EmployerTabbedMainActivity;

public class ChooseLoginRegistrationActivity extends AppCompatActivity {

    private Button mLogin, mGoogleLoginBtn;
    private TextView mRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_login_registration);

        mLogin = (Button) findViewById(R.id.login_email);
        mGoogleLoginBtn = (Button) findViewById(R.id.login_google);
        mRegister = (TextView) findViewById(R.id.register);

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseLoginRegistrationActivity.this, LoginActivity.class);
                intent.putExtra("LoginMode", "EmailLogin");
                startActivity(intent);
                //finish();
                return;
            }
        });

        mGoogleLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseLoginRegistrationActivity.this, LoginActivity.class);
                intent.putExtra("LoginMode", "GoogleLogin");
                startActivity(intent);
                //finish();
                return;
            }
        });

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseLoginRegistrationActivity.this, RegistrationActivity.class);
                startActivity(intent);
                //Finish nem kell, ha azt szeretném, hogy simán megnyomva a home gombot csak vissza lépjen az előző activity-re és ne tálcára vigye az app-ot
                //finish();
                return;
            }
        });
    }
}
