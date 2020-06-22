package com.example.jobfinder.Employee;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.jobfinder.ChooseLoginRegistrationActivity;
import com.example.jobfinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class PreviewEmployeeProfileActivity extends AppCompatActivity {
    final static int PICK_PDF_CODE = 2342;
    private static final String LOGTAG = "UserRole";

    private EditText mDescriptionField, mWebsiteField, mContactField, mLinkedInField, mFacebookField, mPhoneField;
    private TextView mNameField, mAgeField, mProfessionField, mCountryField, mCityField;

    private Button mBack, mEditBtn, mPreviewCVBtn, mDeleteBtn, mLogout;

    private ImageView mProfileImage, mFacebookIcon, mLinkedinIcon, mWebsiteIcon, mBackArrowBtnIV;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase, usersDb;

    private String userId, userRole, currentUId, jobId, employeeId;
    private String name, age, profession, description, country, city, contact, phone, userCVUrl, websiteURL, profileImageUrl;
    private String facebook, linkedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_employee_profile);

        employeeId = getIntent().getExtras().getString("employeeId");

        mNameField = (TextView) findViewById(R.id.name_textview);
        mAgeField = (TextView) findViewById(R.id.age_textview);
        mProfessionField = (TextView) findViewById(R.id.profession_textview);
        mCountryField = (TextView) findViewById(R.id.country_textview);
        mCityField = (TextView) findViewById(R.id.city_textview);

        mDescriptionField = (EditText) findViewById(R.id.employeeDescription);
        mContactField = (EditText) findViewById(R.id.contact);
        mPhoneField = (EditText) findViewById(R.id.phone);

        mFacebookIcon = (ImageView) findViewById(R.id.facebook_icon_imageview);
        mLinkedinIcon = (ImageView) findViewById(R.id.linkedin_icon_imageview);
        mWebsiteIcon = (ImageView) findViewById(R.id.website_icon_imageview);

        mProfileImage = (ImageView) findViewById(R.id.profileImage);
        mBackArrowBtnIV = (ImageView) findViewById(R.id.backArrow_imageview);

        mPreviewCVBtn = (Button)  findViewById(R.id.btn_preview_cv);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Employee").child(employeeId);
        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");
        //Log.i(LOGTAG, userId);



        getUserInfo();

        hideEditTextKeypadOnFocusChange();

        mFacebookIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(facebook != null){
                    if (!facebook.startsWith("http://") && !facebook.startsWith("https://"))
                        facebook = "http://" + facebook;
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(facebook));
                    startActivity(browserIntent);
                    return;
                }else{
                    Toast.makeText(PreviewEmployeeProfileActivity.this, "No Facebook Profile Linked!", Toast.LENGTH_LONG).show();
                }

            }
        });
        mLinkedinIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(linkedIn != null){
                    if (!linkedIn.startsWith("http://") && !linkedIn.startsWith("https://"))
                        linkedIn = "http://" + linkedIn;
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkedIn));
                    startActivity(browserIntent);
                    return;
                }else{
                    Toast.makeText(PreviewEmployeeProfileActivity.this, "No LinkedIn Profile Linked!", Toast.LENGTH_LONG).show();
                }


            }
        });
        mWebsiteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(websiteURL != null){
                    if (!websiteURL.startsWith("http://") && !websiteURL.startsWith("https://"))
                        websiteURL = "http://" + websiteURL;
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(websiteURL));
                    startActivity(browserIntent);
                    return;
                }else{
                    Toast.makeText(PreviewEmployeeProfileActivity.this, "No Website Linked!", Toast.LENGTH_LONG).show();
                }

            }
        });
        mPreviewCVBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userCVUrl != null){
                    Intent intent = new Intent(Intent.ACTION_QUICK_VIEW);
                    intent.setData(Uri.parse(userCVUrl));
                    startActivity(intent);
                }
            }
        });
        mBackArrowBtnIV.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                finish();
                return;
            }
        });
    }

    private void getUserInfo() {
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        name = map.get("name").toString();
                        mNameField.setText(name);
                    }
                    if(map.get("age")!=null){
                        age = map.get("age").toString();
                        mAgeField.setText(age);
                    }
                    if(map.get("profession")!=null){
                        profession = map.get("profession").toString();
                        mProfessionField.setText(profession);
                    }
                    if(map.get("description")!=null){
                        description = map.get("description").toString();
                        mDescriptionField.setText(description);
                    }
                    if(map.get("phone")!=null){
                        phone = map.get("phone").toString();
                        mPhoneField.setText(phone);
                    }
                    if(map.get("country")!=null){
                        country = map.get("country").toString();
                        mCountryField.setText(country + ", ");
                    }
                    if(map.get("city")!=null){
                        city = map.get("city").toString();
                        mCityField.setText(city);
                    }
                    if(map.get("facebook")!=null){
                        facebook = map.get("facebook").toString();
                    }
                    if(map.get("linkedIn")!=null){
                        linkedIn = map.get("linkedIn").toString();
                    }
                    if(map.get("websiteURL")!=null){
                        websiteURL = map.get("websiteURL").toString();
                    }
                    if(map.get("phone")!=null){
                        phone = map.get("phone").toString();
                        mPhoneField.setText(phone);
                    }
                    if(map.get("userCVUrl") != null){
                        mPreviewCVBtn.setEnabled(true);
                        userCVUrl = map.get("userCVUrl").toString();
                    }
                    Glide.clear(mProfileImage);

                    if(map.get("profileImageUrl")!=null){
                        profileImageUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(profileImageUrl).into(mProfileImage);
                        switch(profileImageUrl){
                            case "default":
                                Glide.with(getApplication()).load(R.mipmap.ic_launcher).into(mProfileImage);
                                break;
                            default:
                                Glide.with(getApplication()).load(profileImageUrl).into(mProfileImage);
                                break;
                        }
                    }
                    //Log.i(LOGTAG, profileImageUrl);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }



    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void hideEditTextKeypadOnFocusChange(){
        mDescriptionField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        mContactField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        mPhoneField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
    }
}
